package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandSearch implements IslandDetection{

    private Radar drone_radar;
    private DroneState drone;
    private JSONObject decision;
    private JSONObject headingParams;
    private JSONObject radarParams;
    private IslandSearchStatus islandSearchStatus = IslandSearchStatus.SETUP_FOR_TURN;
    private IslandSearchStatus groundPresent;

    private int distanceToIsland;


    private final Logger islandLogger = LogManager.getLogger();

    public IslandSearch(DroneState drone, Radar drone_radar){
        this.drone = drone;
        this.drone_radar = drone_radar;
    }   

    @Override
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
         * Maintain drone y level: when turning towards the island
         * when the drone turns east to fly towards the island it moves one down - you need to adjust for this.
         * Fly one more time south - turn east - then turn north - then turn east again.
         */

        
        //if ground is found currently - or ground was found and status was preserved 
        if(drone_radar.getFound().equalsIgnoreCase("GROUND") || groundPresent == IslandSearchStatus.GROUND_DETECTED){

            //if ground is found it should always enter this if statement
            groundPresent = IslandSearchStatus.GROUND_DETECTED;

            // reset echo usage once ground was found
            drone_radar.resetEchoUsage();

            //these if statements follow a chain - they execute one after the other using the enums
            if(islandSearchStatus == IslandSearchStatus.SETUP_FOR_TURN){
                decision.put("action", "fly");
                drone.move();
                islandLogger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                islandLogger.debug(drone.getBattery());
                islandSearchStatus = IslandSearchStatus.INITIAL_TURN;
                return decision;
            }

            else if(islandSearchStatus == IslandSearchStatus.INITIAL_TURN){
                decision.put("action", "heading");
                drone.changeDirection("L"); 
                headingParams.put("direction", drone.getHeading()); 
                decision.put("parameters", headingParams);
                islandSearchStatus = IslandSearchStatus.ADJUSTING_TURN;
                return decision;
            }

            else if(islandSearchStatus == IslandSearchStatus.ADJUSTING_TURN){
                decision.put("action", "heading");
                drone.changeDirection("L"); 
                headingParams.put("direction", drone.getHeading()); 
                decision.put("parameters", headingParams);
                islandSearchStatus = IslandSearchStatus.FINAL_TURN;
                return decision;
            }

            else if(islandSearchStatus == IslandSearchStatus.FINAL_TURN){
                decision.put("action", "heading");
                drone.changeDirection("R"); 
                headingParams.put("direction", drone.getHeading()); 
                decision.put("parameters", headingParams);
                setDistanceToIsland(drone_radar.getRange()-3); //since it moved 3 times while setting up to fly towards the island
                islandSearchStatus = IslandSearchStatus.HEAD_TO_ISLAND;
                return decision;
            }

            else if(islandSearchStatus == IslandSearchStatus.HEAD_TO_ISLAND && distanceToIsland > 0){
                decision.put("action", "fly");
                drone.move();
                islandLogger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                islandLogger.debug(drone.getBattery());
                setDistanceToIsland(distanceToIsland-1);

                //if the range becomes 0 the island is found - toggle found island (triggers creek search in explorer)
                if(distanceToIsland == 0){
                    islandSearchStatus = IslandSearchStatus.FOUND;
                }

                return decision;
            }
        }
        else if(drone_radar.getFound().equalsIgnoreCase("OUT_OF_RANGE")){ //if drones radar returned out of range - fly forward (still facing south)
            decision.put("action", "fly");
            drone.move();
            islandLogger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
            islandLogger.debug(drone.getBattery());
            drone_radar.nothingFound(); //reset what was found so it will radar again
            return decision;
        }


        //use radar after flying if nothing is found and also to start
        decision.put("action", "echo");
        radarParams.put("direction", "E");
        decision.put("parameters", radarParams);
        islandLogger.info("Drone is scanning in direction: {}", radarParams);
        return decision;
    }

    //if the island 
    @Override
    public boolean foundIsland(){
        if(islandSearchStatus == IslandSearchStatus.FOUND){
            return true;
        }
        return false;
    }

    public void setDistanceToIsland(int distanceToIsland){
        this.distanceToIsland = distanceToIsland;
    }

    @Override
    public JSONObject initiateGroundSearch(){

        decision = new JSONObject();
        headingParams = new JSONObject();

        //changing the heading is costly, so we want to avoid doing this too much
        decision.put("action", "heading"); //change direction to south to start
        drone.changeDirection("R"); 
        headingParams.put("direction", drone.getHeading()); 
        decision.put("parameters", headingParams); //cant pass string in here must be JSON object - use wrapper JSON

        return decision;
    }
}