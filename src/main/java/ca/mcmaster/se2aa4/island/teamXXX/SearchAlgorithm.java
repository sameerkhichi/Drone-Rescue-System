package ca.mcmaster.se2aa4.island.teamXXX;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class SearchAlgorithm {
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

    public JSONObject getNextMove() {
        decision = new JSONObject();
        headingParams = new JSONObject();
        radarParams = new JSONObject();
        
        if (scanState == ScanState.LINE_SCAN) {
            if (scanStage == ScanStage.PRE_SCAN) {
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
                        } else { // fly towards land
                            decision.put("action", "fly");
                            drone.move();
                            currentFlyCounter++;
                            //logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                        }
                    } else {
                        // END OF ISLAND LOGIC GOES HERE
                        
                        scanState = ScanState.END_OF_ISLAND;
                        decision.put("action", "echo");
                        radarParams.put("direction", "N");
                        decision.put("parameters", radarParams);
                        
                         // Temporary
                        endOfIslandDirection = drone.getHeading();
                        endOfIslandTurnCounter = 1;
                        continueFlying = false;
                    }
                    drone_radar.resetEchoUsage();
                }
            } else if (scanStage == ScanStage.SCAN) {
                if (drone_scanner.hasOceanOnly()) {
                    decision.put("action", "echo");
                    radarParams.put("direction", drone.getHeading());
                    decision.put("parameters", radarParams);
                    //logger.info("Drone is scanning in direction: {}", radarParams);
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
                if (drone_radar.echoLastUsed()) {
                    if (!drone_radar.hasGroundAhead()) { // if echo is OUT OF BOUNDS (no ground found ahead), initiate U-Turn
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
                            }
                        }
                    }
                    drone_radar.resetEchoUsage();
                } else {
                    decision.put("action", "echo");
                    radarParams.put("direction", drone.getHeading());
                    decision.put("parameters", radarParams);
                    //logger.info("Drone is scanning in direction: {}", radarParams);
                }
            }
        }

        if (scanState == ScanState.U_TURN) {
            decision.put("action", "heading");

            if (uTurnDirection == UTurnDirection.TURN_RIGHT) {
                drone.changeDirection("R");
            } else {
                drone.changeDirection("L"); 
            }

            headingParams.put("direction", drone.getHeading()); 
            decision.put("parameters", headingParams);

            if (uTurnCounter == 2) {
                scanState = ScanState.LINE_SCAN;
                scanStage = ScanStage.PRE_SCAN;
            } else {
                uTurnCounter++;
            }
        }

        if (scanState == ScanState.END_OF_ISLAND) {
            logger.info("PERFORMING END OF ISLAND LOGIC");
            if (scanDirection == ScanDirection.SCAN_DOWN) { // If first pass
                if (!continueFlying) { // if drone is not allowed to fly continously without echoing above
                    logger.info("NOT IN CONTINUE FLYING MODE");
                    if (drone_radar.echoLastUsed()) {
                        if (previousFlyCounter > 0) {
                            if ((drone_radar.getRange() <= 1 && drone_radar.hasGroundAhead()) || endOfIslandTurnCounter == 2) {
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
                        scanState = ScanState.U_TURN_END_OF_ISLAND;
                        uTurnCounter = 1;
                    }
                }
            } else { // else if second pass
                decision.put("action", "stop"); // Temporary
            }
        }

        if (scanState == ScanState.U_TURN_END_OF_ISLAND) {
            logger.info("PERFORMING U TURN AT END OF ISLAND LOGIC");
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
