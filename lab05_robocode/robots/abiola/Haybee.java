package abiola;
import robocode.*;
import java.awt.Color;

/**
 * Haybee - a robot by Abiola Bamgbose
 */
public class Haybee extends Robot {

    public void run() {
        // Initialization
        setColors(Color.red, Color.blue, Color.green);

        // Main loop using only API methods
        while (true) {
            ahead(100);          // move forward
            turnGunRight(360);   // scan
            back(100);           // move backward
            turnGunRight(360);   // scan again
            turnRight(45);       // change direction
        }
    }

    /**
     * onScannedRobot: When my radar sees another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double distance = e.getDistance();
        if (distance < 200) {
            fire(3);   // strong shot if close
        } else {
            fire(1);   // weaker shot if far
        }
    }

    /**
     * onHitByBullet: When it is hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Dodge perpendicular
        turnRight(90 - e.getBearing());
        ahead(100);
    }

    /**
     * onHitWall: When it collides with a wall
     */
    public void onHitWall(HitWallEvent e) {
        back(50);
        turnRight(90);
    }

    /**
     * onHitRobot: When it bumps into another robot
     */
    public void onHitRobot(HitRobotEvent e) {
        fire(2);       // fire at close range
        back(50);      // retreat
        turnRight(30); // turn away
    }
}
