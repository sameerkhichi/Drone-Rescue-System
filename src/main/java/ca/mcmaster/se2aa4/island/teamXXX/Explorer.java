
// grid is 53 x 52. 53 squares to east and 52 below. Drone starts at top left


package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject; 
import org.json.JSONTokener;

import eu.ace_design.island.bot.IExplorerRaid; //game engine

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private DroneState drone;
    private Radar drone_radar;

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
    }

    //use this method to call a specific request to the drone
    @Override
    public String takeDecision() {
        //create json objects for each action you would be doing
        JSONObject decision = new JSONObject();
        JSONObject headingParams = new JSONObject();
        JSONObject radarParams = new JSONObject();

        //if the battery is full that means it is the first action
        if(drone.getBattery() == drone.getStartingBatteryCapacity()){
            decision.put("action", "heading"); //change direction to south to start
            drone.changeDirection("R"); 
            headingParams.put("direction", drone.getHeading()); 
            decision.put("parameters", headingParams); //cant pass string in here must be JSON object - use wrapper JSON
        }
    
        if(drone.getBattery() > 0 && drone.getBattery() != drone.getStartingBatteryCapacity()){
            
            /*
             * Problem here:
             * as is the drone will fly down and radar if nothing is found
             * need to get the drone to turn back east and fly in that direction
             * when something is detected there.
             * This though just keeps flying down no matter what.
             */

            //if the radar found something then fly forward
            if(drone_radar.getFound().equalsIgnoreCase("GROUND")){
                decision.put("action", "fly"); //fly forward
                drone.move(); //this method is from the DroneState Class basically makes the drone move
                logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
                logger.debug(drone.getBattery());
            }

            else{
                // doing the radar part here:
                decision.put("action", "echo");
                radarParams.put("direction", "E"); // change the heading to scan on the left and right of the wings
                decision.put("parameters", radarParams);
                logger.info("Drone is scanning in direction: {}", radarParams);
            }

        }

        if(drone.getBattery() <= 0){ //battery dead
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
        logger.info("Additional information received: {}", extraInfo);

        //deplete the drone battery by the cost
        drone.updateBatteryLife(cost); 

        if(!extraInfo.isEmpty()){
            //updating the range and found (GROUND/OUT OF RANGE) in radar 
            drone_radar.updateRadarData(extraInfo);
        }
        else{
            drone_radar.nothingFound();
        }

    }

    @Override
    public String deliverFinalReport() { //doesnt give anything rn - to update later 
        return "no creek found";
    }

}
