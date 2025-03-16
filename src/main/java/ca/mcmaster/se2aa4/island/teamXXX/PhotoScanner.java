package ca.mcmaster.se2aa4.island.teamXXX;
import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoScanner {
    private JSONArray biomes;
    private JSONArray creeks;
    private JSONArray sites;

    public PhotoScanner() {
        this.biomes = null;
        this.creeks = null;
        this.sites = null;
    }

    public void updateScanData(JSONObject extraInfo) {
        biomes = extraInfo.getJSONArray("biomes");
        creeks = extraInfo.getJSONArray("creeks");
        sites = extraInfo.getJSONArray("sites");
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

    public boolean hasSite(){
        return (sites!=null);
    }

    //check for if theres more than one biome in the array returned
    public boolean endOfIsland(){
        return ((biomes.length() > 1) && hasOcean());
    }

    //helper - forces the biomes to show up as empty
    public void forceBiomesEmpty(){
        this.biomes = null;
    }

}