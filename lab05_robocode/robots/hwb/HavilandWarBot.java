// Author: Ethan Haviland

package hwb;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class HavilandWarBot extends Robot {

    boolean peek;
    double moveAmount;

    public void run() {

        // Color setup
        setBodyColor(Color.blue);
        setGunColor(Color.orange);
        setRadarColor(Color.orange);
        setBulletColor(Color.orange);
        setScanColor(Color.blue);
		// Move towards the wall and turn right
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        peek = false;
        turnLeft(getHeading() % 90);
        ahead(moveAmount);
        peek = true;
        turnGunRight(90);
        turnRight(90);
        while (true) {
            peek = true;
            ahead(moveAmount);
            peek = false;
			// Ensure robot hugs wall if it hits it at an angle
			double heading = getHeading();
			double remainder = heading % 90;
			if (remainder == 0){
				turnRight(90);
			} else {
				turnRight(90-remainder);
			}
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
		// Fire on the scanned robot with different powers
		double distance = e.getDistance();
		double power = 1;
		if (distance < 100) power = 3;
		else if (distance < 500) power = 2;
		fire(power);
		if(peek) scan();
        
    }


    public void onHitByBullet(HitByBulletEvent e) {
		// When hit turn to the Right at a random angle
		back(50);
		double turnAmount = 45 + Math.random() * 90;
		turnRight(turnAmount);
		ahead(100);
    }


    public void onHitRobot(HitRobotEvent e) {
        // Turn gun towards the enemy and fire power 3.
        if (e.getBearing() > -90 && e.getBearing() < 90) {
            turnGunLeft(90 - e.getBearing());
			fire(3);
			turnGunRight(90 - e.getBearing());
        } else {
            turnGunRight(e.getBearing() - 90);
			fire(3);
			turnGunLeft(e.getBearing() - 90);
        }
    }
}
