
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
    private ScanResults scanResults;
    private DroneSearchMode droneSearchMode;
    private SearchStrategy searchAlgorithm;
    private IslandDetection islandSearch;

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
        scanResults = new ScanResults();
        drone_scanner = new PhotoScanner(drone, scanResults);
        droneSearchMode = DroneSearchMode.START;
        searchAlgorithm = new SearchAlgorithm(drone, drone_radar, drone_scanner);
        islandSearch = new IslandSearch(drone, drone_radar);
    }

    //use this method to call a specific request to the drone
    @Override
    public String takeDecision() {
        //create json objects for each action you would be doing
        JSONObject decision = new JSONObject();

        // Stops search if the drone ran out of battery - leaves enough budget to return to base.
        if (drone.getBattery() <= 20) {
            droneSearchMode = DroneSearchMode.OFF;
        }

        if (droneSearchMode == DroneSearchMode.START) {
            //turn to the south and then initiate the ground search
            decision = islandSearch.initiateGroundSearch();
            droneSearchMode = DroneSearchMode.FIND_GROUND;
        } 
        else if (droneSearchMode == DroneSearchMode.FIND_GROUND) {
            
            logger.info("CURRENTLY LOOKING FOR THE ISLAND");

            decision = islandSearch.getNextMove();

            //if the island is found change to initiate search algorithm
            if(islandSearch.foundIsland()){
                droneSearchMode = DroneSearchMode.FIND_CREEK;
            }
        }
        else if (droneSearchMode == DroneSearchMode.FIND_CREEK) {

            logger.info("CURRENTLY USING FIND CREEK STRATEGY");

            decision = searchAlgorithm.getNextMove();
        }
        
        //maybe you could do this first, and you may find a creek in the process
        else if(droneSearchMode == DroneSearchMode.FIND_SITE){

        }

        //when the drone dies
        else if (droneSearchMode == DroneSearchMode.OFF) {
            logger.info("Drone has run out of battery!");
            decision.put("action", "stop"); // we stop the exploration immediately
        }
        
        logger.info("** Decision: {}",decision.toString());
        logger.debug("Current Creek Locations: {}", scanResults.getCreekLocations());
        logger.debug("creekID's " + scanResults.getCreekIDs());
        logger.debug("Current Closest/Chosen Creek: {}", scanResults.getClosestCreek());
        logger.debug("emergency site coordinate: {},{}", scanResults.getSiteX(), scanResults.getSiteY());

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

        logger.debug("DRONE IS AT X: " + drone.getX());

        //deplete the drone battery by the cost
        drone.updateBatteryLife(cost); 

        if(!extraInfo.isEmpty()){
            if (extraInfo.has("found")) { // if action was echo
                //updating the range and found (GROUND/OUT OF RANGE) in radar 
                drone_radar.updateRadarData(extraInfo);
            } else if (extraInfo.has("creeks")) { // if action was scan
                logger.debug("Checking results of scan");
                drone_scanner.updateScanData(extraInfo);
            }   
        }
        if(extraInfo.isEmpty() && !drone.getTurningStatus()){
            drone_radar.nothingFound();
        }



    }

    //this method is to deliver the identifier of the creek which the rescue boat will be sent
    @Override
    public String deliverFinalReport(){
        logger.info("Rescue Boat should be sent to creek: {}", scanResults.getClosestCreek());
        return String.format("\nRescue boat should be sent to creek: %s", scanResults.getClosestCreek());
    }


}