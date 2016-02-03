package optimization;

import basic.CoveredFigures;
import rule.Rule;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class Utils {

    public static int cost(ArrayList<Rule> S, int numCellFigures) {
        return cost(S, numCellFigures, null);
    }

    public static int cost(ArrayList<Rule> S, int numCellFigures, StringBuilder sb) {
        if (numCellFigures < 1) { throw new IllegalArgumentException(); }
        if (S.isEmpty()) { return Integer.MAX_VALUE; }
        CoveredFigures cf = new CoveredFigures(numCellFigures);   Rule r;
        int i, operands = 0, parameters = 0, falsePositives = 0, falseNegatives = 0;
        int absNegatives = 0, absFalsePositives = 0, absFalseNegatives = 0;
        ArrayList<Point> results = new ArrayList<Point>();   Point result = new Point();
        for (i = 0; i < S.size(); i++) {
            r = S.get(i);   cf.add(r.coveredFigures);
            operands += r.numOperands();
            parameters += r.numParameters();
            absNegatives += r.coveredFigures.getAbsoluteNegativeCoverage();
            falsePositives += r.coveredFigures.getFalsePositives();
            falseNegatives += r.coveredFigures.getFalseNegatives();
            absFalsePositives += r.coveredFigures.getAbsoluteFalsePositives();
            absFalseNegatives += r.coveredFigures.getAbsoluteFalseNegatives();
            result.y = r.row;   result.x = r.col;
            if (!results.contains(result)) { results.add((Point) result.clone()); }
        }
        int positiveOverlap = cf.getAbsolutePositiveCoverage() - cf.getPositiveCoverage();
        int leftovers = numCellFigures - cf.getCoverage();
        int positiveLeftovers = numCellFigures - cf.getPositiveCoverage();
        int irregularity = results.size();
        if (sb != null) {
            sb.setLength(0);
            sb.append(positiveOverlap).append(",").append(leftovers).append(",");
            sb.append(positiveLeftovers).append(",").append(irregularity).append(",");
            sb.append(operands).append(",").append(parameters).append(",");
            sb.append(absNegatives).append(",");
            sb.append(falsePositives).append(",").append(falseNegatives).append(",");
            sb.append(absFalsePositives).append(",").append(absFalseNegatives);
        }
        return costFunction(positiveOverlap, leftovers, positiveLeftovers,
                irregularity, operands, parameters, absNegatives,
                falsePositives, falseNegatives, absFalsePositives, absFalseNegatives);
    }

    public static int maxCost(int maxRules, int numCellFigures, int numRows, int numCols) {
        if ( (maxRules < 1) || (numCellFigures < 1) || (numRows < 1) || (numCols < 1) ) {
            throw new IllegalArgumentException();
        }
        // estimated max cost for a set of maxRules rules
        int positiveOverlap = 0, leftovers = numCellFigures;
        int positiveLeftovers = numCellFigures, irregularity = maxRules;
        int operands = maxRules*2*Math.max(numRows, numCols);
        int parameters = operands, absNegatives = 0;
        int falsePositives = numCellFigures, falseNegatives = 0;
        int absFalsePositives = numCellFigures, absFalseNegatives = 0;
        return costFunction(positiveOverlap, leftovers, positiveLeftovers,
                irregularity, operands, parameters, absNegatives,
                falsePositives, falseNegatives, absFalsePositives, absFalseNegatives);
    }

    public static int goToBestNeighbor(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int numCellFigures) {
        if (numCellFigures < 1) { throw new IllegalArgumentException(); }
        ArrayList<Rule> nextS = new ArrayList<Rule>(S.size()+1);
        int move, index, cost, bestCost = cost(S, numCellFigures), bestMove = -1;
        nextS.clear();   nextS.addAll(S);   Rule r;
        for (move = 0; move < availableRules.size(); move++) {
            // move
            r = availableRules.get(move);   index = r.indexIn(nextS);
            if (index == -1) { nextS.add(r); } else { nextS.remove(index); }
            // evaluate
            if (!nextS.isEmpty()) {
                cost = cost(nextS, numCellFigures);
                if (cost < bestCost) { bestCost = cost;   bestMove = move; }
            }
            // restore nextS
            if (index == -1) { nextS.remove(nextS.size()-1); } else { nextS.add(r); }
        }
        if (bestMove == -1) { return -1; }
        // perform best move
        r = availableRules.get(bestMove);   index = r.indexIn(S);
        if (index == -1) { S.add(r); } else { S.remove(index); }
        return bestCost;
    }

    public static int goToBestNeighbor(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int numCellFigures, TabuList tabuList, int aspiration) {
        if (numCellFigures < 1) { throw new IllegalArgumentException(); }
        ArrayList<Rule> nextS = new ArrayList<Rule>(S.size()+1);
        int move, index, cost, bestCost = Integer.MAX_VALUE, bestMove = -1;
        nextS.clear();   nextS.addAll(S);   Rule r;
        for (move = 0; move < availableRules.size(); move++) {
            // move
            r = availableRules.get(move);   index = r.indexIn(nextS);
            if (index == -1) { nextS.add(r); } else { nextS.remove(index); }
            // evaluate
            if (!nextS.isEmpty()) {
                cost = cost(nextS, numCellFigures);
                if (tabuList.contains(move)) {
                    if ( (cost < aspiration) && (cost < bestCost) ) {
                        bestCost = cost;   bestMove = move;
                    }
                } else if (cost < bestCost) {
                    bestCost = cost;   bestMove = move;
                }
            }
            // restore nextS
            if (index == -1) { nextS.remove(nextS.size()-1); } else { nextS.add(r); }
        }
        if (bestMove == -1) { return -1; }
        // perform best move
        r = availableRules.get(bestMove);   index = r.indexIn(S);
        if (index == -1) { S.add(r); } else { S.remove(index); }
        tabuList.add(bestMove);   return bestCost;
    }

    public static void getInitialState(ArrayList<Rule> S, ArrayList<Rule> availableRules,
    int maxRules, Random rand) {
        if (maxRules < 1) { throw new IllegalArgumentException(); }
        S.clear();   int size = 1 + rand.nextInt(maxRules);   // size in [1,maxRules]
        if (availableRules.size() <= size) { S.addAll(availableRules);   return; }
        // create array of indices
        int i, maxIndex = availableRules.size();   int[] indices = new int[maxIndex];
        for (i = 0; i < maxIndex; i++) { indices[i] = i; }
        // select from array
        Rule r;   int tmp;
        while (S.size() < size) {
            i = rand.nextInt(maxIndex);   r = availableRules.get(indices[i]);
            S.add(r);   maxIndex--;
            tmp = indices[maxIndex];   indices[maxIndex] = indices[i];   indices[i] = tmp;
        }
    }

    public static void getRandomNeighbor(ArrayList<Rule> S1, ArrayList<Rule> S2,
    ArrayList<Rule> availableRules, int maxRules, int[] availableIndices, Random rand) {
        if (maxRules < 1) { throw new IllegalArgumentException(); }
        if ( (S1.size() < availableRules.size()) && (S1.size() < maxRules)
        && ((S1.size() <= 1) || rand.nextBoolean()) ) {
            // add a rule
            int tmp, maxIndex = availableIndices.length, index = rand.nextInt(maxIndex);
            Rule r = availableRules.get(availableIndices[index]);
            boolean valid = (r.indexIn(S1) == -1);
            while ( (maxIndex > 1) && !valid ) {
                maxIndex--;   tmp = availableIndices[index];
                availableIndices[index] = availableIndices[maxIndex];
                availableIndices[maxIndex] = tmp;
                index = rand.nextInt(maxIndex);
                r = availableRules.get(availableIndices[index]);
                valid = (r.indexIn(S1) == -1);
            }
            if (valid) { S2.clear();   S2.addAll(S1);   S2.add(r);   return; }
        }
        // remove a rule
        if (S1.size() <= 1) { throw new RuntimeException("No available neighbors"); }
        int i, index = rand.nextInt(S1.size());   S2.clear();
        for (i = 0; i < S1.size(); i++) { if (i != index) { S2.add(S1.get(i)); } }
    }

    // private methods ---------------------------------------------------------

    private static int costFunction(int positiveOverlap, int leftovers,
    int positiveLeftovers, int irregularity, int operands, int parameters, int absNegatives,
    int falsePositives, int falseNegatives, int absFalsePositives, int absFalseNegatives) {
        if ( (positiveOverlap < 0) || (leftovers < 0) || (positiveLeftovers < 0)
        || (irregularity < 0) || (operands < 0) || (parameters < 0)
        || (falsePositives < 0) || (falseNegatives < 0)
        || (absFalsePositives < 0) || (absFalseNegatives < 0) ) {
            throw new IllegalArgumentException("Negative argument");
        }
        return positiveOverlap + 15*leftovers + 5*positiveLeftovers + 3*irregularity
                + 2*operands + 2*parameters + 0*absNegatives
                + 2*falsePositives + 0*falseNegatives
                + absFalsePositives + absFalseNegatives
                + positiveLeftovers*positiveOverlap;
    }

}
