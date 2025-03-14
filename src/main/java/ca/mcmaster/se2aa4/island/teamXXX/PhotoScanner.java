package ca.mcmaster.se2aa4.island.teamXXX;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoScanner {
    private JSONArray biomes;
    private JSONArray creeks;

    public PhotoScanner() {
        this.biomes = null;
        this.creeks = null;
    }

    public void updateScanData(JSONObject extraInfo) {
        biomes = extraInfo.getJSONArray("biomes");
        creeks = extraInfo.getJSONArray("creeks");
    }

    public boolean hasOcean() {
        if (biomes == null) {
            return false;
        }
        for (int i = 0; i < biomes.length(); i++) {
            if (biomes.getString(i).equals("OCEAN")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCreek() {
        return (creeks!=null);
    }
}