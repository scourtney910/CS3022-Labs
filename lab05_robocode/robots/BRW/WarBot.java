package BRW;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class WarBot extends AdvancedRobot {

    private double moveAmount;
    private boolean peek = false; 

    @Override
    public void run() {
        
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        setBodyColor(Color.black);
        setGunColor(Color.darkGray);
        setRadarColor(Color.orange);
        setBulletColor(Color.cyan);
        setScanColor(Color.cyan);

        
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        peek = false;

        
        turnLeft(getHeading() % 90);  
        ahead(moveAmount);            
        turnRight(90);                

        
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        
        pointGunInwardCenter();

        
        peek = true;
        while (true) {
            ahead(moveAmount);  
            peek = false;
            turnRight(90);      
            peek = true;
        }
    }

    
    private void pointGunInwardCenter() {
        double inward = getInwardAngle();
        double turn = Utils.normalRelativeAngleDegrees(inward - getGunHeading());
        turnGunRight(turn); 
    }

    
    private double getInwardAngle() {
        double centerX = getBattleFieldWidth() / 2.0;
        double centerY = getBattleFieldHeight() / 2.0;

        double dx = centerX - getX();
        double dy = centerY - getY();

    
        double angleRad = Math.atan2(dx, -dy);
        double angleDeg = Math.toDegrees(angleRad);

        return Utils.normalAbsoluteAngleDegrees(angleDeg);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        
        double absBearing = getHeading() + e.getBearing();
        double gunTurn = Utils.normalRelativeAngleDegrees(absBearing - getGunHeading());
        setTurnGunRight(gunTurn);

        
        double radarTurn = Utils.normalRelativeAngleDegrees(absBearing - getRadarHeading());
        setTurnRadarRight(radarTurn * 2);

        
        if (Math.abs(gunTurn) < 15) {
            setFire(2.5);
        } else {
            setFire(1.0);
        }

        
        if (peek) {
            scan(); 
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        
        if (e.getBearing() > -90 && e.getBearing() <= 90) {
            back(75);
        } else {
            ahead(75);
        }
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        
        back(50);
    }
}
