package ca.mcmaster.se2aa4.island.teamXXX;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


//we should turn drone state into an interface with the different actions the drone can have
//the We should also split move and changedirection methods into classes to adhere to SRP
public class DroneState {
    
    private final Logger droneLogger = LogManager.getLogger();
    
    //private attributes of the drone when initialized
    private int x;
    private int y;
    private String heading;
    private int battery;
    private int startLevel;
    private boolean turningTowardsLand = false;


    //constructor for the initialization of the attributes for the drone
    public DroneState(int startX, int startY, String startHeading, int startBatteryLevel){
        this.battery = startBatteryLevel;
        this.heading = startHeading;
        this.x = startX;
        this.y = startY;
        setStartingBatteryCapacity(startBatteryLevel);
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
    //the plane can only turn left or right ill use L or R
    public void changeDirection(String nextDirection){
        
        String[] directions = {"N", "E", "S", "W"};
        int directionIndex;
        String prevHeading = heading;

        //optimize this later, you dont need to check each time you change direction
        //you should store the directionIndex for later use
        for(directionIndex = 0; directionIndex < directions.length; directionIndex++){
            if(getHeading().equalsIgnoreCase(directions[directionIndex])){
                break;
            }
        }

        //this will set the direction to the right value if it turns left or right from where it currently is
        //remember the drone moves forward before turning and also once after. - refer to assignment picture
        if(nextDirection.equalsIgnoreCase("L")){
            move();
            updateHeading(directions[((directionIndex+3)%4)]);
            move();
        }
        else if(nextDirection.equalsIgnoreCase("R")){
            move(); 
            updateHeading(directions[((directionIndex+1)%4)]);
            move();
        }
        
        droneLogger.info("Turning from facing {}, to {}", prevHeading, getHeading());
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

    public int getStartingBatteryCapacity(){
        return startLevel;
    }

    public void setStartingBatteryCapacity(int startLevel){
        this.startLevel = startLevel;
    }

    public boolean getTurningStatus(){
        return turningTowardsLand;
    }

    public void setTurningStatus(boolean turningTowardsLand){
        this.turningTowardsLand = turningTowardsLand;
    }

}