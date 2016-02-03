package answer;

import basic.Diagram;
import basic.Problem;
import basic.Solution;
import optimization.*;
import rule.Rule;
import rule.RuleSelector;
import java.util.Arrays;

public class PostAnswerSelector {
    private static final int MAX_RULES = 20;
    private Problem problem;
    private RuleSelector bestRuleSelector, currentRuleSelector;
    private Diagram[] bestSolution, currentSolution;
    private Counter counter;
    private StringBuilder report;
    private boolean tiedScores, solved;
    private int level;

    public PostAnswerSelector(Problem p) {
        problem = p;   bestRuleSelector = null;   currentRuleSelector = null;
        bestSolution = new Diagram[problem.missingCells.length];
        Arrays.fill(bestSolution, null);
        currentSolution = new Diagram[problem.missingCells.length];
        for (int i = 0; i < currentSolution.length; i++) {
            currentSolution[i] = new Diagram();
        }
        counter = new Counter(problem.missingCells.length, problem.answers.length-1);
        tiedScores = true;   solved = false;   level = 1;
    }

    public void solveNextLevel() {
        level++;   select();
    }

    public void setLevel(int i) {
        if (i < 1) { throw new IllegalArgumentException("Invalid level"); }
        level = i;   solved = false;
    }
    
    public boolean isCorrect(Solution correctSolution) {
        if (!solved) {
            throw new RuntimeException("The problem has not been solved yet");
        }
        if ( (!correctSolution.id.equals(problem.id))
        || (correctSolution.values.length != bestSolution.length ) ) {
            throw new IllegalArgumentException("Invalid solution for problem \"" + problem.id + "\"");
        }
        if (tiedScores || (bestSolution[0] == null)) { return false; }
        for (int i = 0; i < correctSolution.values.length; i++) {
            if (correctSolution.values[i] != bestSolution[i].col+1) { return false; }
        }
        return true;
    }

    @Override
    public String toString() {
        if (!solved) { return "The problem has not been solved yet\n"; }
        StringBuilder sb = new StringBuilder();
        sb.append("Level ").append(level).append("\n");
        if (bestRuleSelector == null) {
            sb.append("No selected rules\n");   return sb.toString();
        }
        sb.append(Rule.toString(bestRuleSelector.getSelectedRules()));
        sb.append("Selected solution: ");
        sb.append("(").append(problem.missingCells[0].row);
        sb.append(",").append(problem.missingCells[0].col).append(")|");
        sb.append(bestSolution[0].col+1);
        for (int i = 1; i < problem.missingCells.length; i++) {
            sb.append(", (").append(problem.missingCells[i].row);
            sb.append(",").append(problem.missingCells[i].col).append(")|");
            sb.append(bestSolution[i].col+1);
        }
        sb.append("\nTied scores: ").append(tiedScores).append("\n").append(report.toString());
        return sb.toString();
    }

    // private methods ---------------------------------------------------------

    private void select() {
        Arrays.fill(bestSolution, null);
        int i, j, bestCost = Integer.MAX_VALUE, currentCost, tmpNumCellFigures;
        boolean stop;   Diagram[] tmpMissingCells;
        StringBuilder currentCostString = new StringBuilder();
        // evaluate each possible solution
        counter.clear();   tiedScores = false;   report = new StringBuilder();
        int permutations = (int) Math.pow(problem.answers.length, problem.missingCells.length);
        int iteration = 1;
        do {
            System.out.println("\tSolution " + iteration + "/" + permutations);
            // complete matrix
            tmpNumCellFigures = problem.numCellFigures;
            for (i = 0; i < problem.missingCells.length; i++) {
                currentSolution[i] = (Diagram) problem.answers[ counter.getDigit(i) ].clone();
                currentSolution[i].row = problem.missingCells[i].row;
                currentSolution[i].col = problem.missingCells[i].col;
                for (j = 0; j < currentSolution[i].figures.size(); j++) {
                    currentSolution[i].figures.get(j).setIndex(problem.numCellFigures);
                    problem.numCellFigures++;
                }
                problem.cells[currentSolution[i].row][currentSolution[i].col] = currentSolution[i];
            }
            tmpMissingCells = problem.missingCells;   problem.missingCells = null;
            // evaluate
            currentRuleSelector = new RuleSelector(problem, MAX_RULES);
            for (i = 2; i <= level; i++) {
                currentRuleSelector.greedySelection();
                SteepestDescent.run(currentRuleSelector.getSelectedRules(),
                        currentRuleSelector.getSelectedRules(), MAX_RULES,
                        problem.numCellFigures);
                currentRuleSelector.updateSelectedRules();
            }
            currentCost = Utils.cost(currentRuleSelector.getSelectedRules(),
                    problem.numCellFigures, currentCostString);
            // restore matrix
            problem.numCellFigures = tmpNumCellFigures;
            problem.missingCells = tmpMissingCells;
            // update best solution
            if (currentCost < bestCost) {
                bestCost = currentCost;   bestRuleSelector = currentRuleSelector;
                for (i = 0; i < bestSolution.length; i++) {
                    bestSolution[i] = problem.answers[ counter.getDigit(i) ];
                }
                tiedScores = false;
            } else if (currentCost == bestCost) { tiedScores = true; }
            // report
            report.append("\tSolution (").append(counter);
            report.append("), Cost: ").append(currentCost).append(" (");
            report.append(currentCostString).append(")\n");
            // next list
            stop = counter.inc();   iteration++;
        } while (!stop);
        solved = true;
    }

}
