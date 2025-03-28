package ca.mcmaster.se2aa4.island.teamXXX;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Radar {
    private final Logger logger = LogManager.getLogger();
    private int range;
    private String found;
    private boolean echoUsed;

    public Radar(){
        this.range = -1;
        this.found = "Unknown";
        this.echoUsed = false;
    }

    public void updateRadarData(JSONObject extras){
        this.range = extras.getInt("range");
        this.found = extras.getString("found");
        logger.debug("Radar scan results: {} found at range {}", found, range);
        this.echoUsed = true;
    }
    
    public int getRange(){
        return range;
    }

    public String getFound(){
        return found;
    }

    //used to set found to null when extras are not found
    public void nothingFound(){
        this.found = "";
    }

    public void resetRange() {
        this.range = -1;
    }

    public boolean hasGroundAhead() {
        if (found.equalsIgnoreCase("GROUND")) {
            return true;
        }
        return false;
    }

    public boolean echoLastUsed() {
        return echoUsed;
    }

    public void resetEchoUsage() {
        echoUsed = false;
    }
}
