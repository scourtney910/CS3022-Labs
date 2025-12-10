import pandas as pd
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# CONFIG GLOBALS
INPUT_COLS = 7  # Distance, Bearing, E_Vel, My_Energy, E_Energy, Wall_Dist, Wall_Bearing
OUTPUT_COLS = 3 # Turn, Move, Fire
HIDDEN_NODES = 20

# LOAD DATA
# training_data.csv has to be in the same directory as this file
try:
    data = pd.read_csv('training_data.csv')
except FileNotFoundError:
    print("Error: training_data.csv not found")
    exit()

print(f"Loaded {len(data)} rows of battle data.")

# Split into Inputs (X) and Targets (y)
X = data.iloc[:, 0 : INPUT_COLS].values
y = data.iloc[:, INPUT_COLS : INPUT_COLS + OUTPUT_COLS].values

# NORMALIZE VALUES
# Neural nets fail if one input is large (i.e. 1000 Distance) and another is small (i.e. 0.2 Velocity)
# We calculate Mean and StdDev to scale inputs to roughly -1 to 1.
mean_X = X.mean(axis=0)
std_X = X.std(axis=0)
# Avoid division by zero if a value never changes
std_X[std_X == 0] = 1.0

X_normalized = (X - mean_X) / std_X

# Normalize outputs as well
# Without this, the neural net ignores fire power (range ~3) and focuses on
# turn/move (range ~200) because they contribute more to the loss function
mean_y = y.mean(axis=0)
std_y = y.std(axis=0)
std_y[std_y == 0] = 1.0

y_normalized = (y - mean_y) / std_y

# BUILD MODEL
model = keras.Sequential([
    layers.Input(shape=(INPUT_COLS,)),
    layers.Dense(HIDDEN_NODES, activation='relu'),  # Hidden Layer 1
    layers.Dense(HIDDEN_NODES, activation='relu'),  # Hidden Layer 2
    layers.Dense(OUTPUT_COLS, activation='linear')  # Output Layer (for regression)
])

model.compile(optimizer='adam', loss='mse', metrics=['mae'])

# TRAIN
model.fit(X_normalized, y_normalized, epochs=100, batch_size=32, validation_split=0.2, verbose=1)

# EXPORT WEIGHTS TO A TEXT FILE
# We need a file format that is incredibly easy to read in Java without Java libraries.
# Format:
# Line 1: Input Means (comma separated)
# Line 2: Input Stds (comma separated)
# Line 3: Output Means (comma separated)
# Line 4: Output Stds (comma separated)
# Line 5: Layer 1 Weights (flattened)
# Line 6: Layer 1 Biases
# Line 7: Layer 2 Weights
# Line 8: Layer 2 Biases
# Line 9: Output Layer Weight
# Line 10: Output Layer Bias

with open('neural_weights.txt', 'w') as f:
    # Header: Input Normalization Constants
    f.write(",".join(map(str, mean_X)) + "\n")
    f.write(",".join(map(str, std_X)) + "\n")

    # Header: Output Normalization Constants
    f.write(",".join(map(str, mean_y)) + "\n")
    f.write(",".join(map(str, std_y)) + "\n")

    # Layers
    for layer in model.layers:
        weights, biases = layer.get_weights()
        
        # Flatten weights row by row
        # Java will read this as a long stream of numbers
        w_flat = weights.flatten() 
        f.write(",".join(map(str, w_flat)) + "\n")
        
        b_flat = biases.flatten()
        f.write(",".join(map(str, b_flat)) + "\n")

print("Done - now move neural_weights.txt to the neural net's data directory")