package Krampus;

import robocode.*;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Krampus - a robot by Onur Orkut Genisel
 */
public class Krampus extends AdvancedRobot {

	/**
	 * run: Krampus's default behavior
	 */
    @Override
    public void run() {
        setColors(Color.black, Color.black, Color.red); // body, gun, radar
		
        setAdjustGunForRobotTurn(true);

        // Robot main loop
        while (true) {
            setTurnRadarRight(360);
            execute();
        }
    }

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {

        setTurnRight(e.getBearing());

        double gunTurn = getHeading() + e.getBearing() - getGunHeading();
        setTurnGunRight(gunTurn);
 
        if (Math.abs(gunTurn) < 5 && getGunHeat() == 0 && getEnergy() > 1) {
            setFire(3);
        }

        setAhead(e.getDistance() + 5);
     }

	/**
	 * onHitRobot: What to do when you hit your opponent
	 */
    @Override
    public void onHitRobot(HitRobotEvent e) {
        setTurnRight(e.getBearing());
        if (getGunHeat() == 0 && getEnergy() > 1) {
            setFire(3);
        }
        setAhead(40);
    }

	/**
	 * onHitWall: What to do when you hit a wall
	 */
    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(60);
        setTurnRight(45);
        execute();
    }
}
