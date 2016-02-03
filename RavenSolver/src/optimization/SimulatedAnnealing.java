package optimization;

import rule.Rule;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures, float T0, float uFactor, int coolingIter,
    int maxIter, long seed) {
        Random rand = new Random(seed);
        Utils.getInitialState(S, availableRules, maxRules, rand);
        run(S, availableRules, maxRules, numCellFigures, T0, uFactor, coolingIter, maxIter, rand);
    }

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures, float T0, float uFactor, int coolingIter,
    int maxIter, Random rand) {
        if ( (maxRules < 1) || (numCellFigures < 1) || (T0 < 1) || (uFactor < 0)
        || (coolingIter <= 0) || (coolingIter > maxIter) ) {
            throw new IllegalArgumentException();
        }
        int cost, nextCost, bestCost, iter, iterTemp = 0, worstCost, maxSize = 0;
        float T = T0;
        int[] availableIndices = new int[availableRules.size()];
        for (iter = 0; iter < availableIndices.length; iter++) {
            availableIndices[iter] = iter;
        }
        ArrayList<Rule> bestS = new ArrayList<Rule>(), nextS = new ArrayList<Rule>();
        Utils.getInitialState(S, availableRules, maxRules, rand);
        cost = Utils.cost(S, numCellFigures);
        bestS.addAll(S);   bestCost = cost;   iter = 0;
        worstCost = cost;   maxSize = S.size();
        // simulated annealing
        while ( (bestCost > 0) && (iter < maxIter) ) {
            Utils.getRandomNeighbor(S, nextS, availableRules, maxRules, availableIndices, rand);
            nextCost = Utils.cost(nextS, numCellFigures);
            if (nextCost < bestCost) {
                bestCost = nextCost;   bestS.clear();   bestS.addAll(nextS);
            } else if (nextCost > worstCost) { worstCost = nextCost; }
            if (nextS.size() > maxSize) { maxSize = nextS.size(); }
            // accept or reject the neighbor
            if (accept(cost, nextCost, T, rand)) {
                cost = nextCost;   S.clear();   S.addAll(nextS);
            }
            iter++;   iterTemp++;
            if (iterTemp == coolingIter) { iterTemp = 0;   T *= uFactor; }
        }
        S.clear();   S.addAll(bestS);
    }

    // private methods ---------------------------------------------------------

    private static boolean accept(int cost, int nextCost, float T, Random rand) {
        if (nextCost <= cost) { return true; }
        float x = (float) Math.exp( (cost-nextCost) / T );
        if ( rand.nextFloat() < x ) { return true; }
        return false;
    }
    
}
