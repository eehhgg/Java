package answer;

import basic.CoveredFigures;
import basic.Diagram;
import basic.Problem;
import basic.Solution;
import rule.Rule;
import rule.RuleSelector;
import java.util.ArrayList;
import java.util.Arrays;

public class PreAnswerSelector {
    private static final int MAX_RULES = 20;
    private RuleSelector ruleSelector;
    private Problem problem;
    private Diagram[] selectedSolution, currentSolution;
    private Counter counter;
    private StringBuilder report;
    private boolean tiedScores;

    public PreAnswerSelector(Problem p) {
        ruleSelector = new RuleSelector(p, MAX_RULES);   problem = p;
        selectedSolution = new Diagram[problem.missingCells.length];
        Arrays.fill(selectedSolution, null);
        currentSolution = new Diagram[problem.missingCells.length];
        counter = new Counter(problem.missingCells.length, problem.answers.length-1);
        tiedScores = true;
    }

    public void solveNextLevel() {
        ruleSelector.greedySelection();   select();
    }

    public boolean isCorrect(Solution solution) {
        if ( (!solution.id.equals(problem.id))
        || (solution.values.length != selectedSolution.length ) ) {
            throw new IllegalArgumentException("Invalid solution for problem \"" + problem.id + "\"");
        }
        if (tiedScores || (selectedSolution[0] == null)) { return false; }
        for (int i = 0; i < solution.values.length; i++) {
            if (solution.values[i] != selectedSolution[i].col+1) { return false; }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Level ").append(ruleSelector.getLevel()).append("\n");
        if (ruleSelector.getSelectedRules().isEmpty()) {
            sb.append("No selected rules\n");   return sb.toString();
        }
        sb.append(ruleSelector);
        sb.append("Selected solution: ");
        sb.append("(").append(problem.missingCells[0].row);
        sb.append(",").append(problem.missingCells[0].col).append(")|");
        sb.append(selectedSolution[0].col+1);
        for (int i = 1; i < problem.missingCells.length; i++) {
            sb.append(", (").append(problem.missingCells[i].row);
            sb.append(",").append(problem.missingCells[i].col).append(")|");
            sb.append(selectedSolution[i].col+1);
        }
        sb.append("\nTied scores: ").append(tiedScores).append("\n").append(report.toString());
        return sb.toString();
    }

    // private methods ---------------------------------------------------------

    private void select() {
        Arrays.fill(selectedSolution, null);
        if (ruleSelector.getSelectedRules().isEmpty()) { return; }
        int i, cmp;   boolean stop;
        int[] bestScore = new int[3], currentScore = new int[3];
        // evaluate each possible solution
        counter.clear();   Arrays.fill(bestScore, -1);
        tiedScores = false;   report = new StringBuilder();
        do {
            // define list
            for (i = 0; i < problem.missingCells.length; i++) {
                currentSolution[i] = problem.answers[ counter.getDigit(i) ];
                problem.cells[problem.missingCells[i].row][problem.missingCells[i].col] = currentSolution[i];
            }
            // evaluate
            evaluateSolution(currentScore);
            cmp = compareScores(currentScore, bestScore);
            if (cmp > 0) {
                System.arraycopy(currentScore,0, bestScore,0, bestScore.length);
                System.arraycopy(currentSolution,0, selectedSolution,0, problem.missingCells.length);
                tiedScores = false;
            } else if (cmp == 0) { tiedScores = true; }
            // report
            report.append("\tSolution (").append(counter);
            report.append("), Score (").append(currentScore[0]).append(",");
            report.append(currentScore[1]).append(",").append(currentScore[2]).append(")\n");
            // next list
            stop = counter.inc();
        } while (!stop);
    }
    
    private void evaluateSolution(int[] score) {
        int i;
        CoveredFigures coveredF = new CoveredFigures(problem.numCellFigures);
        ArrayList<Rule> selectedRules = ruleSelector.getSelectedRules();
        for (i = 0; i < selectedRules.size(); i++) {
            // evaluate the selected rules with the missing cells
            selectedRules.get(i).evaluate(problem, coveredF, true);
        }
        score[0] = coveredF.getAdditionalCoverage(ruleSelector.getCoveredFigures());
        score[1] = coveredF.getAbsoluteCoverage();
        score[2] = 0;   Diagram d;
        for (i = 0; i < problem.missingCells.length; i++) {
            d = problem.cells[problem.missingCells[i].row][problem.missingCells[i].col];
            score[2] += d.figures.size();
        }
    }

    private static int compareScores(int[] s1, int[] s2) {
        // more coverage
        if (s1[0] > s2[0]) { return 1; }
        if (s1[0] < s2[0]) { return -1; }
        // more satisfied rules
        if (s1[1] > s2[1]) { return 1; }
        if (s1[1] < s2[1]) { return -1; }
        // less number of figures
        if (s1[2] > s2[2]) { return -1; }
        if (s1[2] < s2[2]) { return 1; }
        return 0;
    }

}
