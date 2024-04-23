#!/bin/bash

# Compile the Java code
javac -cp "./lib/*;." @infexf.srcs

# Function to run a single instance of the game
run_instance() {
    java -cp "./lib/*;." edu.cwru.sepia.Main2 data/labs/infexf/TwoUnitSmallMaze.xml &
}

# Run 100 instances concurrently
for ((i=1; i<=100; i++)); do
    run_instance
done

# Wait for all instances to finish
wait