Haybee Robot – Algorithm Explanation

By: LT Abiola Bamgbose



Robot Name: Haybee



Overall Strategy

Haybee uses a straightforward patrol-and-attack approach. It moves back and forth across the battlefield while continuously scanning for opponents with its gun. When it detects another robot, it fires with different power levels depending on how close the target is. It also responds to being hit or bumping into obstacles by changing direction and creating distance.



Movement Pattern

Haybee follows a repeating movement cycle:

\- Move forward 100 pixels

\- Move backward 100 pixels

\- After each forward or backward move, turn 45 degrees to the right



This creates a zigzag path that sweeps through different parts of the battlefield. The 45-degree turns keep the movement from becoming predictable and help prevent getting stuck in one direction.



Scanning Strategy

During movement, the gun rotates a full 360 degrees after each forward and backward motion. Spinning the gun twice per cycle provides full radar coverage so enemies can be detected from any direction.



Targeting and Firing

When another robot is detected, Haybee uses a distance-based firing strategy:

\- If the enemy is within 200 pixels, it fires with maximum power (3.0) because close-range shots are more accurate and deal more damage.

\- If the enemy is more than 200 pixels away, it fires with lower power (1.0) to conserve energy, since long-range shots are more likely to miss.



Defensive Behaviors



Getting Hit by Bullets

When Haybee is hit by a bullet, it tries to dodge by moving perpendicular to the incoming fire. It does this by turning 90 degrees minus the bullet bearing, then moving ahead 100 pixels. This is intended to move it out of the line of fire.



Hitting Walls

If Haybee runs into a wall, it backs up 50 pixels and then turns 90 degrees to the right. This backs it away from the wall and points it in a new direction so it does not stay stuck.



Bumping Other Robots

On physical collision with another robot, Haybee fires a medium-power shot (2.0) because the target is extremely close, then backs up 50 pixels and turns 30 degrees to create some space.



Color Scheme

The robot’s colors are:

\- Body: Red

\- Gun: Blue

\- Radar: Green



These contrasting colors make it easy to track visually during battles.



Key Methods Used

Main Robocode API methods used include:



Movement

\- ahead(distance) – moves the robot forward by the given number of pixels

\- back(distance) – moves the robot backward

\- turnRight(degrees) – rotates the robot’s body to the right



Gun Control

\- turnGunRight(degrees) – rotates the gun independently of the body

\- fire(power) – fires a bullet with a power value between 0.1 and 3.0



Appearance

\- setColors(bodyColor, gunColor, radarColor) – sets the colors for the body, gun, and radar



Event Information

\- e.getDistance() – returns the distance to a scanned robot

\- e.getBearing() – returns the relative direction to a scanned robot or bullet



What I Learned

Through experimentation with different movement styles, the zigzag pattern proved more effective than simple circles or straight lines. It provided better coverage and made Haybee harder to hit. Testing also showed how important energy management is; firing at maximum power constantly drains energy too quickly.



Tuning the wall-avoidance logic was the most challenging part. Early versions often got stuck in corners, but adding a backward step before turning helped Haybee escape tight spots.



Testing Results

Haybee was tested against several sample robots:

\- It defeated sample.Fire most of the time.

\- It had mixed results against sample.Crazy because of that robot’s unpredictable movement.

\- It struggled against sample.Tracker, which closely follows its opponent.



Time Spent

Total time spent on the project was about 12 hours:

\- 2 hours learning Robocode basics and the API

\- 3 hours writing the initial code and testing basic movement

\- 4 hours refining targeting and defensive behavior

\- 2 hours testing against different opponents

\- 1 hour documenting and final testing



Academic Integrity Statement

I, Abiola Bamgbose, designed the algorithm and wrote the code for Haybee independently. I spent at least 10 hours on this project. I studied the sample robots and the official API documentation to understand how Robocode works, then created my own strategy and implementation.



