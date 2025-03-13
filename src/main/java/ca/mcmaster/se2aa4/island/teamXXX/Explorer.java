
// grid is 53 x 52. 53 squares to east and 52 below. Drone starts at top left


package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject; //game engine
import org.json.JSONTokener;

import eu.ace_design.island.bot.IExplorerRaid;

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
        JSONObject decision = new JSONObject();
        JSONObject params = new JSONObject();


        
        //this will just loop till x=53 for now - will deal with movement decisions later
        if(drone.getBattery() > 0){
            //drone movement is handled by the 
            

            //decision.put("action", "fly"); //fly forward
            //decision.put("action", "heading"); //change direction
            //params.put("parameters", drone.getHeading());
            drone.move(); //this method is from the DroneState Class basically makes the drone move
            drone.changeDirection("L"); //testing by turning right
            logger.info("Drone is located at x: {}, y: {}", drone.getX(), drone.getY());
            logger.debug(drone.getBattery());

            // doing the radar part here:
            decision.put("action", "echo");
            params.put("direction", "S"); // ???
            decision.put("parameters", params);
            logger.info("Drone is scanning in direction: {}", drone.getHeading());

            }
        else{ //battery dead
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

        drone.updateBatteryLife(cost); //deplete the drone battery by the cost

        //updating the range and found (GROUND/OUT OF RANGE) in radar 
        drone_radar.updateRadarData(extraInfo);
    }

    @Override
    public String deliverFinalReport() { //doesnt give anything rn - to update later 
        return "no creek found";
    }

}
