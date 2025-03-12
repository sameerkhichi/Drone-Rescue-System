package ca.mcmaster.se2aa4.island.teamXXX;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Radar {
    private final Logger logger = LogManager.getLogger();
    private int range;
    private String found;

    public Radar(){
        this.range = -1;
        this.found = "Unknown";
    }

    public void updateRadarData(JSONObject extras){
        this.range = extras.getInt("range");
        this.found = extras.getString("found");
        logger.info("Radar scan results: {} found at range {}", found, range);
    }
    
    public int getRange(){
        return range;
    }

    public String getFound(){
        return found;
    }

}
