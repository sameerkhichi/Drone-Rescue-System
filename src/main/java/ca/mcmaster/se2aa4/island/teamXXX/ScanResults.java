package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;

//Class to store island details from drone exploration and island details
public class ScanResults {
    private ArrayList<double[]> creekLocations;
    private ArrayList<String> creekIDs;
    private double siteX;
    private double siteY;
    private boolean hasSite;

    public ScanResults() {
        this.creekLocations = new ArrayList<>();
        this.creekIDs = new ArrayList<>();
        this.siteX = -1; // Default values indicating no site
        this.siteY = -1;
        this.hasSite = false;
    }

    public void addCreek(double x, double y, String creekID) {
        creekLocations.add(new double[]{x, y});
        creekIDs.add(creekID);
        System.out.println("Added Creek: ID=" + creekID + " at (" + x + ", " + y + ")");
    }

    public void setSite(double x, double y) {
        this.siteX = x;
        this.siteY = y;
        this.hasSite = true;
        System.out.println("Site Set at: (" + x + ", " + y + ")");
    }

    public ArrayList<double[]> getCreekLocations() {
        return creekLocations;
    }

    public ArrayList<String> getCreekIDs() {
        return creekIDs;
    }

    public boolean hasSite() {
        return hasSite;
    }

    public double getSiteX() {
        return siteX;
    }

    public double getSiteY() {
        return siteY;
    }

    // NEW: Find the closest creek to the site
    public String getClosestCreek() {
        if (creekLocations.size() == 0) { //essentially if there were no creeks found after exploration
            return "No creeks were located";
        }
    
        double minDistance = Double.MAX_VALUE;
        int closestIndex = -1;
    
        for (int i = 0; i < creekLocations.size(); i++) {
            double[] coords = creekLocations.get(i);
            double x = coords[0], y = coords[1];
    
            double distance = Math.sqrt(Math.pow(x - siteX, 2) + Math.pow(y - siteY, 2));
    
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

    
    /* 
        if(!hasSite){ //if no site is found - return any creek
            return creekIDs.get(closestIndex);
        }
    
        if (closestIndex != -1) {
            return String.format("Closest Creek: ID=%s at (%.2f, %.2f), Distance=%.2f",
                    creekIDs.get(closestIndex), creekLocations.get(closestIndex)[0], creekLocations.get(closestIndex)[1], minDistance);
        }
    */
        //if it gets here atleast one creek was found
        return creekIDs.get(closestIndex);
    }
    

    
}

/* 
//These attributes should come form the Scan action
    //{x, y} of emergency site coordinates
    private double[] siteLocation;
    //{(distanceToSite, x, y), (distanceToSite, x, y), (distanceToSite, x, y), .....} of creek coordinates
    private double[][] creekLocations;
    

    public void storeSiteLocation(){


    }

*/