package GG;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html
//advnaced robot methods - https://robocode.sourceforge.io/docs/robocode/

/**
 * GoohsGobstopper - a robot by Jon Goohs. My thought is that the top means for scoring (on the aggregate points scale) is survival and bullet damage, so I aimed to make a rapid-fire marksman.
 */
public class GoohsGobstopper extends AdvancedRobot
{
	private double survivalRate = 1.0;
	private int roundsWon = 0; //storing for in between round improved performance
	private double aggressiveness = 1.0; //0.5 for defensive, 1.5 for aggressive
	private double movementDistance = 150; //adjust based on survival and performance scoring
	private double targetBearing = 0; //hold last target bearing
    private long lastScanTime = 0;
	private int moveDir = 1;
	/**
	 * run: GoohsGobstopper's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		setColors(Color.red,Color.white,Color.green); // body,gun,radar
		setTurnRadarRight(Double.POSITIVE_INFINITY); //always spin radar
		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			setAhead(movementDistance * moveDir);
			execute(); //perform above
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		//A bullet travels at a speed between 11.0 and 19.7 depending on the power.
		//The more powerful the bullet, the slower. The formula to calculate it is velocity = 20 - (3 * power).
		double bearing = e.getBearing();
    	targetBearing = bearing;
    	lastScanTime = getTime();

		double absoluteBearing = getHeading() + e.getBearing();
		double gunTurn = Utils.normalRelativeAngleDegrees(
            absoluteBearing - getGunHeading()
    	);
    	setTurnGunRight(gunTurn);
		double radarTurn = Utils.normalRelativeAngleDegrees(
			absoluteBearing - getRadarHeading()
		);
		setTurnGunRight(radarTurn * 2); //turn gun to face target
		//use power based on distance from scanned robot to conserve if it dodges
        double setPower = (e.getDistance() < 150) ? (3.0 * aggressiveness) : ((e.getDistance() < 300) ? (2.0 * aggressiveness) : (1.0 * aggressiveness));
		setPower = Math.min(3.0, Math.max(0.1, setPower));
		if (getEnergy() > 0.5) {
		    fire(setPower);
		}	

	    setTurnRight(bearing + 90 - 30 * moveDir); //add some randomness to moving, perpen to enemy
		setAhead(120*moveDir);
		if (Math.random() < 0.08) {
			moveDir = -moveDir; //flip direction to increase unpredictability
		}
		execute();
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		//even though the crazy robot didnt hit a lot, its ability to move a lot made it almost always a survivor for long
		//dodging is important
		double bulletBearing = e.getBearing();
		if (Math.abs(bulletBearing) < 90) {
			setBack(75);
			setTurnLeft(bulletBearing + 90); //turn 90 degrees away from shot
		} else {
			setAhead(75);
			setTurnRight(bulletBearing - 90);
		}
		execute();
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		moveDir = -moveDir;

		double wallBearing = e.getBearing();
		if (wallBearing > -45 && wallBearing < 45) {
            // Hit right wall
            setTurnRight(135);
		} else if (wallBearing > 45 && wallBearing < 135) {
            // Hit bottom wall
            setTurnRight(-90);
		} else if (wallBearing > 135 || wallBearing < -135) {
            // Hit left wall
            setTurnRight(-135);
		} else {
            // Hit top wall
            setTurnRight(90);
		}

		setAhead(movementDistance);
		execute();
	}

	public void onHitRobot(HitRobotEvent e) {
		setTurnGunRight(e.getBearing());
		fire(3.0);
		setBack(40);
		setTurnRight(45);
		execute();
	}

	public void onRoundEnded(RoundEndedEvent e) {
        //Analyze this round's performance
        boolean survived = getEnergy() > 0;
        
        if (survived) {
            survivalRate = Math.min(1.0, survivalRate + 0.1);
            aggressiveness = Math.min(1.5, aggressiveness + 0.1); //more aggressive if surviving
            movementDistance = Math.min(200, movementDistance + 10); // move more for more survivability hopefully
        } else {
            survivalRate *= 0.9;
            aggressiveness = Math.max(0.5, aggressiveness - 0.2); //less aggressive if dying 
            movementDistance = Math.max(100, movementDistance - 20); //too exposed so move less
        }
		execute();
        
        out.println("Round " + e.getRound() + " - Survived: " + survived + 
            " | Aggression: " + aggressiveness + 
            " | Movement: " + movementDistance);
    }
}
