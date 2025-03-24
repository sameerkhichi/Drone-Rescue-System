package ca.mcmaster.se2aa4.island.teamXXX;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class SearchAlgorithm implements SearchStrategy{
    private final Logger logger = LogManager.getLogger();

    private DroneState drone;
    private Radar drone_radar;
    private PhotoScanner drone_scanner;
    private ScanStage scanStage;
    private ScanState scanState;
    private ScanDirection scanDirection;
    private UTurnDirection uTurnDirection;
    private int uTurnCounter;
    private int currentFlyCounter;
    private int previousFlyCounter;
    private int endOfIslandTurnCounter;
    private String endOfIslandDirection;
    private boolean continueFlying;
    private JSONObject decision;
    private JSONObject headingParams;
    private JSONObject radarParams;

    // Constructor
    public SearchAlgorithm(DroneState drone, Radar drone_radar, PhotoScanner drone_scanner) {
        scanStage = ScanStage.PRE_SCAN;
        scanState = ScanState.LINE_SCAN;
        scanDirection = ScanDirection.SCAN_DOWN;
        this.drone = drone;
        this.drone_radar = drone_radar;
        this.drone_scanner = drone_scanner;
        currentFlyCounter = 0;
        previousFlyCounter = 0;
        continueFlying = false;
    }

    /* 
     * This method returns a JSONObject containing the instructions for the drone's next move
     * 
     * Overall Strategy:
     * - Scan every other line on the way down
     * - Scan the rest of the lines on the way up
     */
    public JSONObject getNextMove() {
        decision = new JSONObject();
        headingParams = new JSONObject();
        radarParams = new JSONObject();

        logger.info("SCAN DIRECTION: " + scanDirection);
        
        // LINE_SCAN state handles the island scanning mechanism in a horizontal manner
        /*
         * State Strategy:
         * - Pre-scan stage checks if there is ground ahead before scanning, flys towards ground if found
         *      - Continues to scan stage once the drone is directly on top of ground
         *      - Changes to end of island state if no ground is found ahead
         * - Scan stage alternates between flying and scanning the ground
         *      - If the drone is directly on top of a tile containing the ocean biome only, it transitions to the post-scan stage
         * - Post-scan stage initiates a U-Turn if no more ground is found ahead
         *      - Otherwise, it flys towards the ground and goes back to the scan stage once on top of ground
         */
        if (scanState == ScanState.LINE_SCAN) {
            logger.info("CURRENTLY IN LINE_SCAN STATE");
            if (scanStage == ScanStage.PRE_SCAN) {
                logger.info("CURRENTLY IN PRE_SCAN STAGE");
                if (!drone_radar.echoLastUsed()) {
                    decision.put("action", "echo");
                    radarParams.put("direction", drone.getHeading());
                    decision.put("parameters", radarParams);
                    //logger.info("Drone is scanning in direction: {}", radarParams);
                } else {
                    if (drone_radar.hasGroundAhead()) {
                        if (drone_radar.getRange() == 0) {
                            decision.put("action", "scan");
                            scanStage = ScanStage.SCAN;
                            logger.info("CHANGING TO SCAN STAGE");
                        } else { // fly towards land
                            decision.put("action", "fly");
                            drone.move();
                            currentFlyCounter++;
                            //logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                        }
                        drone_radar.resetEchoUsage();
                    } else {
                        // END OF ISLAND LOGIC GOES HERE
                        logger.info("CHANGING TO END_OF_ISLAND STATE");
                        
                        scanState = ScanState.END_OF_ISLAND;
                        decision.put("action", "echo");
                        radarParams.put("direction", "N");
                        decision.put("parameters", radarParams);
                        
                        endOfIslandDirection = drone.getHeading();
                        endOfIslandTurnCounter = 1;
                        continueFlying = false;

                        drone_radar.resetEchoUsage();
                        return decision;
                    }
                }
            } else if (scanStage == ScanStage.SCAN) {
                logger.info("CURRENTLY IN SCAN STATE");
                if (drone_scanner.hasOceanOnly()) {
                    decision.put("action", "echo");
                    radarParams.put("direction", drone.getHeading());
                    decision.put("parameters", radarParams);
                    //logger.info("Drone is scanning in direction: {}", radarParams);
                    logger.info("CHANGING TO POST_SCAN STAGE");
                    scanStage = ScanStage.POST_SCAN;
                } else {
                    // If the tile is already scanned, fly forwards
                    if (drone_scanner.isTileScanned()) {
                        decision.put("action", "fly");
                        drone_scanner.resetScannedTile(); // Move to a new, unscanned tile
                        drone.move();
                        currentFlyCounter++;
                        //logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                    } else {
                        decision.put("action", "scan");
                    }
                }
            } else { // if scanStage == ScanStage.POST_SCAN
                logger.info("CURRENTLY IN POST_SCAN STATE");
                if (drone_radar.echoLastUsed()) {
                    if (!drone_radar.hasGroundAhead()) { // if echo is OUT OF BOUNDS (no ground found ahead), initiate U-Turn
                        // Note: decision is assigned in the U_TURN state, not within this state
                        logger.info("CHANGING TO U_TURN STATE");
                        scanState = ScanState.U_TURN;
                        previousFlyCounter = currentFlyCounter;
                        currentFlyCounter = 0;
                        uTurnCounter = 1;
                        if (drone.getHeading().equals("E")) {
                            if (scanDirection == ScanDirection.SCAN_DOWN) {
                                uTurnDirection = UTurnDirection.TURN_RIGHT;
                            } else {
                                uTurnDirection = UTurnDirection.TURN_LEFT;
                            }
                        } else { // If heading West
                            if (scanDirection == ScanDirection.SCAN_DOWN) {
                                uTurnDirection = UTurnDirection.TURN_LEFT;
                            } else {
                                uTurnDirection = UTurnDirection.TURN_RIGHT;
                            }
                        }
                    } else { // if echo found GROUND
                        if (drone_radar.getRange() > 0) { // if not on top of GROUND
                            decision.put("action", "fly");
                            drone_scanner.resetScannedTile();
                            drone.move();
                            currentFlyCounter++;
                            //logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                        } else { // if on top of GROUND
                            if (drone_scanner.isTileScanned() && drone_scanner.hasOceanOnly()) { // if tile is both OCEAN and GROUND
                                decision.put("action", "fly");
                                drone_scanner.resetScannedTile();
                                drone.move();
                                currentFlyCounter++;
                                //logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                            } else {
                                decision.put("action", "scan");
                                scanStage = ScanStage.SCAN;
                                logger.info("CHANGING TO SCAN STAGE");
                            }
                        }
                    }
                    drone_radar.resetEchoUsage();
                } else { // if echo wasn't used last turn
                    decision.put("action", "echo");
                    radarParams.put("direction", drone.getHeading());
                    decision.put("parameters", radarParams);
                    //logger.info("Drone is scanning in direction: {}", radarParams);
                }
            }
        }

        // U_TURN state handles basic U-Turn logic (not at the top/bottom ends of the island)
        /*
         * State Strategy:
         * - Turns right twice or left twice depending on the current direction (heading) and the scan direction
         * - Once complete, it transitions to the line scan state
         */
        if (scanState == ScanState.U_TURN) {
            logger.info("CURRENTLY IN U_TURN STAGE");
            decision.put("action", "heading");

            if (uTurnDirection == UTurnDirection.TURN_RIGHT) {
                drone.changeDirection("R");
            } else {
                drone.changeDirection("L"); 
            }

            headingParams.put("direction", drone.getHeading()); 
            decision.put("parameters", headingParams);

            if (uTurnCounter == 2) {
                logger.info("CHANGING TO LINE_SCAN STATE");
                scanState = ScanState.LINE_SCAN;
                scanStage = ScanStage.PRE_SCAN;
                logger.info("CHANGING TO PRE_SCAN STAGE");
            } else {
                uTurnCounter++;
            }
        }

        // END_OF_ISLAND state handles logic at the ends of the island (bottom and top)
        /*
         * State Strategy:
         * - If at the bottom of the island, the drone will fly however many tiles were flown on the last line scan
         * - If the drone encounters any land right beside it (range 0), the drone will move two tiles in the opposite direction from it
         *      - This allows the drone to be able to scan those unscanned tiles identified next to it
         * - Once it is done flying, it will initiate a U-Turn specialized for the end of island logic
         */
        if (scanState == ScanState.END_OF_ISLAND) {
            logger.info("CURRENTLY IN END_OF_ISLAND STATE");
            if (scanDirection == ScanDirection.SCAN_DOWN) { // If first pass
                if (!continueFlying) { // if drone is not allowed to fly continously without echoing above
                    logger.info("NOT IN CONTINUE FLYING MODE");
                    if (drone_radar.echoLastUsed()) {
                        if (previousFlyCounter > 0) {
                            // If the drone finds ground right next to it, it moves two tiles away from it
                            if ((drone_radar.getRange() == 0 && drone_radar.hasGroundAhead()) || endOfIslandTurnCounter == 2) {
                                logger.info("MOVING AWAY FROM COAST");
                                decision.put("action", "heading");
                                if (endOfIslandTurnCounter == 1) {
                                    if (endOfIslandDirection == "E") {
                                        drone.changeDirection("R");
                                    } else { // WEST
                                        drone.changeDirection("L");
                                    }
                                    endOfIslandTurnCounter++;
                                } else {
                                    if (endOfIslandDirection == "E") {
                                        drone.changeDirection("L");
                                    } else { // WEST
                                        drone.changeDirection("R");
                                    }
                                    endOfIslandTurnCounter = 1;
                                    continueFlying = true;
                                    drone_radar.resetEchoUsage();
                                }
                                headingParams.put("direction", drone.getHeading()); 
                                decision.put("parameters", headingParams);
                            } else {
                                decision.put("action", "fly");
                                drone.move();
                                previousFlyCounter--;
                                drone_radar.resetEchoUsage();
                            }
                        } else {
                            logger.info("CHANGING TO U_TURN_AT_END_OF_ISLAND STATE");
                            scanState = ScanState.U_TURN_END_OF_ISLAND;
                            uTurnCounter = 1;
                            drone_radar.resetEchoUsage();
                        }
                    } else {
                        decision.put("action", "echo");
                        radarParams.put("direction", "N");
                        decision.put("parameters", radarParams);
                    }
                } else {
                    logger.info("IN CONTINUE FLYING MODE");
                    if (previousFlyCounter > 0) {
                        decision.put("action", "fly");
                        drone.move();
                        previousFlyCounter--;
                    } else {
                        logger.info("CHANGING TO U_TURN_AT_END_OF_ISLAND STATE");
                        scanState = ScanState.U_TURN_END_OF_ISLAND;
                        uTurnCounter = 1;
                    }
                }
            } else { // else if second pass
                logger.info("STOP SEARCHING DUE TO SECOND PASS");
                decision.put("action", "stop");
            }
        }

        // U_TURN_END_OF_ISLAND state handles a special kind of U-Turn that allows the drone to scan the unscanned lines
        // Only used after END_OF_ISLAND state
        /*
         * State Strategy:
         * - Turns
         * - Flys once
         * - Turns again
         * - Once U-Turns is complete, it will transition to the line scan state
         */
        if (scanState == ScanState.U_TURN_END_OF_ISLAND) {
            logger.info("CURRENTLY IN U_TURN_AT_END_OF_ISLAND STATE");
            if (uTurnCounter == 1 || uTurnCounter == 3) {
                decision.put("action", "heading");
                if (endOfIslandDirection == "E") {
                    drone.changeDirection("L");
                } else {
                    drone.changeDirection("R");
                }
                headingParams.put("direction", drone.getHeading()); 
                decision.put("parameters", headingParams);
                if (uTurnCounter == 3) {
                    logger.info("CHANGING TO LINE_SCAN STATE");
                    logger.info("CHANGING TO PRE_SCAN STAGE");
                    scanState = ScanState.LINE_SCAN;
                    scanStage = ScanStage.PRE_SCAN;
                    scanDirection = ScanDirection.SCAN_UP;
                } else {
                    uTurnCounter++;
                }
            } else { // if uTurnCounter == 2
                decision.put("action", "fly");
                drone.move();
                uTurnCounter++;
            }
        }

        return decision;
    }
}