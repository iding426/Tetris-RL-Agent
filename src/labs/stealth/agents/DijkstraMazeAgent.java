package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;                           // Directions in Sepia


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue; // heap in java
import java.util.Set;
import java.util.Stack;


// JAVA PROJECT IMPORTS

public class DijkstraMazeAgent
    extends MazeAgent
{

    public DijkstraMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        // Maps of distances to vertices
        HashMap<Vertex, Float> dist = new HashMap<Vertex, Float>();

        // Parents map 
        HashMap<Vertex, Vertex> parents = new HashMap<Vertex, Vertex>();

        // Priority Queue for next locked vertex 
        PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(Comparator.comparingDouble(v -> dist.get(v)));

        // End
        Vertex end = null;

        dist.put(src, 0f);
        pq.add(src);

        while (!pq.isEmpty()) {
            Vertex current = pq.poll();
            int x = current.getXCoordinate();
            int y = current.getYCoordinate();
            // Check if we reached the goal
            if (isAdjacent(x, y, goal.getXCoordinate(), goal.getYCoordinate())) {
                end = current;
                pq.clear();
                break;
            } else {
                // Up left
                if (validCoordinate(x - 1, y - 1, state)) {
                    Vertex v = new Vertex(x - 1, y - 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + (float) (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2)));
                        pq.add(v);
                        parents.put(v, current);
                    } else if (dist.get(v) > dist.get(current) + (float) (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2))) {
                        dist.put(v, dist.get(current) + (float) (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2)));
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Up
                if (validCoordinate(x, y - 1, state)) {
                    Vertex v = new Vertex(x, y - 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + 10);
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + 10) {
                        dist.put(v, dist.get(current) + 10);
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Up right
                if (validCoordinate(x + 1, y - 1, state)) {
                    Vertex v = new Vertex(x + 1, y - 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2)));
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2))) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + Math.pow(10, 2)));
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Right
                if (validCoordinate(x + 1, y, state)) {
                    Vertex v = new Vertex(x + 1, y);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + 5);
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + 5) {
                        dist.put(v, dist.get(current) + 5);
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Down Right
                if (validCoordinate(x + 1, y + 1, state)) {
                    Vertex v = new Vertex(x + 1, y + 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1));
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1)) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1));
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Down
                if (validCoordinate(x, y + 1, state)) {
                    Vertex v = new Vertex(x, y + 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + 1);
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + 1) {
                        dist.put(v, dist.get(current) + 1);
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Down Left
                if (validCoordinate(x - 1, y + 1, state)) {
                    Vertex v = new Vertex(x - 1, y + 1);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1));
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1)) {
                        dist.put(v, dist.get(current) + (float) Math.sqrt(Math.pow(5,2) + 1));
                        parents.put(v, current);
                        pq.add(v);
                    }
                }

                // Left
                if (validCoordinate(x - 1, y, state)) {
                    Vertex v = new Vertex(x - 1, y);

                    // Check if the vertex has been visited, if not add it to the map
                    // If so check if the new path is shorter
                    if (!dist.containsKey(v)) {
                        dist.put(v, dist.get(current) + 5);
                        parents.put(v, current);
                        pq.add(v);
                    } else if (dist.get(v) > dist.get(current) + 5) {
                        dist.put(v, dist.get(current) + 5);
                        parents.put(v, current);
                        pq.add(v);
                    }
                }
            }
            
        }

        // Create the path using a stack to give the right order
        Stack<Vertex> stack = new Stack<Vertex>();
        stack.push(end);
        System.out.println(dist.get(end));
        System.out.println(end);
        while (!stack.peek().equals(src)) {
            stack.push(parents.get(stack.peek()));
        }

        Path path = new Path(stack.pop());

        while (!stack.isEmpty()) {
            Vertex p = stack.pop();
            float cost = dist.get(p) - dist.get(parents.get(p));
            path = new Path(p, cost, path);
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
