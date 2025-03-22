package ca.mcmaster.se2aa4.island.teamXXX;
import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoScanner {
    private JSONArray biomes;
    private JSONArray creeks;
    private JSONArray sites;
    private boolean scannedTile;
    private DroneState drone;
    private ScanResults scanResults;


    public PhotoScanner(DroneState drone, ScanResults scanResults) {
        this.drone = drone;
        this.scanResults = scanResults;
        this.biomes = null;
        this.creeks = null;
        this.sites = null;
    }

    public void updateScanData(JSONObject extraInfo) {
        biomes = extraInfo.getJSONArray("biomes");
        creeks = extraInfo.getJSONArray("creeks");
        sites = extraInfo.getJSONArray("sites");
        scannedTile = true;
        updateCreekCoordinatesAndID();
        updateSiteCoordinates();
    }

    public void updateCreekCoordinatesAndID(){
        if (creeks != null && creeks.length() > 0) {
            double creekX = drone.getX();
            double creekY = drone.getY();
            String creekID = creeks.getString(0); // Assuming first creek

            // Store in ScanResults
            scanResults.addCreek(creekX, creekY, creekID);
        }
    }

    // Checks if Ocean is one of the biomes
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

    // Checks if the only biome is Ocean
    public boolean hasOceanOnly() {
        if ((biomes.length() == 1) && (biomes.getString(0).equals("OCEAN"))) {
            return true;
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
        if(this.biomes == null){
            return false;
        }
        else{
            return ((biomes.length() > 1) && hasOcean());
        }
    }

    //helper - forces the biomes to show up as empty
    public void forceBiomesEmpty(){
        this.biomes = null;
    }

    // Used to check if the current tile has already been scanned
    public boolean isTileScanned() {
        return scannedTile;
    }

    // Resets the scan status of the tile
    public void resetScannedTile() {
        scannedTile = false;
    }
    

    public void updateSiteCoordinates(){
        if (sites != null && sites.length() > 0) {
            double siteX = drone.getX();
            double siteY = drone.getY();

            // Store in ScanResults
            scanResults.setSite(siteX, siteY);
        }
    }
}