package src.pas.battleship.agents;


// SYSTEM IMPORTS
import java.util.*;


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.EnemyBoard;
import edu.bu.battleship.game.ships.Ship;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;


public class ProbabilisticAgent
    extends Agent
{

    private static Map<Ship.ShipType, Integer> shipLengths = new HashMap<Ship.ShipType, Integer>();
    private static Coordinate previousShot; // The last shot we took
    private static HashSet<Coordinate> hitSquares = new HashSet<Coordinate>(); // The squares that we have hit and not sunk

    public ProbabilisticAgent(String name)
    {
        super(name);
        shipLengths.put(Ship.ShipType.AIRCRAFT_CARRIER, 5);
        shipLengths.put(Ship.ShipType.BATTLESHIP, 4);
        shipLengths.put(Ship.ShipType.DESTROYER, 3);
        shipLengths.put(Ship.ShipType.SUBMARINE, 3);
        shipLengths.put(Ship.ShipType.PATROL_BOAT, 2); 

        previousShot = null; 

        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game)
    {
        EnemyBoard.Outcome[][] previousShots = game.getEnemyBoardView(); // Check the previous shot

        if (previousShot != null) {
            // If the previous shot was a hit, add it to the hitSquares
            if (previousShots[previousShot.getXCoordinate()][previousShot.getYCoordinate()].equals(Outcome.HIT)) {
                hitSquares.add(previousShot);
                // System.out.println("Hit at: " + previousShot.toString());
            }
        }

        List<Coordinate> sunkSquares = new ArrayList<Coordinate>(); // List of sunk squares

        // Remove any sunked squares from the hitSquares
        for (Coordinate square: hitSquares) {
            if (previousShots[square.getXCoordinate()][square.getYCoordinate()] == Outcome.SUNK) {
                sunkSquares.add(square);
            }
        }

        for (Coordinate square: sunkSquares) {
            hitSquares.remove(square);
        }

        sunkSquares.clear(); // Clear the list
        
        int[][] probMap = getHeatMap(game); // Generate Base heat Map

        /*
         * Notes:
         * -- If there are "hit" not sunken squares, then weight the neighbors of the hit square higher (*2 or *3 for example) 
         * -- If the previous "hit" non sinks are aligned vertically, weight vertical neighbors more. Vice versa for horizontal (need some way to detect direction of hits)
         *    -- Could use a hashset to store hits, then start with one and check if vertical or horizonal neighbors are in the set
         *    -- Iterate over each coordinate in the set, if it has horizonal neighbors, 
         *       increase weight of horizontal "UNKNOWN" neighbors higher and increase weight of vertical "UNKNOWN" neighbors a bit less.
         *       Vice versa for vertical neighbors contained in the set
         */

        // Loop through hitSquares
        for (Coordinate square: hitSquares) {
            boolean horizontal = false; // Flags for if horizontal hits exist
            boolean vertical = false; // Flags for if vertical hits exist

            // Horizontal and Vertical flags
            if (hitSquares.contains(new Coordinate(square.getXCoordinate() + 1, square.getYCoordinate())) || hitSquares.contains(new Coordinate(square.getXCoordinate() - 1, square.getYCoordinate()))) {
                horizontal = true;
            }

            if (hitSquares.contains(new Coordinate(square.getXCoordinate(), square.getYCoordinate() + 1)) || hitSquares.contains(new Coordinate(square.getXCoordinate(), square.getYCoordinate() - 1))) {
                vertical = true;
            }

            int x = square.getXCoordinate();
            int y = square.getYCoordinate();

            // Up neighbor
            if (y - 1 >= 0 && previousShots[x][y - 1] == Outcome.UNKNOWN) {
                if (vertical) {
                    probMap[x][y - 1] += 150;
                } else {
                    probMap[x][y - 1] += 75;
                
                }
            }

            // Right neighbor
            if (x + 1 < probMap.length && previousShots[x + 1][y] == Outcome.UNKNOWN) {
                if (horizontal) {
                    probMap[x + 1][y] += 150;
                } else {
                    probMap[x + 1][y] += 75;
                }
            }

            // Down neighbor
            if (y + 1 < probMap[0].length && previousShots[x][y + 1] == Outcome.UNKNOWN) {
                if (vertical) {
                    probMap[x][y + 1] += 150;
                } else {
                    probMap[x][y + 1] += 75;
                }
            }

            // Left neighbor 
            if (x - 1 >= 0 && previousShots[x - 1][y] == Outcome.UNKNOWN) {
                if (horizontal) {
                    probMap[x - 1][y] += 150;
                } else {
                    probMap[x - 1][y] += 75;
                }
            }
        }

        // Pick square with the highest probability
        int maxProb = 0;
        Coordinate target = null; 

        // Get the most probable square to shoot at
        for (int i = 0; i < probMap.length; i++) {
            for (int j = 0; j < probMap[0].length; j++) {
                if (probMap[i][j] > maxProb) {
                    maxProb = probMap[i][j];
                    target = new Coordinate(i, j);
                }
            }
        }

        previousShot = target; // Update the previous shot
        
        // System.out.println();
        // System.out.println("Attacking square at: " + target.toString());
        // System.out.println();

        return target;
    }

    private int[][] getHeatMap(final GameView game) {

        // Get the dimension of the game board 
        int rows = game.getGameConstants().getNumRows();
        int cols = game.getGameConstants().getNumCols();

        // Should be init to a grid of 0.0s 
        int[][] heatMap = new int[rows][cols];

        // Previous Shots 
        EnemyBoard.Outcome[][] previousShots = game.getEnemyBoardView();

        Map<Ship.ShipType, Integer> shipCounts = game.getEnemyShipTypeToNumRemaining();

        // Loop over each ship type
        for (Ship.ShipType ship : shipCounts.keySet()) {
            
            int remainingLength = shipLengths.get(ship) - 1; // The length of the current polling ship, subtract one because current square counts as one of the "length"

            int count = shipCounts.get(ship); // The count of the number of ships of this type remaining

            // Loop over board
            for (int i = 0; i < heatMap.length; i++) {
                for (int j = 0; j < heatMap[0].length; j++) {

                    // Only poll this cell, if we have not shot here before
                    if (previousShots[i][j].equals(Outcome.UNKNOWN)) {
                        // List of possible orientation's endpoints
                        List<Coordinate> endpoints = new ArrayList<Coordinate>();

                        if (i - remainingLength >= 0) {
                            endpoints.add(new Coordinate(i - remainingLength, j));
                        }

                        if (i + remainingLength < heatMap.length) {
                            endpoints.add(new Coordinate(i + remainingLength, j));
                        }

                        if (j - remainingLength >= 0) {
                            endpoints.add(new Coordinate(i, j - remainingLength));
                        }

                        if (j + remainingLength < heatMap[0].length) {
                            endpoints.add(new Coordinate(i, j + remainingLength));
                        }

                        // If all points between [i][j] and endpoint are unknown, increment the heatmap
                        for (Coordinate endpoint: endpoints) {
                            boolean valid = true;
                            int x = i;
                            int y = j;

                            // Check which direction the boat is for this endpoint
                            if (endpoint.getXCoordinate() == x) {
                                // Vertical so change y
                                y = Math.min(y, endpoint.getYCoordinate());
                                for (int k = 0; k < remainingLength + 1; k++) {
                                    if (!previousShots[x][y + k].equals(Outcome.UNKNOWN)) {
                                        valid = false;
                                        break;
                                    }
                                }

                            } else {
                                // Horizonal so change x
                                x = Math.min(x, endpoint.getXCoordinate());
                                for (int k = 0; k < remainingLength + 1; k++) {
                                    if (!previousShots[x + k][y].equals(Outcome.UNKNOWN)) {
                                        valid = false;
                                        break;
                                    }
                                }
                            }

                            // If valid update the heatmap
                            if (valid) {
                                heatMap[i][j] += count;
                            
                            }
                        }
                    }
                }
            }

        }

        return heatMap;
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
