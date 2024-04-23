package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;

import java.util.HashSet;       // will need for bfs
import java.util.Queue;         // will need for bfs
import java.util.LinkedList;    // will need for bfs
import java.util.Set;           // will need for bfs
import java.util.Stack;
import java.util.HashMap;              


// JAVA PROJECT IMPORTS


public class BFSMazeAgent
    extends MazeAgent
{

    public BFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        // Coordinates of the Goal
        int xG = goal.getXCoordinate();
        int yG = goal.getYCoordinate();
        
        // Queue for BFS
        Queue<Vertex> queue = new LinkedList<Vertex>();

        // Hashset for BFS
        Set<Vertex> visited = new HashSet<Vertex>();

        // Add the source to the queue
        queue.add(src);
        // Add source to visited 
        visited.add(src);

        // Hashstable for parents
        HashMap<Vertex, Vertex> parents = new HashMap<Vertex, Vertex>();

        // End Vertex
        Vertex end = null;

        while (!queue.isEmpty()) {
            // Size of the current layer
            int layerSize = queue.size();

            for (int i = 0; i < layerSize; i++) {
                // Current Vertex
                Vertex current = queue.poll();

                int x = current.getXCoordinate();
                int y = current.getYCoordinate();

                // If the current vertex is adjacent to the goal, then we clear the queue and break
                if (isAdjacent(xG, yG, x, y)) {
                    end = current;
                    queue.clear();
                    break;
                } else {
                    // If not we need to add the valid, non visited adjacent vertices to the queue
                    // Left
                    if (validCoordinate(x - 1, y, state)) {
                        Vertex left = new Vertex(x - 1, y);
                        if (!visited.contains(left)) {
                            queue.add(left);
                            visited.add(left);
                            parents.put(left, current);
                        }
                    }

                    // Right
                    if (validCoordinate(x + 1, y, state)) {
                        Vertex right = new Vertex(x + 1, y);
                        if (!visited.contains(right)) {
                            queue.add(right);
                            visited.add(right);
                            parents.put(right, current);
                        }
                    }

                    // Up
                    if (validCoordinate(x, y - 1, state)) {
                        Vertex up = new Vertex(x, y - 1);
                        if (!visited.contains(up)) {
                            queue.add(up);
                            visited.add(up);
                            parents.put(up, current);
                        }
                    }

                    // Down
                    if (validCoordinate(x, y + 1, state)) {
                        Vertex down = new Vertex(x, y + 1);
                        if (!visited.contains(down)) {
                            queue.add(down);
                            visited.add(down);
                            parents.put(down, current);
                        }
                    }

                    // Up Left
                    if (validCoordinate(x - 1, y - 1, state)) {
                        Vertex upLeft = new Vertex(x - 1, y - 1);
                        if (!visited.contains(upLeft)) {
                            queue.add(upLeft);
                            visited.add(upLeft);
                            parents.put(upLeft, current);
                        }
                    }

                    // Up Right
                    if (validCoordinate(x + 1, y - 1, state)) {
                        Vertex upRight = new Vertex(x + 1, y - 1);
                        if (!visited.contains(upRight)) {
                            queue.add(upRight);
                            visited.add(upRight);
                            parents.put(upRight, current);
                        }
                    }

                    // Down Left
                    if (validCoordinate(x - 1, y + 1, state)) {
                        Vertex downLeft = new Vertex(x - 1, y + 1);
                        if (!visited.contains(downLeft)) {
                            queue.add(downLeft);
                            visited.add(downLeft);
                            parents.put(downLeft, current);
                        }
                    }

                    // Down Right
                    if (validCoordinate(x + 1, y + 1, state)) {
                        Vertex downRight = new Vertex(x + 1, y + 1);
                        if (!visited.contains(downRight)) {
                            queue.add(downRight);
                            visited.add(downRight);
                            parents.put(downRight, current);
                        }
                    }
                }
            }
        }

        Stack<Vertex> stack = new Stack<Vertex>();
        stack.push(end);

        while (!stack.peek().equals(src)) {
            stack.push(parents.get(stack.peek()));
        }

        Path path = new Path(stack.pop());

        while (!stack.isEmpty()) {
            path = new Path(stack.pop(), 1f, path);
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
