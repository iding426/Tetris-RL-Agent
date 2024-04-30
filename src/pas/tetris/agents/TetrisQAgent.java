package src.pas.tetris.agents;


// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.bu.battleship.utils.Coordinate;
// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.game.minos.Mino.MinoType;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;
import edu.bu.tetris.game.Block;


public class TetrisQAgent
    extends QAgent
{
    // public static final double EXPLORATION_PROB = 0.05;

    private Random random;
    private int epochCount = 1; 
    public static double previousReward = 0.0;

    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int numCols = 37;
        final int numCols = 37;
        final int hiddenDim1 = 64; // More information
        final int hiddenDim2 = 32; // Condense information
        final int outDim = 1;

        Sequential qFunction = new Sequential(); // New NN
        qFunction.add(new Dense(numCols, hiddenDim1)); // Add input layer (numCols) and connect to hiddenLayer 1 with hiddenDim1 nodes
        qFunction.add(new Sigmoid()); // Give the connection from input to hiddenLayer 1 a sigmoid activation function
        qFunction.add(new Dense(hiddenDim1, hiddenDim2)); // Connect hiddenLayer 1 with hiddenDim1 nodes to hiddenLayer 2 with hiddenDim2 nodes
        qFunction.add(new Sigmoid()); // Give the connection from hiddenLayer 1 to hiddenLayer 2 a sigmoid activation function
        qFunction.add(new Dense(hiddenDim2, outDim)); // Connect hiddlen Layer 2 with hiddenDim2 nodes to the output layer with outDim nodes
        return qFunction;
    }

    /**
        This function is for you to figure out what your features
        are. This should end up being a single row-vector, and the
        dimensions should be what your qfunction is expecting.
        One thing we can do is get the grayscale image
        where squares in the image are 0.0 if unoccupied, 0.5 if
        there is a "background" square (i.e. that square is occupied
        but it is not the current piece being placed), and 1.0 for
        any squares that the current piece is being considered for.
        
        We can then flatten this image to get a row-vector, but we
        can do more than this! Try to be creative: how can you measure the
        "state" of the game without relying on the pixels? If you were given
        a tetris game midway through play, what properties would you look for?
     */
    @Override
    public Matrix getQFunctionInput(final GameView game,
                                    final Mino potentialAction)
    {
        Matrix grayScale = null;
        try {
            grayScale = game.getGrayscaleImage(potentialAction);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // print grayscale image
        // System.out.println("Grayscale Image: ");
        // System.out.println(grayScale);

        int rows = grayScale.getShape().getNumRows();
        int cols = grayScale.getShape().getNumCols();

        // matrix to store closest hole to the top of the column and how many holes are directly below it. Hole is defined as an empty space such that there is at least one tile in the same column above it.
        Matrix heightsHoles = Matrix.zeros(3, cols);

        int maxHeight = 0; // Current tallest column

        for (int i = 0; i < cols; i ++) {
            for (int j = 0; j < rows; j++) {

                // // Found top
                if (grayScale.get(j,i) != 0.0) {

                    int height = rows - j;
                    heightsHoles.set(0, i, height);
                    maxHeight = Math.max(maxHeight, height);

                    break;
                }

            }
        }

        if (maxHeight == 0) {
            return Matrix.zeros(1, 33);
        }

        // Calculate holes
        for (int i = 0; i < cols; i++) {
            int top = 0;
            int bottom = 1;
            boolean foundFirstHole = false;
            while (bottom < rows) {
                if (grayScale.get(top, i) != 0.0 && grayScale.get(bottom, i) == 0.0) {
                    if (foundFirstHole) {
                        heightsHoles.set(2, i, heightsHoles.get(2, i) + 1);
                        bottom++;
                    } else {
                        heightsHoles.set(1, i, rows - bottom);
                        heightsHoles.set(2, i, 1);
                        bottom++;
                        foundFirstHole = true;
                    }
                } else if (grayScale.get(top, i) != 0.0 && grayScale.get(bottom, i) != 0.0 && foundFirstHole) {
                    break;
                } else {
                    top = bottom;
                    bottom++;
                }
            }
        }


        // get next 3 types of minos
        List<Mino.MinoType> nextMinos = game.getNextThreeMinoTypes();
        Matrix minos = Matrix.zeros(1, 3);

        // check length of List
        if (nextMinos.size() == 3) {
            minos.set(0, 0, ((double) nextMinos.get(0).ordinal() + 1.0) / 8.0);
            minos.set(0, 1, ((double) nextMinos.get(1).ordinal() + 1.0) / 8.0);
            minos.set(0, 2, ((double) nextMinos.get(2).ordinal() + 1.0) / 8.0);
        } else if (nextMinos.size() == 2) {
            minos.set(0, 0, ((double) nextMinos.get(0).ordinal() + 1.0) / 8.0);
            minos.set(0, 1, ((double) nextMinos.get(1).ordinal() + 1.0) / 8.0);
        } else if (nextMinos.size() == 1) {
            minos.set(0, 0, ((double) nextMinos.get(0).ordinal() + 1.0) / 8.0);
        }

        // get total amount of holes by looping through the matrix again
        double totalHoles = 0.0;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (grayScale.get(j, i) == 0.0) {
                    totalHoles++;
                }
            }
        }

        //get amount of lines cleared and detect if how many of those lines are next to each other
        double linesCleared = 0.0;
        for (int i = 0; i < rows; i++) {
            boolean flag = true;
            for (int j = 0; j < cols; j++) {
                if (grayScale.get(i, j) == 0.0) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                linesCleared++;
            }
        }

        // check if it is a T-spin
        boolean tSpin = false;
        if (potentialAction.getType() == MinoType.T) {
            // Check if this move is a T-Spin
            // Get the grayScale

            HashSet<Coordinate> tCoords = new HashSet<>();
            HashSet<Integer> rowsTspin = new HashSet<>();
            // Find the Rows the T-Spin resides in
            for (int i = 0; i < 22; i++) {
                for (int j = 0; j < 10; j++) {
                    if (grayScale.get(i,j) == 1.0) {
                        tCoords.add(new Coordinate(j,i));
                        rowsTspin.add(i);
                    }
                }
            }

            // Check for overhang
            boolean flag = false;
            for (Coordinate coord: tCoords) {
                // Check if there is a 0.5 above one of the pieces
                int row = coord.getYCoordinate();
                int col = coord.getXCoordinate();

                if (row - 1 >= 0 && grayScale.get(row - 1, col) == 0.5) {
                    flag = true;
                    break;
                }
            }

            // Has to clear lines to get points
            int clearCount = 0;
            for (int row: rowsTspin) {
                boolean flag2 = true;
                for (int col = 0; col < 10; col++) {
                    if (grayScale.get(row, col) == 0.0) {
                        flag2 = false;
                        break;
                    }
                }

                // Row cleared
                if (flag2) {
                    clearCount++;
                }
            }

            if (flag && clearCount >= 1) {
                tSpin = true;
            }
        }

        // check if it is a Tetris
        boolean tetris = false;
        if (potentialAction.getType() == MinoType.I && linesCleared >= 2) {
            tetris = true;
        }

        // Normalize the values
        totalHoles = totalHoles / 220.0;
        linesCleared = linesCleared / 22.0;

        // Normalize heights and holes
        for (int i = 0; i < cols; i++) {
            if (heightsHoles.get(0, i) != 0.0) {
                heightsHoles.set(0, i, heightsHoles.get(0, i) / rows);
            }

            if (heightsHoles.get(1, i) != 0.0) {
                heightsHoles.set(1, i, heightsHoles.get(1, i) / rows);
            }

            if (heightsHoles.get(2, i) != 0.0) {
                heightsHoles.set(2, i, heightsHoles.get(2, i) / rows);
            }
        }


        Matrix combined = Matrix.zeros(1, 37);
        Matrix flatten = null;
        Matrix flattenMinos = null;

        // flatten the matrix
        try {
            flatten = heightsHoles.flatten();
            flattenMinos = minos.flatten();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // loop through the matrix and add the values to the combined matrix
        for (int i = 0; i < 3 * cols; i++) {
            combined.set(0, i, flatten.get(0, i));
        }

        for (int i = 0; i < 3; i++) {
            combined.set(0, 3 * cols + i, flattenMinos.get(0, i));
        }

        combined.set(0, 3 * cols + 3, totalHoles);
        combined.set(0, 3 * cols + 4, linesCleared);
        combined.set(0, 3 * cols + 5, tSpin ? 1.0 : 0.0);
        combined.set(0, 3 * cols + 6, tetris ? 1.0 : 0.0);

        // System.out.println("Combined: ");
        // System.out.println(combined);

        return combined;
    }

    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game,
                                 final GameCounter gameCounter)
    {
        // rand being a random number between 0 and 1
        double rand = this.random.nextDouble();
        double b = Math.log(500) / 10000;
        double prob = Math.exp(-b * this.epochCount);
        return (rand < prob);
    }

    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */
    @Override
    public Mino getExplorationMove(final GameView game)
    {

        Mino result = null;
        List<Double> qValues = new ArrayList<Double>();
        HashMap<Double, Mino> qValueToMino = new HashMap<Double, Mino>();
        HashMap<Double, Mino> softmaxToMino = new HashMap<Double, Mino>();

        for (Mino pAction: game.getFinalMinoPositions()) {
            // Check for T-spin, Double T-Spin, or Tetris, and force it to explore that
            if (pAction.getType() == MinoType.T) {
                // Check if this move is a T-Spin
                // Get the grayScale
                Matrix grayScale = null;

                try {
                    grayScale = game.getGrayscaleImage(pAction);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                HashSet<Coordinate> tCoords = new HashSet<>();
                HashSet<Integer> rows = new HashSet<>();
                // Find the Rows the T-Spin resides in
                for (int i = 0; i < 22; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (grayScale.get(i,j) == 1.0) {
                            tCoords.add(new Coordinate(j,i));
                            rows.add(i);
                        }
                    }
                }

                // Check for overhang
                boolean flag = false;
                for (Coordinate coord: tCoords) {
                    // Check if there is a 0.5 above one of the pieces
                    int row = coord.getYCoordinate();
                    int col = coord.getXCoordinate();

                    if (row - 1 >= 0 && grayScale.get(row - 1, col) == 0.5) {
                        flag = true;
                        break;
                    }
                }

                // Has to clear lines to get points
                int clearCount = 0;
                for (int row: rows) {
                    boolean flag2 = true;
                    for (int col = 0; col < 10; col++) {
                        if (grayScale.get(row, col) == 0.0) {
                            flag2 = false;
                            break;
                        }
                    }

                    // Row cleared
                    if (flag2) {
                        clearCount++;
                    }
                }

                if (flag && clearCount >= 1) {
                    return pAction;
                }
                

            } else if (pAction.getType() == MinoType.I) {
                // Get the grayScale
                Matrix grayScale = null;
                try {
                    grayScale = game.getGrayscaleImage(pAction);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                HashSet<Integer> rows = new HashSet<>();

                // All of the rows the piece spans
                for (int i = 0; i < 22; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (grayScale.get(i,j) == 1.0) {
                            rows.add(i);
                        }
                    }
                }

                // Count how many rows there are
                if (rows.size() == 4) {
                    // Check if this move clears 4 rows
                    int clearCount = 0;

                    for (int row: rows) {
                        boolean flag = true;
                        for (int col = 0; col < 10; col++) {
                            if (grayScale.get(row, col) == 0.0) {
                                flag = false;
                                break;
                            }
                        }

                        // There are no empty spaces in the row
                        if (flag) {
                            clearCount++;
                        }
                    }

                    // If it clears 4 rows, its a tetris so return it
                    if (clearCount == 4) {
                        return pAction;
                    }
                }
            } else {
                // Check if its a perfect clear
                Matrix grayScale = null;
                try {
                    grayScale = game.getGrayscaleImage(pAction);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                HashSet<Integer> rows = new HashSet<>();

                for (int i = 0; i < 22; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (grayScale.get(i,j) != 0.0) {
                            rows.add(i);
                        }
                    }
                }

                // Check if all rows are cleared 

                int clearCount = 0;
                for (int row: rows) {
                    boolean flag = true;

                    for (int col = 0; col < 10; col++) {
                        if (grayScale.get(row, col) == 0.0) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) {
                        clearCount++;
                    }
                }

                // Every row with a block is cleared
                if (clearCount == rows.size()) {
                    return pAction;
                }
            }

            // Not a T-Spin or a Tetris, so just use softmax to explore
            Matrix input = this.getQFunctionInput(game, pAction);

            try {
                Matrix qValue = this.getQFunction().forward(input);

                qValues.add(qValue.get(0, 0));
                qValueToMino.put(qValue.get(0, 0), pAction);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }

        // Softmax the list
        List<Double> softmax = new ArrayList<Double>();

        double sum = 0.0;

        for (double qValue: qValues) {
            sum += Math.pow(Math.E, qValue);
        }

        for (int i = 0; i < qValues.size(); i++) {
            softmax.add(Math.pow(Math.E, qValues.get(i)) / sum);
            softmaxToMino.put(Math.pow(Math.E, qValues.get(i)) / sum, qValueToMino.get(qValues.get(i)));
        }

        // Sort the list in ascending order
        softmax.sort((a, b) -> Double.compare(a, b));

        double[] weights = new double[softmax.size()];
        double totalWeight = 0;

        // Get total weight
        // Get total weight
        for (int i = 0; i < softmax.size(); i++) {
            weights[i] = softmax.get(i);
            weights[i] = softmax.get(i);
            totalWeight += weights[i];
        }

        // Weight based on frequency
        // Weight based on frequency
        for (int i = 0; i < softmax.size(); i++) {
            weights[i] /= totalWeight;
        }

        // Cumulative Ranges
        double[] cumulativeWeights = new double[softmax.size()];
        cumulativeWeights[0] = weights[0];

        for (int i = 1; i < softmax.size(); i++) {
            cumulativeWeights[i] = cumulativeWeights[i - 1] + weights[i];
        }

        double rand = this.random.nextDouble();

        int index = 0;

        while (index < softmax.size() && rand > cumulativeWeights[index]) {
            index++;
        }

        result = softmaxToMino.get(softmax.get(index));
        
        return result;
    }

    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a phase, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
                               LossFunction lossFunction,
                               Optimizer optimizer,
                               long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                                                  lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
        epochCount++;
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */
    @Override
    public double getReward(final GameView game)
    {
        /*
        game is a matrix of the board state, 1 is a block being placed, 0.5 is a block that is not being placed, 0 is empty. 
        
        reward function takes into account the aggregate height of the tetris grid (i.e. the sum of the heights of every column),
        the number of complete lines, the number of holes in the grid, and the "bumpiness" of the grid (i.e. the sum of the absolute differences in height between adjacent columns). 
        The actual formula for this fitness function is:

        - 0.51 x Height + 0.76 x Lines
        - 0.36 x Holes - 0.18 x Bumpiness

        And the reward was simply the change in this fitness function.
        */

        double currentReward = 0.0;
        Board board = game.getBoard();

        // Check if a new game started
        boolean flag = true;

        // Loop over whole board
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 22; j++) {
                if (board.getBlockAt(i, j) != null) {
                    flag = false;
                    break;
                }
            }
        }

        if (flag && game.getTotalScore() == 0) {
            // Reset the previous reward
            previousReward = 0.0; 
        }

        // System.out.println(Board.NUM_ROWS);

        // // loop through the board and print out the blocks
        // for (int i = 0; i < Board.NUM_ROWS; i++) {
        //     for (int j = 0; j < Board.NUM_COLS; j++) {
        //         System.out.print(i + "," + j + ": "); // Fix: Replace comma with plus sign
        //         Block block = board.getBlockAt(j, i);
        //         if (block != null) {
        //             System.out.print("X ");
        //         } else {
        //             System.out.print("O ");
        //         }
        //     }
        //     System.out.println();
        // }

        double height = 0.0;
        // get aggregate height
        for (int i = 0; i < Board.NUM_COLS; i++) {
            for (int j = 0; j < Board.NUM_ROWS; j++) {
                Block block = board.getBlockAt(i, j); // board.get(col, row)

                if (block != null) {
                    height += (Board.NUM_ROWS - j);
                    break;
                }
            }
        }

        double lines = 0.0;
        // get number of complete lines
        for (int i = 0; i < Board.NUM_ROWS; i++) {
            boolean complete = true;
            for (int j = 0; j < Board.NUM_COLS; j++) {
                Block block = board.getBlockAt(j, i);
                if (block == null) {
                    complete = false;
                    break;
                }
            }

            if (complete) {
                lines++;
            }
        }

        double holes = 0.0;
        // get number of holes. A hole is defined as an empty space such that there is at least one tile in the same column above it.
        for (int i = 0; i < Board.NUM_COLS; i++) {
            int top = 0;
            int bottom = 1;
            while (bottom < Board.NUM_ROWS) {
                Block block = board.getBlockAt(i, top);
                Block blockBelow = board.getBlockAt(i, bottom);

                if (block != null && blockBelow == null) {
                    bottom++;
                    holes++;
                } else {
                    top = bottom;
                    bottom++;
                }
            }
        }

        // System.out.println("Holes: " + holes);

        // get bumpiness
        double bumpiness = 0.0;
        for (int i = 0; i < Board.NUM_COLS - 1; i++) {
            int height1 = 0;
            int height2 = 0;

            boolean found1 = false;
            boolean found2 = false;

            for (int j = 0; j < Board.NUM_ROWS; j++) {
                Block block1 = board.getBlockAt(i, j); // board.get(col, row)
                Block block2 = board.getBlockAt(i + 1, j);

                if (block1 != null && !found1) {
                    height1 = Board.NUM_ROWS - j;
                    found1 = true;
                }

                if (block2 != null && !found2) {
                    height2 = Board.NUM_ROWS - j;
                    found2 = true;
                }

                if (found1 && found2) {
                    break;
                }
            }

            bumpiness += Math.abs(height1 - height2);
        }

        currentReward = -0.51 * height + 0.34 * lines - 0.36 * holes - 0.18 * bumpiness + (3.21 * game.getScoreThisTurn());

        // reward is the change in the fitness function
        double reward = currentReward - previousReward;

        previousReward = currentReward;

        // print all the features and the reward
        // System.out.println("Height: " + height);
        // System.out.println(" Lines: " + lines);
        // System.out.println(" Holes: " + holes);
        // System.out.println(" Bumpiness: " + bumpiness);
        // System.out.println(" Reward: " + reward);

        return reward;
    }

}
