package ca.mcmaster.se2aa4.island.teamXXX;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DroneStateTest {

    @Test
    public void moveEastTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        drone.move();
        assertTrue(drone.getX() == 1 && drone.getY() == 0);
    }

    @Test
    public void moveWestTest() {
        DroneState drone = new DroneState(0, 0, "W", 7000);
        drone.move();
        assertTrue(drone.getX() == -1 && drone.getY() == 0);
    }

    @Test
    public void moveNorthTest() {
        DroneState drone = new DroneState(0, 0, "N", 7000);
        drone.move();
        assertTrue(drone.getX() == 0 && drone.getY() == 1);
    }

    @Test
    public void moveSouthTest() {
        DroneState drone = new DroneState(0, 0, "S", 7000);
        drone.move();
        assertTrue(drone.getX() == 0 && drone.getY() == -1);
    }

    @Test
    public void turnLeftTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        drone.changeDirection("L");
        assertTrue(drone.getX() == 1 && drone.getY() == 1);
    }

    @Test
    public void turnRightTest() {
        DroneState drone = new DroneState(0, 0, "E", 7000);
        drone.changeDirection("R");
        assertTrue(drone.getX() == 1 && drone.getY() == -1);
    }
}