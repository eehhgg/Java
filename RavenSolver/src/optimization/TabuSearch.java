package optimization;

import rule.Rule;
import java.util.ArrayList;
import java.util.Random;

public class TabuSearch {

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures, int tabuSize, int maxIter, long seed) {
        Utils.getInitialState(S, availableRules, maxRules, new Random(seed));
        run(S, availableRules, maxRules, numCellFigures, tabuSize, maxIter);
    }

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures, int tabuSize, int maxIter) {
        if ( (maxRules < 1) || (numCellFigures < 1) || (tabuSize < 1) || (maxIter < 1) ) {
            throw new IllegalArgumentException();
        }
        int cost, bestCost, iter = 0;
        TabuList tabuList = new TabuList(tabuSize);
        ArrayList<Rule> bestS = new ArrayList<Rule>(), nextS = new ArrayList<Rule>();
        cost = Utils.cost(S, numCellFigures);   bestS.addAll(S);   bestCost = cost;
        // tabu search
        while ( (bestCost > 0) && (iter < maxIter) && (cost != -1) ) {
            cost = Utils.goToBestNeighbor(S, availableRules, numCellFigures, tabuList, bestCost);
            if (cost != -1) {
                if (cost < bestCost) {
                    bestCost = cost;   bestS.clear();   bestS.addAll(S);
                }
                iter++;
            }
        }
        S.clear();   S.addAll(bestS);
    }

}
