// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html
/*
RoboCode_Lab
Name: Dylan Schrom
Date: November 20, 2025

Course: CS3022 - Programming Paradigms
Description: A custom robot for RoboCode
*/

/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package dylan_robot;
import robocode.*;
//import java.awt.Color;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;


/**
 * Walls - a sample robot by Mathew Nelson, and maintained by Flemming N. Larsen
 * <p>
 * Moves around the outer edge with the gun facing in.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */

// Custom Robot by Dylan Schrom
// 1. Collects Initial Intel on Scanned Robots
// 2. Updates Removes Robots that are destroyed from its records
// 3. If num_robots > 1 it will act as Walls Robot. 
// 4. If num_robots == 1 it will act as Modified Combo of RamFire and Tracker
//    a. Ram the last robot
//    b. Modify the scanner to keep the gun on target while ramming.
//        i. Change the direction as needed to track ram.

// Key Points:
// 1. Energy Lost when firing and when hit.
// 2. Energy Gained when you ram/your shot hits another robot.
// 3. Fire Conservatively. If many robots, fire weak shots to conserve energy.
// 4. Ram when only one robot remains to maximize energy gain.

public class dylan_Robot extends AdvancedRobot {

	////////////////////////////
	// Local Variables//////////
	////////////////////////////
	
	int turnDirection = 1; 	// RAM-FIRE - Clockwise or counterclockwise
	boolean peek; 			// Don't turn if there's a robot there
	double moveAmount; 		// How much to move

	// Scan Variables
	boolean first_scan = false;				// Collects Intel.
	Set<String> enemies = new HashSet<>();	// All Enemies. Used for Strat.
	
	/////////////////////////////////////
	/////////// M E T H O D S ///////////
	/////////////////////////////////////
	
	////////////////////////////
	// Scan Robot //////////////  Add New Robots to List
	//////////////////////////// 
	 
	public void onScannedRobot(ScannedRobotEvent e) {
		
		// Collect All Enemies
		enemies.add(e.getName());
		if (!first_scan) return; // Exit if it is the first scan (only collecting data)

		////////////////////////////
		// > 1 Enemies - Walls /////
		////////////////////////////

		if (enemies.size() > 1){
			fire(2);
			// Note that scan is called automatically when the robot is moving.
			// By calling it manually here, we make sure we generate another scan event if there's a robot on the next
			// wall, so that we do not start moving up it until it's gone.
			if (peek) {
				scan();
			}
		}
		

		////////////////////////////
		// 1 Enemy - Ram Fire ////// 
		////////////////////////////
		if (enemies.size() == 1){
			if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}

		turnRight(e.getBearing());
		ahead(e.getDistance() + 5);
		scan(); // Might want to move ahead again!
		}
	}//End onScannedRobot

	////////////////////////////
	// Hit Robot ///////////////
	////////////////////////////
	
	public void onHitRobot(HitRobotEvent e) {

		////////////////////////////
		// > 1 Enemies - Walls /////
		////////////////////////////
		
		if (enemies.size() > 1){
			// If he's in front of us, set back up a bit.
			if (e.getBearing() > -90 && e.getBearing() < 90) {
				back(100);
			} 
			// else he's in back of us, so set ahead a bit.
			else {ahead(100);}
		}

		////////////////////////////
		// 1 Enemy - Ram Fire //////
		////////////////////////////
		
		if (enemies.size() == 1){
			if (e.getBearing() >= 0) {
				turnDirection = 1;
			} else {
				turnDirection = -1;
			}
			turnRight(e.getBearing());

			// Determine a shot that won't kill the robot...
			// We want to ram him instead for bonus points
			if (e.getEnergy() > 16) {
				fire(3);
			} else if (e.getEnergy() > 10) {
				fire(2);
			} else if (e.getEnergy() > 4) {
				fire(1);
			} else if (e.getEnergy() > 2) {
				fire(.5);
			} else if (e.getEnergy() > .4) {
				fire(.1);
			}
			ahead(40); // Ram him again!	
		}		
	}//End onHitRobot

	////////////////////////////
	/// On Robot Death /////////	Removes Destroyed Robot from List
	////////////////////////////
	public void onRobotDeath(RobotDeathEvent e) {
		enemies.remove(e.getName());
	}//End onRobotDeath


	// Walls - Helper for Run Method
	private void runWallsMode() {

    setBodyColor(Color.blue);
    setGunColor(Color.yellow);
    setRadarColor(Color.green);

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
        turnRight(90);

        if (enemies.size() == 1) break;
    }

    // switch to ramfire
	turnGunRight(-90); // Face gun forward
    runRamFireMode();
}

	// RamFire - Helper for Run Method
	private void runRamFireMode() {

    setBodyColor(Color.red);
    setGunColor(Color.black);
    setRadarColor(Color.yellow);

    while (true) {
		turnRadarRight(360);
        //turnRight(5 * turnDirection);
    }
}

	////////////////////////////
	/// Main Run Method ////////
	////////////////////////////
	 
	public void run() {
		// Allow independent radar
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		// Do full 360° sweep (collect only)
		while (!first_scan) {
			turnRadarRight(360);
			first_scan = true;
		}

		// Now choose mode
		if (enemies.size() > 1) {
			runWallsMode();
		} else {
			runRamFireMode();
		}
	}

} //End of Dylan_Robot.java
