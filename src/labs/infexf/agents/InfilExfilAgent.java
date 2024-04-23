package src.labs.infexf.agents;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;

import java.util.Stack; // used for the should replace plan


// JAVA PROJECT IMPORTS


public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state)
    {
        /* 
         *  Notes:
         *  - If the destination moves within the attack radius of an enemy, the weight should be infinite
         *  - Edges which move closer to an enemy should have a higher weight
         *  - Edges moving away from the enemy should have lower weight
         *  - Potentially, edges walking into a "tunnel" should have higher weight to avoid getting trapped
         *  - Find a way to balance the goal of moving towards the goal and avoiding enemies
         */

        // Starting weight before any adjustments
        float weight = 10;

        // Loop over enemies
        for (int id : this.getOtherEnemyUnitIDs()) {
            if (state.getUnit(id) == null) {
                continue;
            }
            // Get location of the enemy as a vertex
            Vertex enemy = new Vertex(state.getUnit(id).getXPosition(), state.getUnit(id).getYPosition());
            // Attack radius of the enemy
            int attackRadius = state.getUnit(id).getTemplateView().getRange();

            float dist = DistanceMetric.chebyshevDistance(dst, enemy);

            float pDist = DistanceMetric.chebyshevDistance(src, enemy);
            if (dist < pDist) {
                weight *= 0.75;
            }
            
            if (dist <= attackRadius) {
                // The destination is within the attack radius of an enemy
                return Float.POSITIVE_INFINITY;
            }

            // Depend on distance
            if (dist == attackRadius + 1) {
                weight *= 40;
            } else if (dist == attackRadius + 2) {
                weight *= 25;
            } else if (dist == attackRadius + 3) {
                weight *= 20;
            } else if (dist == attackRadius + 4) {
                weight *= 10;
            } else if (dist == attackRadius + 5) {
                weight *= 5;
            } else if (dist == attackRadius + 6) {
                weight *= 1.5;
            } else if (dist == attackRadius + 7) {
                weight *= 1.05;
            }
        }

        // Increase the weight heavily if the new destination is a tunnel
        // This means there is a tree on the left and on the right or on the top and bottom
        if (state.isResourceAt(dst.getXCoordinate() + 1, dst.getYCoordinate()) && state.isResourceAt(dst.getXCoordinate() - 1, dst.getYCoordinate())) {
            weight *= 30;
        } else if (state.isResourceAt(dst.getXCoordinate(), dst.getYCoordinate() + 1) && state.isResourceAt(dst.getXCoordinate(), dst.getYCoordinate() - 1)) {
            weight *= 30;
        }

        // System.out.println(dst + " " + src + " " + weight);
        return weight;
    }

    // private boolean validCoordinate(int x, int y , StateView state) {
    //     // Check if a certain coordinate is valid
    //     if (!state.inBounds(x, y)) {
    //         // The Coordinate is out of bounds
    //         return false;
    //     } else if (state.resourceAt(x, y) != null || state.unitAt(x, y) != null) {
    //         // There is something on that coordinate
    //         return false;
    //     } else {
    //         // Unit can move to that coordinate
    //         return true;
    //     }
    // }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        // Loop over the enemies
        // Return true if the enemy is "too close"

        // Current plan
        Stack<Vertex> plan = this.getCurrentPlan();

        // Loop over path
        for (Vertex v : plan) {
            int x = v.getXCoordinate();
            int y = v.getYCoordinate();

            if (state.isUnitAt(x, y) && state.unitAt(x, y) != this.getMyUnitID()) {
                // There is a unit at the current location
                return true;
            }

            for (int id : this.getOtherEnemyUnitIDs()) {
                if (state.getUnit(id) == null) {
                    continue;
                }
                // Get location of the enemy as a vertex
                Vertex enemy = new Vertex(state.getUnit(id).getXPosition(), state.getUnit(id).getYPosition());
                // Attack radius of the enemy
                int attackRadius = state.getUnit(id).getTemplateView().getRange();

                float dist = DistanceMetric.chebyshevDistance(v, enemy);
                if (dist <= attackRadius + 3) {
                    // The destination is within the attack radius of an enemy
                    return true;
                }
            }
        }

        return false;
    }

}
