package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


import java.util.HashSet;   // will need for dfs
import java.util.Stack;     // will need for dfs
import java.util.HashMap; 


// JAVA PROJECT IMPORTS


public class DFSMazeAgent
    extends MazeAgent
{

    public DFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        
        // All of the visited vertices
        HashSet<Vertex> visited = new HashSet<Vertex>();
        
        // The parents used to reach each vertex
        HashMap<Vertex, Vertex> parents = new HashMap<Vertex, Vertex>();


        // Coordinates of the Goal
        int xG = goal.getXCoordinate();
        int yG = goal.getYCoordinate();

        // Stack for DFS
        Stack<Vertex> stack = new Stack<Vertex>();

        Vertex end = null;

        stack.push(src);

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();

            // Coordinates of the current vertex
            int x = current.getXCoordinate();
            int y = current.getYCoordinate();

            if (isAdjacent(xG, yG, current.getXCoordinate(), current.getYCoordinate())) {
                // We have reached the goal
                end = current;
                break;
            } else {
                // We need to parse the neighbors 
                // Left
                if (validCoordinate(x - 1, y, state)) {
                    Vertex left = new Vertex(x - 1, y);
                    if (!visited.contains(left)) {
                        stack.push(left);
                        visited.add(left);
                        parents.put(left, current);
                    }
                }

                // Right
                if (validCoordinate(x + 1, y, state)) {
                    Vertex right = new Vertex(x + 1, y);
                    if (!visited.contains(right)) {
                        stack.push(right);
                        visited.add(right);
                        parents.put(right, current);
                    }
                }

                // Up
                if (validCoordinate(x, y - 1, state)) {
                    Vertex up = new Vertex(x, y - 1);
                    if (!visited.contains(up)) {
                        stack.push(up);
                        visited.add(up);
                        parents.put(up, current);
                    }
                }

                // Down
                if (validCoordinate(x, y + 1, state)) {
                    Vertex down = new Vertex(x, y + 1);
                    if (!visited.contains(down)) {
                        stack.push(down);
                        visited.add(down);
                        parents.put(down, current);
                    }
                }

                // Up Left
                if (validCoordinate(x - 1, y - 1, state)) {
                    Vertex upLeft = new Vertex(x - 1, y - 1);
                    if (!visited.contains(upLeft)) {
                        stack.push(upLeft);
                        visited.add(upLeft);
                        parents.put(upLeft, current);
                    }
                }

                // Up Right
                if (validCoordinate(x + 1, y - 1, state)) {
                    Vertex upRight = new Vertex(x + 1, y - 1);
                    if (!visited.contains(upRight)) {
                        stack.push(upRight);
                        visited.add(upRight);
                        parents.put(upRight, current);
                    }
                }

                // Down Left
                if (validCoordinate(x - 1, y + 1, state)) {
                    Vertex downLeft = new Vertex(x - 1, y + 1);
                    if (!visited.contains(downLeft)) {
                        stack.push(downLeft);
                        visited.add(downLeft);
                        parents.put(downLeft, current);
                    }
                }

                // Down Right
                if (validCoordinate(x + 1, y + 1, state)) {
                    Vertex downRight = new Vertex(x + 1, y + 1);
                    if (!visited.contains(downRight)) {
                        stack.push(downRight);
                        visited.add(downRight);
                        parents.put(downRight, current);
                    }
                }
            }
        }

        Stack<Vertex> reversePath = new Stack<Vertex>();
        reversePath.push(end);

        while (!reversePath.peek().equals(src)) {
            reversePath.push(parents.get(reversePath.peek()));
        }

        Path path = new Path(reversePath.pop());

        while (!reversePath.isEmpty()) {
            path = new Path(reversePath.pop(), 1f, path);
        }

        System.out.println(path);

        return path;
    }

    private boolean validCoordinate(int x, int y , StateView state) {
        // Check if a certain coordinate is valid
        if (!state.inBounds(x, y)) {
            // The Coordinate is out of bounds
            return false;
        } else if (state.resourceAt(x, y) != null || state.unitAt(x, y) != null) {
            // There is something on that coordinate
            return false;
        } else {
            // Unit can move to that coordinate
            return true;
        }
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        if (x2 == x1 - 1 && y2 == y1) {
            // Left
            return true;
        } else if (x2 == x1 + 1 && y2 == y1) {
            // Right
            return true;
        } else if (x2 == x1 && y2 == y1 - 1) {
            // Up
            return true;
        } else if (x2 == x1 && y2 == y1 + 1) {
            // Down
            return true;
        } else if (x2 + 1 == x1 && y2 + 1 == y1) {
            // Up Right
            return true;
        } else if (x2 - 1 == x1 && y2 - 1 == y1) {
            // Down Left
            return true;
        } else if (x2 + 1 == x1 && y2 - 1 == y1) {
            // Down Right
            return true;
        } else if (x2 - 1 == x1 && y2 + 1 == y1) {
            // Up Left
            return true;
        } else {
            // Not adjacent
            return false;
        }
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        // The current plan
        Stack<Vertex> s = this.getCurrentPlan();

        // Loop over the vertices in the stack
        while (!s.isEmpty()) {
            Vertex current = s.pop();

            // Check if its valid
            if (!validCoordinate(current.getXCoordinate(), current.getYCoordinate(), state)) {
                return true;
            }
        }

        // All valid squares
        return false;
    }

}
