package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandSearch{

    private Radar drone_radar;
    private DroneState drone;
    private JSONObject decision;
    private JSONObject headingParams;
    private JSONObject radarParams;
    private DroneSearchMode droneSearchMode;

    private final Logger islandLogger = LogManager.getLogger();

    public IslandSearch(DroneState drone, Radar drone_radar){
        this.drone = drone;
        this.drone_radar = drone_radar;
    }   

    public JSONObject getNextMove(){

        decision = new JSONObject();
        headingParams = new JSONObject();
        radarParams = new JSONObject();

        /*
        * Strategy:
        * - Keep moving South until land is found to the East
        * - Turn to the East and continue flying forwards until directly over ground
        */

        /*
         * CURRENT PROBLEM
         * when the drone turns east to fly towards the island it moves one down - you need to adjust for this.
         * Fly one more time south - turn east - then turn north - then turn east again.
         */

        
        


        return decision;
    }

    //if the island 
    public boolean foundIsland(){
        if(droneSearchMode == DroneSearchMode.FIND_CREEK){
            return true;
        }
        return false;
    }

    public JSONObject initiateGroundSearch(){

        decision = new JSONObject();
        headingParams = new JSONObject();

        //changing the heading is costly, so we want to avoid doing this too much
        decision.put("action", "heading"); //change direction to south to start
        drone.changeDirection("R"); 
        headingParams.put("direction", drone.getHeading()); 
        decision.put("parameters", headingParams); //cant pass string in here must be JSON object - use wrapper JSON
        droneSearchMode = DroneSearchMode.FIND_GROUND; // change to find ground mode

        return decision;
    }



}