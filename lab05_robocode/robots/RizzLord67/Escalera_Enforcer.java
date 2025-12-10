package RizzLord67;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.HashMap;


public class Escalera_Enforcer extends AdvancedRobot
{
    // Movement
    private double moveAngle = 0;
    
    // Adaptive targeting - tracks enemy movement patterns
    private HashMap<String, int[]> targetingData = new HashMap<>();
    private static final int GUESS_FACTORS = 31;
    
    // Enemy info
    private double enemyX;
    private double enemyY;
    private double enemyVelocity;
    private double enemyHeading;
    
    // Rage mode
    private boolean enraged = false;
    
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        while(true) {
            if (enraged) {
                // === RAGE MODE - FULL RED DEMON ===
                int pulse = (int)(Math.abs(Math.sin(getTime() / 3.0)) * 55) + 200;
                setBodyColor(new Color(pulse, 0, 0));
                setGunColor(new Color(255, 0, 0));
                setRadarColor(new Color(255, 50, 50));
                setBulletColor(new Color(255, 100, 0));
                setScanColor(new Color(255, 0, 0));
            } else {
                // === NORMAL MODE - BLACK & GOLD ===
                setBodyColor(Color.black);
                setGunColor(new Color(255, 215, 0));      // Gold gun
                setRadarColor(new Color(255, 215, 0));    // Gold radar
                setBulletColor(new Color(255, 215, 0));   // Gold bullets
                setScanColor(new Color(255, 0, 0));       // Red scan arc
            }
            
            turnRadarRight(360);
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        String enemyName = e.getName();
        double enemyDistance = e.getDistance();
        double enemyBearing = e.getBearing();
        enemyHeading = e.getHeading();
        enemyVelocity = e.getVelocity();
        
        // Calculate enemy absolute position
        double absoluteBearing = Math.toRadians(getHeading() + enemyBearing);
        enemyX = getX() + enemyDistance * Math.sin(absoluteBearing);
        enemyY = getY() + enemyDistance * Math.cos(absoluteBearing);
        
        // === RADAR LOCK ===
        double radarTurn = Utils.normalRelativeAngleDegrees(
            getHeading() + enemyBearing - getRadarHeading());
        setTurnRadarRight(radarTurn * 2);
        
        // === ANTI-GRAVITY MOVEMENT ===
        doAntiGravityMovement(enemyX, enemyY, enemyDistance);
        
        // === ADAPTIVE TARGETING ===
        doAdaptiveTargeting(e, enemyName, absoluteBearing);
        
        execute();
    }
    
    private void doAntiGravityMovement(double enemyX, double enemyY, double enemyDistance) {
        double forceX = 0;
        double forceY = 0;
        
        // Repulsion from enemy - stay at medium range
        double optimalDistance = 250;
        double enemyForce = (optimalDistance - enemyDistance) / 50;
        double angleToEnemy = Math.atan2(enemyX - getX(), enemyY - getY());
        forceX -= enemyForce * Math.sin(angleToEnemy);
        forceY -= enemyForce * Math.cos(angleToEnemy);
        
        // Add perpendicular movement component for strafing
        double strafeAngle = angleToEnemy + Math.PI / 2;
        forceX += 3 * Math.sin(strafeAngle);
        forceY += 3 * Math.cos(strafeAngle);
        
        // Repulsion from walls
        double wallForce = 800;
        forceX += wallForce / Math.pow(getX(), 2);
        forceX -= wallForce / Math.pow(getBattleFieldWidth() - getX(), 2);
        forceY += wallForce / Math.pow(getY(), 2);
        forceY -= wallForce / Math.pow(getBattleFieldHeight() - getY(), 2);
        
        // Calculate movement angle from combined forces
        moveAngle = Math.toDegrees(Math.atan2(forceX, forceY));
        
        // Turn and move
        double turnAngle = Utils.normalRelativeAngleDegrees(moveAngle - getHeading());
        
        if (Math.abs(turnAngle) > 90) {
            setTurnRight(Utils.normalRelativeAngleDegrees(turnAngle + 180));
            setBack(100);
        } else {
            setTurnRight(turnAngle);
            setAhead(100);
        }
    }
    
    private void doAdaptiveTargeting(ScannedRobotEvent e, String enemyName, double absoluteBearing) {
        double enemyDistance = e.getDistance();
        
        // Initialize tracking array for new enemies
        if (!targetingData.containsKey(enemyName)) {
            targetingData.put(enemyName, new int[GUESS_FACTORS]);
        }
        
        int[] guessFactors = targetingData.get(enemyName);
        
        // Calculate fire power and bullet speed
        double bulletPower = calculateFirePower(enemyDistance);
        double bulletSpeed = 20 - 3 * bulletPower;
        
        // Find the guess factor with highest success rate
        int bestGuess = 15;
        int bestCount = guessFactors[15];
        for (int i = 0; i < GUESS_FACTORS; i++) {
            if (guessFactors[i] > bestCount) {
                bestCount = guessFactors[i];
                bestGuess = i;
            }
        }
        
        // Convert guess factor to angle offset
        double guessOffset = (bestGuess - 15.0) / 15.0;
        double maxEscapeAngle = Math.asin(8.0 / bulletSpeed);
        
        // Calculate predicted bearing
        double predictedAngle = absoluteBearing + guessOffset * maxEscapeAngle * Math.signum(enemyVelocity);
        
        // Add simple linear prediction as baseline
        double timeToHit = enemyDistance / bulletSpeed;
        double linearOffset = Math.atan2(
            enemyVelocity * timeToHit * Math.sin(Math.toRadians(enemyHeading)), 
            enemyDistance);
        
        // Blend adaptive and linear targeting
        double finalAngle = Math.toDegrees(predictedAngle) * 0.6 + 
                           (Math.toDegrees(absoluteBearing) + Math.toDegrees(linearOffset)) * 0.4;
        
        double gunTurn = Utils.normalRelativeAngleDegrees(finalAngle - getGunHeading());
        setTurnGunRight(gunTurn);
        
        // Fire if gun is aimed
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
            setFire(bulletPower);
        }
    }
    
    private double calculateFirePower(double distance) {
        // More aggressive when enraged
        if (enraged) {
            if (distance < 150) return 3.0;
            if (distance < 300) return 2.5;
            return 2.0;
        }
        
        if (distance < 100) return 3.0;
        if (distance < 200) return 2.5;
        if (distance < 400) return 2.0;
        return 1.5;
    }
    
    public void onBulletHit(BulletHitEvent e) {
        String enemyName = e.getName();
        if (targetingData.containsKey(enemyName)) {
            int[] guessFactors = targetingData.get(enemyName);
            for (int i = 0; i < GUESS_FACTORS; i++) {
                guessFactors[i] += Math.max(0, 5 - Math.abs(i - 15));
            }
        }
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        moveAngle += (Math.random() > 0.5 ? 90 : -90);
        
        // Enter rage mode below 30% health
        if (getEnergy() < 50) {
            enraged = true;
        }
    }
    
    public void onHitWall(HitWallEvent e) {
        moveAngle += 180;
    }
    
    public void onHitRobot(HitRobotEvent e) {
        double absoluteBearing = getHeading() + e.getBearing();
        setTurnGunRight(Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading()));
        setFire(3);
        setBack(80);
        execute();
    }
}
