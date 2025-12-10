package mybots;

import robocode.*;
import robocode.util.Utils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * NeuralNetBot
 * A robot that reads a raw text file containing NN weights
 * and performs a Forward Pass to decide on actions.
 */
public class NeuralNetBot extends AdvancedRobot {

    // Network Architecture 
    // Note that this MUST match the training.py globals
    final int INPUT_NODES = 7;
    final int HIDDEN_NODES = 20;
    final int OUTPUT_NODES = 3;

    // Input normalization constants
    double[] inputMean;
    double[] inputStd;

    // Output normalization constants
    double[] outputMean;
    double[] outputStd;

    // Layer 1 (Input -> Hidden 1)
    double[][] w1;
    double[] b1;

    // Layer 2 (Hidden 1 -> Hidden 2)
    double[][] w2;
    double[] b2;

    // Layer 3 (Hidden 2 -> Output)
    double[][] w3;
    double[] b3;

    boolean weightsLoaded = false;

    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Try to load the brain
        try {
            loadWeights("neural_weights.txt");
            weightsLoaded = true;
        } catch (Exception e) {
            out.println("ERROR: Could not load weights! " + e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            // If weights failed, just spin radar
            turnRadarRight(360);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (!weightsLoaded) return;

        // PREPARE INPUTS
        // Has to match the order of the DataCollectorBot.java
        double[] inputs = new double[INPUT_NODES];
        inputs[0] = e.getDistance();
        inputs[1] = e.getBearing();
        inputs[2] = e.getVelocity();
        inputs[3] = getEnergy();
        inputs[4] = e.getEnergy();
        inputs[5] = getDistanceToNearestWall();
        inputs[6] = getBearingToNearestWall();

        // NORMALIZE (Formula: (Val - Mean) / Std)
        for (int i = 0; i < INPUT_NODES; i++) {
            inputs[i] = (inputs[i] - inputMean[i]) / inputStd[i];
        }

        // FORWARD PASS
        // Layer 1
        double[] h1 = denseLayer(inputs, w1, b1, true); // true = use ReLU
        // Layer 2
        double[] h2 = denseLayer(h1, w2, b2, true);
        // Output Layer
        double[] outputs = denseLayer(h2, w3, b3, false); // false = Linear

        // DENORMALIZE OUTPUTS (reverse the normalization from training)
        // Formula: Val = (NormalizedVal * Std) + Mean
        for (int i = 0; i < OUTPUT_NODES; i++) {
            outputs[i] = (outputs[i] * outputStd[i]) + outputMean[i];
        }

        // DECODE OUTPUTS -> ACTIONS
        // Output 0: Turn Right Degrees
        // Output 1: Move Ahead Distance
        // Output 2: Fire Power

        setTurnRight(outputs[0]);
        setAhead(outputs[1]);
        
        // Safety check for fire power
        double firePower = outputs[2];
        if (firePower > 3) firePower = 3;
        if (firePower < 0.1) firePower = 0;
        
        // Only fire if gun is roughly cool
        if (getGunHeat() < 0.5 && firePower > 0.1) {
            setFire(firePower);
        }

        // NON-NN HOUSEKEEPING
        // (We keep radar/gun locking simple in Java because NNs struggle to learn
        // tracking without massive datasets)
        setTurnRadarRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getGunHeading()));
    }

    // --- MATH HELPERS ---

    /**
     * Performs Matrix Dot Product + Bias + Activation
     */
    private double[] denseLayer(double[] input, double[][] weights, double[] biases, boolean relu) {
        double[] output = new double[biases.length];

        for (int i = 0; i < output.length; i++) {
            double sum = 0;
            // Dot Product
            for (int j = 0; j < input.length; j++) {
                sum += input[j] * weights[j][i];
            }
            // Add Bias
            sum += biases[i];
            
            // Activation Function
            if (relu) {
                output[i] = Math.max(0, sum); // ReLU
            } else {
                output[i] = sum; // Linear
            }
        }
        return output;
    }

    /**
     * Parsers the simple CSV format
     */
    private void loadWeights(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getDataFile(filename))));

        // Read Input Normalization Stats
        inputMean = parseLine(br.readLine());
        inputStd = parseLine(br.readLine());

        // Read Output Normalization Stats
        outputMean = parseLine(br.readLine());
        outputStd = parseLine(br.readLine());

        // Initialize Arrays based on architecture
        w1 = new double[INPUT_NODES][HIDDEN_NODES];
        b1 = new double[HIDDEN_NODES];
        w2 = new double[HIDDEN_NODES][HIDDEN_NODES];
        b2 = new double[HIDDEN_NODES];
        w3 = new double[HIDDEN_NODES][OUTPUT_NODES];
        b3 = new double[OUTPUT_NODES];

        // Load Layer 1
        fillWeights(w1, br.readLine());
        b1 = parseLine(br.readLine());

        // Load Layer 2
        fillWeights(w2, br.readLine());
        b2 = parseLine(br.readLine());

        // Load Layer 3
        fillWeights(w3, br.readLine());
        b3 = parseLine(br.readLine());

        br.close();
    }

    private double[] parseLine(String line) {
        StringTokenizer st = new StringTokenizer(line, ",");
        double[] res = new double[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            res[i++] = Double.parseDouble(st.nextToken());
        }
        return res;
    }

    // Helper to map flattened weight list back to 2D array
    private void fillWeights(double[][] matrix, String line) {
        double[] flat = parseLine(line);
        int k = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = flat[k++];
            }
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