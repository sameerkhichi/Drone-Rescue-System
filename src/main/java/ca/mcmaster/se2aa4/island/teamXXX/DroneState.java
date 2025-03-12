package ca.mcmaster.se2aa4.island.teamXXX;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
        droneLogger.info("Moving from ({}, {}) facing {}", x, y, heading);

        //different moves the drone could take following traditional cardinal system
        if(heading.equals("NORTH")){
            y++;
        } 
        else if(heading.equals("SOUTH")){
            y--;
        } 
        else if(heading.equals("EAST")){
            x++;
        } 
        else if(heading.equals("WEST")){
            x--;
        }

        droneLogger.info("Moved to ({}, {})", x, y);
        battery--;
    }

    //updates the heading to where it is now facing
    public void updateHeading(String newHeading){
        this.heading = newHeading;
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
