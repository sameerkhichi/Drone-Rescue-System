package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoScannerTest {
    
    @Test
    public void hasOceanTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        ScanResults scanResults = new ScanResults();
        PhotoScanner scanner = new PhotoScanner(drone, scanResults);
        JSONObject extras = new JSONObject();
        JSONArray biomes = new JSONArray();
        JSONArray creeks = new JSONArray();
        JSONArray sites = new JSONArray();

        biomes.put("OCEAN");
        biomes.put("MANGROVE");
        extras.put("biomes", biomes);

        creeks.put("");
        extras.put("creeks", creeks);

        sites.put("");
        extras.put("sites", sites);

        scanner.updateScanData(extras);
        
        assertTrue(scanner.hasOcean());
    }

    @Test
    public void hasOceanOnlyTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        ScanResults scanResults = new ScanResults();
        PhotoScanner scanner = new PhotoScanner(drone, scanResults);
        JSONObject extras = new JSONObject();
        JSONArray biomes = new JSONArray();
        JSONArray creeks = new JSONArray();
        JSONArray sites = new JSONArray();

        biomes.put("OCEAN");
        biomes.put("MANGROVE");
        extras.put("biomes", biomes);

        creeks.put("");
        extras.put("creeks", creeks);

        sites.put("");
        extras.put("sites", sites);

        scanner.updateScanData(extras);
        
        assertFalse(scanner.hasOceanOnly());
    }

    @Test
    public void hasSiteTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        ScanResults scanResults = new ScanResults();
        PhotoScanner scanner = new PhotoScanner(drone, scanResults);
        JSONObject extras = new JSONObject();
        JSONArray biomes = new JSONArray();
        JSONArray creeks = new JSONArray();
        JSONArray sites = new JSONArray();

        biomes.put("MANGROVE");
        extras.put("biomes", biomes);

        creeks.put("");
        extras.put("creeks", creeks);

        sites.put("SAMPLE");
        extras.put("sites", sites);

        scanner.updateScanData(extras);
        
        assertTrue(scanner.hasSite());
    }

    @Test
    public void hasCreekTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        ScanResults scanResults = new ScanResults();
        PhotoScanner scanner = new PhotoScanner(drone, scanResults);
        JSONObject extras = new JSONObject();
        JSONArray biomes = new JSONArray();
        JSONArray creeks = new JSONArray();
        JSONArray sites = new JSONArray();

        biomes.put("MANGROVE");
        extras.put("biomes", biomes);

        creeks.put("SAMPLE");
        extras.put("creeks", creeks);

        sites.put("");
        extras.put("sites", sites);

        scanner.updateScanData(extras);
        
        assertTrue(scanner.hasCreek());
    }
}
