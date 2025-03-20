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

        // If the drone's radar detected ground
        if (drone_radar.getFound().equalsIgnoreCase("GROUND")) {
            // If the drone is currently over ground, scan
            if (drone_radar.getRange() == 0) {
                decision.put("action", "scan");
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
                islandLogger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                islandLogger.debug(drone.getBattery());
            }
        }
        else {
            // doing the radar part here:
            decision.put("action", "echo");
            radarParams.put("direction", "E"); // change the heading to scan on the left and right of the wings
            decision.put("parameters", radarParams);
            islandLogger.info("Drone is scanning in direction: {}", radarParams);
        }


        return decision;
    }

    //if the island 
    public boolean foundIsland(){
        if(droneSearchMode == DroneSearchMode.FIND_CREEK){
            return true;
        }
        return false;
    }



}