package ApocalypseTank;

import robocode.*;
import robocode.util.Utils;    // Needed for radar tracking
import java.awt.Color;         // Needed for color change
import java.awt.geom.*;        // Needed for anti-gravity movement

public class Apocalypse extends AdvancedRobot {

    private int moveDirection = 1;   // Used for flipping direction when hit/wall

    // Anti-gravity: store enemy positions
    private static final int MAX_ENEMIES = 15;
    static Point2D.Double[] enemyPoints = new Point2D.Double[MAX_ENEMIES];
    int enemyIndex = 0;

    public void run() {
        // Colors
        setColors(Color.black, Color.black, Color.red); // body, gun, radar

        // Make radar and gun independent of body turns
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Start radar sweeping
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        // Keep radar active and execute commands
        while (true) {
            if (getRadarTurnRemaining() == 0.0) {
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double myHeading = getHeadingRadians();
        double enemyBearing = e.getBearingRadians();
        double enemyAbsolute = myHeading + enemyBearing;
        double distance = e.getDistance();

        // Radar 
        double angleToEnemy = enemyAbsolute;
        double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        double extraTurn = Math.min(Math.atan(36.0 / distance), Rules.RADAR_TURN_RATE_RADIANS);

        if (radarTurn < 0) {
            radarTurn -= extraTurn;
        } else {
            radarTurn += extraTurn;
        }

        setTurnRadarRightRadians(radarTurn);

        // Compute repulsion from enemy positions
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        double enemyX = getX() + e.getDistance() * Math.sin(absBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absBearing);
		
		 // For detecting enemy is hugging a wall
		 double bfWidth = getBattleFieldWidth();
		 double bfHeight = getBattleFieldHeight();
		 
		 // Distance from wall
		 double wallMargin = 60; 					  
		 
		 boolean enemyNearWall = enemyX < wallMargin || 
				  bfWidth - enemyX < wallMargin || 
				  enemyY < wallMargin || 
				  bfHeight - enemyY < wallMargin;

		 // Change behavior if encountering a wall hugging enemy
			 boolean followMode = enemyNearWall && distance > 120 && distance < 450;
			 
        // Store enemy position(s)
        enemyPoints[enemyIndex] = new Point2D.Double(enemyX, enemyY);
        enemyIndex++;
        if (enemyIndex >= MAX_ENEMIES) {
            enemyIndex = 0;
        }

        // Compute force for moving away from enemy
        double xForce = 0;
        double yForce = 0;

        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (enemyPoints[i] == null) continue;

            double ex = enemyPoints[i].x;
            double ey = enemyPoints[i].y;

            double dx = ex - getX();
            double dy = ey - getY();

            double enemyAngle = Utils.normalAbsoluteAngle(Math.atan2(dx, dy));
            double d = enemyPoints[i].distance(getX(), getY());

            // Repulsive force
            if (d > 0) {
                xForce -= Math.sin(enemyAngle) / (d * d);
                yForce -= Math.cos(enemyAngle) / (d * d);
            }
        }

        // Wall avoidance
		double margin = 80;   // Adjust how close 
		double x = getX();
		double y = getY();
		
		// Left wall
		if (x < margin) {
			xForce += 20000 / ((x+1) * (x + 1));
		}
		// Right wall
		if (bfWidth - x < margin) {
			xForce -= 20000 / ((bfWidth - x + 1) * (bfWidth - x + 1));
		}
		// Bottom wall
		if (y < margin) {
			yForce += 20000 / ((y + 1) * (y + 1));
		}
		// Top wall
		if (bfHeight - y < margin) {
			yForce -= 20000 / ((bfHeight - y + 1) * (bfHeight - y + 1));
		}
		
        // Movement: Anti-G and Lateral. Follow mode engaged for wall huggers.
        if (xForce != 0 || yForce != 0) {
            boolean nearWall = x < margin || bfWidth - x < margin || y < margin || bfHeight - y < margin;
			 
			 double targetAngle;
			 
			 if (nearWall && !followMode) {
				 // Head toward the center when too close to walls
				 double centerX = bfWidth / 2.0;
				 double centerY = bfHeight / 2.0;
				 targetAngle = Math.atan2(centerX - x, centerY - y);
				 setAhead(180);
			 } else if (followMode) { // Specific behavior against wall huggers...FOLLOW THEM!
			 	 double enemyHeading = e.getHeadingRadians();
				 double followDist = 60; // Distance when following
				 double behindX = enemyX - Math.sin(enemyHeading) * followDist;
				 double behindY = enemyY - Math.cos(enemyHeading) * followDist;
				 double centerX = bfWidth / 2.0;
				 double centerY = bfHeight / 2.0;
				 double inwardFactor = 0.15;
				 double offsetX = behindX + (centerX - behindX) * inwardFactor;
				 double offsetY = behindY + (centerY - behindY) * inwardFactor;
				 
				 targetAngle = Math.atan2(offsetX - x, offsetY - y);
				 setAhead(200); 
			 } else {
				 double gravAngle = Math.atan2(xForce, yForce);							 					 // Anti-gravity movement
            	 double lateralAngle = enemyAbsolute + moveDirection * (Math.PI / 2);  					 // Lateral movement
				 double blendedAngle = Utils.normalAbsoluteAngle(0.7 * gravAngle + 0.3 * lateralAngle); // Combine Anti-G and lateral angles
				 targetAngle = blendedAngle;
				 setAhead(180); 
			 }

			 double turnAngle = Utils.normalRelativeAngle(targetAngle - getHeadingRadians());
			 setTurnRightRadians(turnAngle);
        }

        // Aim at enemy
        double gunTurn = Utils.normalRelativeAngle(enemyAbsolute - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);

        // Determine power based on distance
		 double myEnergy = getEnergy();		
		
		 if (distance < 400) {
    		 setFire(3.0);
		 } else if (distance < 1200) {
    		 setFire(1.5);
		 } else {
		     setFire(0.8);
		 }
		 
		 // Change firing behavior if follow mode engaged
		 if (followMode) {
			 if (distance < 100) {
    		 	 setFire(3);
		 	 } else {
				 setFire(0.1);
			 }
		 }

		 // Power Conservation
		 if (myEnergy < 20) {
			 setFire(0.5);
		 }	 
    }
	
	// Hit Reaction
    public void onHitByBullet(HitByBulletEvent e) {
        moveDirection *= -1;
    }

	// Hit wall reaction
    public void onHitWall(HitWallEvent e) {
        moveDirection *= -1;
    }
}
