package ca.mcmaster.se2aa4.island.teamXXX;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;


public class DroneState {
    
    private final Logger droneLogger = LogManager.getLogger();
    
    //private attributes of the drone when initialized
    private int x;
    private int y;
    private String heading;
    private int battery;


    //constructor for the initialization of the attributes for the drone
    public DroneState(int startX, int startY, String startHeading, int startBatteryLevel){
        this.battery = startBatteryLevel;
        this.heading = startHeading;
        this.x = startX;
        this.y = startY;
    }

    public void move(){
        droneLogger.debug("Moving from ({}, {}) facing {}", x, y, heading);

        //different moves the drone could take following traditional cardinal system
        if(heading.equals("N")){
            y++;
        } 
        else if(heading.equals("S")){
            y--;
        } 
        else if(heading.equals("E")){
            x++;
        } 
        else if(heading.equals("W")){
            x--;
        }

        droneLogger.debug("Moved to ({}, {})", x, y, heading);
    }

    //updating the direction through hard code right now
    public void changeDirection(String nextDirection){
        droneLogger.info("Turning from facing {}, to {}", heading, nextDirection);
        this.heading = nextDirection;
    }

    //updates the heading to where it is now facing
    public void updateHeading(String newHeading){
        this.heading = newHeading;
    }
    
    //gonna call this to reduce the battery based on the cost of each action from acknowledgeResults
    public void updateBatteryLife(int amount){
        this.battery = getBattery() - amount;
    }

    //getters for the private attributes of the drone
    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public String getHeading(){
        return heading;
    }

    public int getBattery(){
        return battery;
    }

}
