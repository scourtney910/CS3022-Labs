package mybots;

import robocode.*;
import robocode.util.Utils;
import java.io.IOException;
import java.io.PrintStream;

/**
 * DataCollectorBot
 * A robot that uses Circle Strafing - saving its sensory data and its reaction decisions to a CSV file
 */
public class DataCollectorBot extends AdvancedRobot {

    PrintStream logger;
    int moveDirection = 1;  // 1 for forward, -1 for backward

    // We will log these 7 inputs:
    // 1. Enemy Distance
    // 2. Enemy Bearing (relative to our heading)
    // 3. Enemy Velocity
    // 4. My Energy
    // 5. Enemy Energy
    // 6. Distance to Nearest Wall
    // 7. Bearing to Nearest Wall

    // We will log these 3 outputs (actions):
    // 1. Body Turn Rate (degrees)
    // 2. Move Distance
    // 3. Fire Power
    
    public void run() {
        // Only initialize the logger once per battle (in Round 0)
        // Keep it open across all rounds to collect data continuously
        if (getRoundNum() == 0) {
            try {
                logger = new PrintStream(new RobocodeFileOutputStream(getDataFile("training_data.csv")), true);
                
                // Write CSV header
                logger.println("enemy_distance,enemy_bearing,enemy_velocity,my_energy,enemy_energy,wall_distance,wall_bearing,turn_rate,move_distance,fire_power");

                if (logger.checkError()) {
                    out.println("[DataCollector] ERROR: Failed to write CSV header!");
                } else {
                    out.println("[DataCollector] CSV header written successfully!");
                }
            } catch (IOException e) {
                out.println("[DataCollector] ERROR initializing logger: " + e.getMessage());
                e.printStackTrace(out);
            }
        } else {
            out.println("[DataCollector] Round " + getRoundNum() + " - Logger already open, continuing to log...");
        }

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            turnRadarRight(360); // Keep scanning
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // --- 1. THE LOGIC (The "Teacher" behavior) ---

        // Always stay perpendicular to the enemy (Circle Strafe)
        setTurnRight(e.getBearing() + 90);

        // Oscillate movement to confuse enemy targeting
        // moveDist set to 60 for tighter circles that fit in smaller arenas
        double moveDist = 60 * moveDirection;
        if (Math.random() < 0.10) moveDirection *= -1;  // Occasionally reverse
        setAhead(moveDist);

        // Fire logic: Fire hard if close, soft if far
        double firePower = 1.0;
        if (e.getDistance() < 200) firePower = 3.0;
        else if (e.getDistance() < 400) firePower = 2.0;

        // Only fire if gun is cool
        if (getGunHeat() == 0) {
            setFire(firePower);
        }

        // Basic Radar Lock (Keep radar focused on enemy)
        double radarTurn = getHeading() + e.getBearing() - getRadarHeading();
        setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn));

        // Basic Gun Lock (Predictive shooting is hard for NN initially, so we teach Head-On targeting)
        double gunTurn = getHeading() + e.getBearing() - getGunHeading();
        setTurnGunRight(Utils.normalRelativeAngleDegrees(gunTurn));


        // --- 2. THE LOGGING ---

        if (logger != null) {
            // Calculate wall proximity
            double wallDist = getDistanceToNearestWall();
            double wallBearing = getBearingToNearestWall();

            // INPUTS
            logger.print(e.getDistance() + ",");
            logger.print(e.getBearing() + ",");
            logger.print(e.getVelocity() + ",");
            logger.print(getEnergy() + ",");
            logger.print(e.getEnergy() + ",");
            logger.print(wallDist + ",");
            logger.print(wallBearing + ",");

            // OUTPUTS (The actions we just set)
            // Note: We log 'e.getBearing() + 90' because that was our logic for turning
            logger.print((e.getBearing() + 90) + ",");
            logger.print(moveDist + ",");
            logger.print(firePower);

            logger.println(); // New line

            // Check for write errors
            if (logger.checkError()) {
                out.println("[DataCollector] ERROR: Failed to write data at time " + getTime());
            }

            // Periodic confirmation (every 100 scans)
            if (getTime() % 100 == 0) {
                out.println("[DataCollector] Data logged successfully at time " + getTime());
            }
        } else {
            out.println("[DataCollector] WARNING: Logger is null at time " + getTime());
        }
    }

    public void onHitWall(HitWallEvent e) {
        // Wall collision - teach the bot to reverse direction
        moveDirection *= -1;
        setBack(100);  // Back away from wall
    }

    public void onBattleEnded(BattleEndedEvent e) {
        if (logger != null) {
            logger.flush();  // Ensure all buffered data is written before closing
            logger.close();
            out.println("[DataCollector] Data collection finished. CSV saved.");
        } else {
            out.println("[DataCollector] ERROR: Logger was never opened!");
        }
    }

    /**
     * Calculate distance to the nearest wall
     */
    private double getDistanceToNearestWall() {
        double x = getX();
        double y = getY();
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();

        // Distance to each wall
        double distLeft = x;
        double distRight = width - x;
        double distBottom = y;
        double distTop = height - y;

        // Return minimum distance
        return Math.min(Math.min(distLeft, distRight), Math.min(distBottom, distTop));
    }

    /**
     * Calculate bearing to the nearest wall (relative to current heading)
     */
    private double getBearingToNearestWall() {
        double x = getX();
        double y = getY();
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();

        // Distance to each wall
        double distLeft = x;
        double distRight = width - x;
        double distBottom = y;
        double distTop = height - y;

        // Find which wall is nearest
        double minDist = Math.min(Math.min(distLeft, distRight), Math.min(distBottom, distTop));

        // Calculate absolute bearing to nearest wall
        double absBearing;
        if (minDist == distLeft) {
            absBearing = 270;  // West
        } else if (minDist == distRight) {
            absBearing = 90;   // East
        } else if (minDist == distBottom) {
            absBearing = 180;  // South
        } else {
            absBearing = 0;    // North
        }

        // Convert to relative bearing
        return Utils.normalRelativeAngleDegrees(absBearing - getHeading());
    }
}