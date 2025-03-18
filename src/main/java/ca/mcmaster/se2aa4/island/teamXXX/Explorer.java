
// grid is 53 x 52. 53 squares to east and 52 below. Drone starts at top left


package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject; 
import org.json.JSONTokener;

import eu.ace_design.island.bot.IExplorerRaid;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private DroneState drone;
    private Radar drone_radar;
    private PhotoScanner drone_scanner;
    private DroneSearchMode droneSearchMode;
    private SearchStatus searchStatus = null;
    private int flyCounter = 0;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.debug("The drone is facing {}", direction);
        logger.debug("Battery level is {}", batteryLevel);

        //starting at 0,0 for now 
        drone = new DroneState(0, 0, direction, batteryLevel);
        logger.info("Drone initialized at ({}, {}), facing {}, with battery {}", 0, 0, direction, batteryLevel);

        drone_radar = new Radar();
        drone_scanner = new PhotoScanner();
        droneSearchMode = DroneSearchMode.START;
    }

    //use this method to call a specific request to the drone
    @Override
    public String takeDecision() {
        //create json objects for each action you would be doing
        JSONObject decision = new JSONObject();
        JSONObject headingParams = new JSONObject();
        JSONObject radarParams = new JSONObject();

        // Stops search if the drone ran out of battery
        if (drone.getBattery() <= 0) {
            droneSearchMode = DroneSearchMode.OFF;
        }

        if (droneSearchMode == DroneSearchMode.START) {
            //changing the heading is costly, so we want to avoid doing this too much
            decision.put("action", "heading"); //change direction to south to start
            drone.changeDirection("R"); 
            headingParams.put("direction", drone.getHeading()); 
            decision.put("parameters", headingParams); //cant pass string in here must be JSON object - use wrapper JSON
            droneSearchMode = DroneSearchMode.FIND_GROUND; // change to find ground mode
        } 
        else if (droneSearchMode == DroneSearchMode.FIND_GROUND) {

            /*
             * Strategy:
             * - Keep moving South until land is found to the East
             * - Turn to the East and continue flying forwards until directly over ground
             */

            // If the drone's radar detected ground
            if (drone_radar.getFound().equalsIgnoreCase("GROUND")) {
                // If the drone is currently over ground, scan
                if (drone_radar.getRange() == 0) {
                    decision.put("action", "scan");
                    //drone_radar.resetRange(); // resets range so that drone is able to fly again
                    droneSearchMode = DroneSearchMode.FIND_CREEK; // Change to find creek mode once island is found
                }
                //if the drone is still heading south after finding land to the east
                else if (drone.getHeading().equalsIgnoreCase("S")) {
                    drone.setTurningStatus(true);

                    //turning back towards the east and fly that way
                    decision.put("action", "heading");
                    drone.changeDirection("L"); 
                    headingParams.put("direction", drone.getHeading()); 
                    decision.put("parameters", headingParams);
                }
                else {
                    //finished the turn start flying east
                    drone.setTurningStatus(false);
                    decision.put("action", "fly"); //fly forward
                    drone.move(); //this method is from the DroneState Class basically makes the drone move
                    logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                    logger.debug(drone.getBattery());
                }
            }
            else {
                // doing the radar part here:
                decision.put("action", "echo");
                radarParams.put("direction", "E"); // change the heading to scan on the left and right of the wings
                decision.put("parameters", radarParams);
                logger.info("Drone is scanning in direction: {}", radarParams);
            }
        }
        else if (droneSearchMode == DroneSearchMode.FIND_CREEK) {
            /*
             * Strategy:
             * - Navigate drone around coastline to find creeks
             * when you scan and there are more than one biomes (something and ocean)
             * go down one row and start scanning that otherwise youll lose the island
             */

            // PUT ACTIONS HERE
            //if its on the right turn right twice 

            logger.info("THIS IS THE FLYCOUNTER: " + flyCounter);
            if(flyCounter > 5){ 
                // had to add this counter to prevent turning of drone at first instance of finding the island. This forces drone to move at least 5 spaces before checking endofisland
                // note still getting a "Cannot turn [SOUTH] when heading [SOUTH]" error
                
                //This stops the drone from trying to turn too many times by forcing biomes to be empty 
                if(drone.getHeading().equalsIgnoreCase("S")){
                    drone_scanner.forceBiomesEmpty();
                }

                if((drone_scanner.endOfIsland() && drone.getHeading().equalsIgnoreCase("E")) || searchStatus == SearchStatus.RIGHT_SIDE_TURN){
                    logger.info("I made it in here");
                    //initiating the turning around sequence
                    decision.put("action", "heading");
                    drone.changeDirection("R"); 
                    headingParams.put("direction", drone.getHeading()); 
                    decision.put("parameters", headingParams);
    
                    //found a bug here, if you put another action for heading in here it will turn twice weirdly, could you use to search for the site
                    if(searchStatus == SearchStatus.RIGHT_SIDE_TURN){
                        searchStatus = null;
                    }
                    else{
                        searchStatus = SearchStatus.RIGHT_SIDE_TURN;
                    }
                }
                else if(drone.getHeading().equalsIgnoreCase("S")){
                    decision.put("action", "heading");
                    drone.changeDirection("R");
                    logger.info("this one");
                    headingParams.put("direction", drone.getHeading());
                    decision.put("parameters", headingParams);
                }

            }
            
            if(drone.getHeading().equalsIgnoreCase("S")){
                drone_scanner.forceBiomesEmpty();
                searchStatus = SearchStatus.OFF;
            }

            //if its on the left turn left twice
            if((drone_scanner.endOfIsland() && drone.getHeading().equalsIgnoreCase("W")) || searchStatus == SearchStatus.LEFT_SIDE_TURN){
                //here is where you need to turn around, remember turning moves the plane forward, so it will automatically go down one.
                decision.put("action", "heading");
                drone.changeDirection("L"); 
                headingParams.put("direction", drone.getHeading()); 
                decision.put("parameters", headingParams);

                if(searchStatus == SearchStatus.LEFT_SIDE_TURN){
                    searchStatus = null;
                }
                else{
                    searchStatus = SearchStatus.LEFT_SIDE_TURN;
                }
            }


            else if(searchStatus == SearchStatus.CREEK_SEARCH){
                decision.put("action", "scan");
                searchStatus = null;
            }

            else if(searchStatus == null){
                decision.put("action", "fly");
                drone.move();
                logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                searchStatus = SearchStatus.CREEK_SEARCH;
                logger.info("FOUND ISLAND, NOW WE ARE TRYING TO FLY ACROSS");
                flyCounter++;
            }

        }
        //maybe you could do this first, and you may find a creek in the process
        else if(droneSearchMode == DroneSearchMode.FIND_SITE){

        }
        else if (droneSearchMode == DroneSearchMode.OFF) {
            logger.info("Drone has run out of battery!");
            decision.put("action", "stop"); // we stop the exploration immediately
        }
        
        logger.info("** Decision: {}",decision.toString());
        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) { //will loop between this and the takeDecision until action stop
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));

        logger.info("** Response received:\n"+response.toString(2));

        Integer cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);

        String status = response.getString("status");
        logger.info("The status of the drone is {}", status);

        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}\n", extraInfo);

        //deplete the drone battery by the cost
        drone.updateBatteryLife(cost); 

        if(!extraInfo.isEmpty()){
            if (extraInfo.has("found")) { // if action was echo
                //updating the range and found (GROUND/OUT OF RANGE) in radar 
                drone_radar.updateRadarData(extraInfo);
            } else if (extraInfo.has("creeks")) { // if action was scan
                logger.info("Checking results of scan");
                drone_scanner.updateScanData(extraInfo);
            }
        }
        if(extraInfo.isEmpty() && !drone.getTurningStatus()){
            drone_radar.nothingFound();
        }

    }

    @Override
    public String deliverFinalReport() { //doesnt give anything rn - to update later 
        return "no creek found";
    }

}