package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

//any searching algorithm used to navigate the island will implement this
public interface SearchStrategy {
    JSONObject getNextMove();
}
