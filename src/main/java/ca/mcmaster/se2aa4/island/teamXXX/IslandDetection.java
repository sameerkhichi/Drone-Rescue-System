package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

//any island searching algorithm should implement this and search strategy
public interface IslandDetection {
    JSONObject initiateGroundSearch();
    boolean foundIsland();
    JSONObject getNextMove();
}