package optimization;

import rule.Rule;
import java.util.ArrayList;
import java.util.Random;

public class SteepestDescent {

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures, long seed) {
        Utils.getInitialState(S, availableRules, maxRules, new Random(seed));
        run(S, availableRules, maxRules, numCellFigures);
    }

    public static void run(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, int numCellFigures) {
        if ( (maxRules < 1) || (numCellFigures < 1) ) { throw new IllegalArgumentException(); }
        int nextCost = -1;
        do {
            nextCost = Utils.goToBestNeighbor(S, availableRules, numCellFigures);
            if (nextCost == -1) { return; }
        } while (true);
    }
    
}
