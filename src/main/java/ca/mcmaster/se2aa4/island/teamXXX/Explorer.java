package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private DroneState drone;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);

        //starting at 0,0 for now 
        drone = new DroneState(0, 0, direction, batteryLevel);
        logger.info("Drone initialized at ({}, {}), facing {}, with battery {}", 0, 0, direction, batteryLevel);


    }

    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        
        if(drone.getBattery() > 0){
            //drone movement is handled by the 
            decision.put("action", "move");
            decision.put("parameters", drone.getHeading());
            drone.move(); //this method is from the DroneState Class basically makes the drone move
        }
        else{
            decision.put("action", "stop"); // we stop the exploration immediately
        }
        
        logger.info("** Decision: {}",decision.toString());
        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n"+response.toString(2));
        Integer cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);
        String status = response.getString("status");
        logger.info("The status of the drone is {}", status);
        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}", extraInfo);
    }

    @Override
    public String deliverFinalReport() { //doesnt give anything rn - to update later 
        return "no creek found";
    }

}
