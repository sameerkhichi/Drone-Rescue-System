package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;

public class RadarTest {
    
    @Test
    public void updateRadarDataGroundTest() {
        Radar droneRadar = new Radar();
        JSONObject extras = new JSONObject();
        extras.put("range", 0);
        extras.put("found", "GROUND");
        droneRadar.updateRadarData(extras);
        assertTrue(droneRadar.getRange() == 0 && droneRadar.getFound().equals("GROUND"));
    }

    @Test
    public void updateRadarDataOutOfBoundsTest() {
        Radar droneRadar = new Radar();
        JSONObject extras = new JSONObject();
        extras.put("range", 0);
        extras.put("found", "OUT_OF_BOUNDS");
        droneRadar.updateRadarData(extras);
        assertTrue(droneRadar.getRange() == 0 && !droneRadar.hasGroundAhead());
    }
}
