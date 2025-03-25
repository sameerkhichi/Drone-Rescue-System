package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;

public class IslandSearchTest {
    
    @Test
    public void noGroundFoundTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        Radar droneRadar = new Radar();

        JSONObject extras = new JSONObject();
        extras.put("range", 0);
        extras.put("found", "OUT_OF_RANGE");
        droneRadar.updateRadarData(extras);

        IslandSearch islandSearch = new IslandSearch(drone, droneRadar);
        JSONObject action = islandSearch.getNextMove();

        assertTrue(action.getString("action").equals("fly"));
    }

    @Test
    public void echoTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        Radar droneRadar = new Radar();

        JSONObject extras = new JSONObject();
        extras.put("range", 0);
        extras.put("found", "");
        droneRadar.updateRadarData(extras);

        IslandSearch islandSearch = new IslandSearch(drone, droneRadar);
        JSONObject action = islandSearch.getNextMove();

        assertTrue(action.getString("action").equals("echo"));
    }

    @Test
    public void setupForTurnTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        Radar droneRadar = new Radar();

        JSONObject extras = new JSONObject();
        extras.put("range", 0);
        extras.put("found", "GROUND");
        droneRadar.updateRadarData(extras);

        IslandSearch islandSearch = new IslandSearch(drone, droneRadar);
        JSONObject action = islandSearch.getNextMove();

        assertTrue(action.getString("action").equals("fly"));
    }

}
