# Tetris Q-Agent

This project implements a Q-learning agent to play Tetris using a neural network for the Q-function approximation. The agent is designed to learn and improve its performance over time by training on the game states and rewards.

## Table of Contents 
- [Agent Design](#agent-design)
- [Feature Extraction](#feature-extraction)
- [Exploration Strategy](#exploration-strategy)
- [Reward Function](#reward-function)

## Agent Design 
- The agent uses a feed-forwards neural network with three layers.
- These are all densly connected layers which are using a sigmoid activation function.
- The final layer of the network is a single node which will return a value grading a specfic move.

## Feature Extraction 
- The input layer of neural network is a single row vector with 35 values.
- This row vector encodes information about the current board state.
- It includes information like the heights of each column, the number of gaps in each row, and the next three approaching pieces.
- These values are all normalized before being fed into the Neural Network.

## Exploration Strategy
- The model uses a decaying probability when deciding when to explore based on the current stage of its training.
- In exploration cases we imposed human interference, by forcing the agent to explore "high value" moves like T-Spins, multiclears, and tetris.
- If no "high value" moves are availble the agent uses a softmax function to weigh moves relatively and using those weights to probabilistically choose a move.

## Reward Function
- The utility value is a single floating value that is determined based on current height, the number of completed lines, the bumpiness, and the holes of the grid.
- Alongside these factors the number of points earned from this turn is added on.
- All of these factors are weighed by amounts outlined as hyperparameters.
- The reward that is returned is the difference in utility between the previous and current turn. 
