package rule;

import basic.CoveredFigures;
import basic.Diagram;
import basic.Figure;
import basic.Problem;
import optimization.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class RuleSelector {
    public static final int STEEPEST_DESCENT = 0, TABU_SEARCH = 1;
    public static final int SIMULATED_ANNEALING = 2, RANDOM = 3;
    private Problem problem;
    private CoveredFigures coveredFigures;
    private ArrayList<Rule> selectedRules;
    private int level, maxRules;
    private ArrayList<Rule> availableRules, newRules, rulesToExpand;
    private RuleList[] hEqualities, vEqualities, hNewRules, vNewRules;
    private RuleComparator ruleComparator;
    private StringBuilder report;

    public RuleSelector(Problem p, int maxRules) {
        if (maxRules < 1) { throw new IllegalArgumentException(); }
        problem = p;   level = 1;   this.maxRules = maxRules;
        coveredFigures = new CoveredFigures(problem.numCellFigures);
        selectedRules = new ArrayList<Rule>();
        availableRules = new ArrayList<Rule>();
        newRules = new ArrayList<Rule>();
        rulesToExpand = new ArrayList<Rule>();
        ruleComparator = new RuleComparator();
        // create lists
        int i;
        hEqualities = new RuleList[problem.cells.length];
        hNewRules = new RuleList[hEqualities.length];
        for (i = 0; i < hEqualities.length; i++) {
            hEqualities[i] = new RuleList();   hNewRules[i] = new RuleList();
        }
        vEqualities = new RuleList[problem.cells[0].length];
        vNewRules = new RuleList[vEqualities.length];
        for (i = 0; i < vEqualities.length; i++) {
            vEqualities[i] = new RuleList();   vNewRules[i] = new RuleList();
        }
    }

    public CoveredFigures getCoveredFigures() {
        return coveredFigures;
    }

    public ArrayList<Rule> getSelectedRules() {
        return selectedRules;
    }

    public ArrayList<Rule> getAvailableRules() {
        return availableRules;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Must be called always after the selectedRules of this object are modified externally,
     * so that the coveredFigures and the report are updated with the new rules.
     */
    public void updateSelectedRules() {
        coveredFigures.clear();   report = new StringBuilder();   Rule r;
        for (int i = 0; i < selectedRules.size(); i++) {
            r = selectedRules.get(i);   coveredFigures.add(r.coveredFigures);
            report.append(r).append("\n");
        }
        report.append("Covered ").append(coveredFigures.getCoverage());
        report.append("/").append(problem.numCellFigures).append(" cell figures\n");
    }

    public void stingySelection() {
        generateRules(true);   coveredFigures.clear();   selectedRules.clear();
        if (availableRules.isEmpty()) { return; }
        Collections.sort(availableRules, ruleComparator);
        report = new StringBuilder();
        int i = 0;   Rule r;
        do {
            r = availableRules.get(i);
            if (r.coveredFigures.getAdditionalPositiveCoverage(coveredFigures) > 0) {
                selectedRules.add(r);   coveredFigures.add(r.coveredFigures);
                report.append(r).append("\n");
            }
            i++;
        } while ( (i < availableRules.size()) && (selectedRules.size() < maxRules)
                && (coveredFigures.getPositiveCoverage() < problem.numCellFigures) );
        report.append("Covered ").append(coveredFigures.getCoverage());
        report.append("/").append(problem.numCellFigures).append(" cell figures\n");
    }

    public void greedySelection() {
        int coverage = 0, i, inc, maxInc;
        generateRules(true);   coveredFigures.clear();   selectedRules.clear();
        if (availableRules.isEmpty()) { return; }
        Rule bestRule;   boolean done = false;
        report = new StringBuilder();
        do {
            bestRule = null;   maxInc = 0;
            for (i = 0; i < availableRules.size(); i++) {
                inc = availableRules.get(i).coveredFigures.getAdditionalPositiveCoverage(coveredFigures);
                if (inc > maxInc) {
                    maxInc = inc;   bestRule = availableRules.get(i);
                } else if ( (inc > 0) && (inc == maxInc)
                && (ruleComparator.compare(availableRules.get(i), bestRule) < 0) ) {
                    bestRule = availableRules.get(i);
                }
            }
            if (maxInc == 0) { done = true; }
            else {
                selectedRules.add(bestRule);   coverage += maxInc;
                coveredFigures.add(bestRule.coveredFigures);
                report.append(bestRule).append("; coverageInc: ").append(maxInc).append("\n");
                if ( (selectedRules.size() == maxRules)
                || (coverage == problem.numCellFigures) ) { done = true; }
            }
        } while (!done);
        report.append("Covered ").append(coveredFigures.getCoverage());
        report.append("/").append(problem.numCellFigures).append(" cell figures\n");
    }

    public void optimalSelection(int method) {
        if ( (method != STEEPEST_DESCENT) && (method != TABU_SEARCH)
        && (method != SIMULATED_ANNEALING) && (method != RANDOM) ) {
            throw new IllegalArgumentException(); }
        generateRules(method != RANDOM);
        coveredFigures.clear();   selectedRules.clear();
        if (availableRules.isEmpty()) { return; }
        if (method == STEEPEST_DESCENT) {
            SteepestDescent.run(selectedRules, availableRules, maxRules,
                    problem.numCellFigures, (new Date()).getTime());
        } else if (method == TABU_SEARCH) {
            int tabuSize = 20, maxIter = 100;
            TabuSearch.run(selectedRules, availableRules, maxRules,
                    problem.numCellFigures, tabuSize, maxIter, (new Date()).getTime());
        } else if (method == SIMULATED_ANNEALING) {
            float T0 = -Utils.maxCost(maxRules, problem.numCellFigures, problem.cells.length,
                    problem.cells[0].length) / (float) Math.log(0.99);
            int maxIter = 10000, coolingSteps = 1000, coolingIter = maxIter / coolingSteps;
            // Tf = T0 * pow(u, coolingSteps-1), then u = pow(Tf/T0, 1/(coolingSteps-1))
            float Tf = 0.0000000001f, u = (float) Math.pow(Tf/T0, 1f/(coolingSteps-1));
            SimulatedAnnealing.run(selectedRules, availableRules, maxRules,
                    problem.numCellFigures, T0, u, coolingIter, maxIter, (new Date()).getTime());
        } else if (method == RANDOM) {
            Utils.getInitialState( selectedRules, availableRules, maxRules,
                    new Random((new Date()).getTime()) );
        } else { throw new IllegalArgumentException(); }
        // report
        report = new StringBuilder();   Rule r;
        for (int i = 0; i < selectedRules.size(); i++) {
            r = selectedRules.get(i);   coveredFigures.add(r.coveredFigures);
            report.append(r).append("\n");
        }
        report.append("Covered ").append(coveredFigures.getCoverage());
        report.append("/").append(problem.numCellFigures).append(" cell figures\n");
    }

    @Override
    public String toString() {
        if (selectedRules.isEmpty()) { return "No selected rules\n"; }
        if (report == null) { return "No generated report\n"; }
        return report.toString();
    }

    // private methods ---------------------------------------------------------
    
    private void generateRules(boolean sortNewRules) {
        if (level == 1) { createEqualities();   level++;   return; }
        // select the rules to be expanded
        int i;
        rulesToExpand.clear();
        for (i = 0; i < selectedRules.size(); i++) {
            if (selectedRules.get(i).level == level) {
                rulesToExpand.add(selectedRules.get(i));
            }
        }
        if (rulesToExpand.isEmpty()) {
            if (sortNewRules) { Collections.sort(newRules, ruleComparator); }
            else { Collections.shuffle(newRules); }
            i = 0;
            while ( (rulesToExpand.size() < maxRules) && (i < newRules.size()) ) {
                if (newRules.get(i).indexIn(rulesToExpand) == -1) {
                    rulesToExpand.add(newRules.get(i));
                }
                i++;
            }
            if (rulesToExpand.isEmpty()) { return; }
        }
        // clear hNewRules and vNewRules
        for (i = 0; i < hNewRules.length; i++) { hNewRules[i].list.clear(); }
        for (i = 0; i < vNewRules.length; i++) { vNewRules[i].list.clear(); }
        // expand rules to fill hNewRules and vNewRules
        Rule r;
        for (i = 0; i < rulesToExpand.size(); i++) {
            r = rulesToExpand.get(i);
            if (r.getType() == Rule.TYPE_HORIZONTAL) {
                expandAndListH(r);   expandAndNotListH(r);
            } else {
                expandAndListV(r);   expandAndNotListV(r);
            }
        }
        // update newRules
        newRules.clear();
        for (i = 0; i < hNewRules.length; i++) {
            newRules.addAll(hNewRules[i].list);
        }
        for (i = 0; i < vNewRules.length; i++) {
            newRules.addAll(vNewRules[i].list);
        }
        level++;   initializeNewRules(level);   availableRules.addAll(newRules);
    }
    
    private void expandAndListH(Rule r) {
        Rule newRule;   Transformation T;   int i;
        ArrayList<Rule> resultEquals = hEqualities[r.row].list;
        for (i = 0; i < resultEquals.size(); i++) {
            T = resultEquals.get(i).andList.get(0);
            if (!r.hasOperand(T)) {
                newRule = (Rule) r.clone();   newRule.andList.add(T);
                if (!hNewRules[r.row].list.contains(newRule)) {
                    newRule.evaluate(problem, newRule.coveredFigures, false);
                    if (newRule.coveredFigures.getPositiveCoverage() > 0) {
                        hNewRules[r.row].list.add(newRule);
                    }
                }
            }
        }
    }

    private void expandAndListV(Rule r) {
        Rule newRule;   Transformation T;   int i;
        ArrayList<Rule> resultEquals = vEqualities[r.col].list;
        for (i = 0; i < resultEquals.size(); i++) {
            T = resultEquals.get(i).andList.get(0);
            if (!r.hasOperand(T)) {
                newRule = (Rule) r.clone();   newRule.andList.add(T);
                if (!vNewRules[r.col].list.contains(newRule)) {
                    newRule.evaluate(problem, newRule.coveredFigures, false);
                    if (newRule.coveredFigures.getPositiveCoverage() > 0) {
                        vNewRules[r.col].list.add(newRule);
                    }
                }
            }
        }
    }

    private void expandAndNotListH(Rule r) {
        Rule newRule;   Transformation operand, T, T2;   int i, j;
        ArrayList<Rule> operandEquals;
        for (i = 0; i < r.andList.size(); i++) {
            operand = r.andList.get(i);
            operandEquals = hEqualities[operand.row].list;
            for (j = 0; j < operandEquals.size(); j++) {
                T = operandEquals.get(j).andList.get(0);
                T2 = (Transformation) T.clone();   T2.col += operand.col;
                if (T2.col >= problem.cells[0].length) { T2.col -= problem.cells[0].length; }
                operand.transform(T2);
                if (!r.hasOperand(T2)) {
                    newRule = (Rule) r.clone();   newRule.andNotList.add(T2);
                    if (!hNewRules[r.row].list.contains(newRule)) {
                        newRule.evaluate(problem, newRule.coveredFigures, false);
                        if (newRule.coveredFigures.getPositiveCoverage() > 0) {
                            hNewRules[r.row].list.add(newRule);
                        }
                    }
                }
            }
        }
    }

    private void expandAndNotListV(Rule r) {
        Rule newRule;   Transformation operand, T, T2;   int i, j;
        ArrayList<Rule> operandEquals;
        for (i = 0; i < r.andList.size(); i++) {
            operand = r.andList.get(i);
            operandEquals = vEqualities[operand.col].list;
            for (j = 0; j < operandEquals.size(); j++) {
                T = operandEquals.get(j).andList.get(0);
                T2 = (Transformation) T.clone();   T2.row += operand.row;
                if (T2.row >= problem.cells.length) { T2.row -= problem.cells.length; }
                operand.transform(T2);
                if (!r.hasOperand(T2)) {
                    newRule = (Rule) r.clone();   newRule.andNotList.add(T2);
                    if (!vNewRules[r.col].list.contains(newRule)) {
                        newRule.evaluate(problem, newRule.coveredFigures, false);
                        if (newRule.coveredFigures.getPositiveCoverage() > 0) {
                            vNewRules[r.col].list.add(newRule);
                        }
                    }
                }
            }
        }
    }

    private void createEqualities() {
        int numCols = problem.cells[0].length, numCells = problem.cells.length*numCols;
        int i, j, k, m;
        Diagram diag1, diag2;
        Figure fig1, fig2;
        boolean postMode = problem.missingCells == null;
        // clear matrix
        if (!postMode) { for (i = 0; i < problem.missingCells.length; i++) {
                j = problem.missingCells[i].row;
                k = problem.missingCells[i].col;
                problem.cells[j][k] = null;
        } }
        // create equalities (empty and missing diagrams are ignored)
        for (i = 0; i <= numCells-2; i++) {
            diag1 = problem.cells[i/numCols][i%numCols];
            if ( (diag1 != null) && (!diag1.figures.isEmpty()) ) {
                for (j = i+1; j <= numCells-1; j++) {
                    diag2 = problem.cells[j/numCols][j%numCols];
                    if ( (diag2 != null) && (!diag2.figures.isEmpty()) ) {
                        for (k = 0; k < diag1.figures.size(); k++) {
                            fig1 = diag1.figures.get(k);
                            for (m = 0; m < diag2.figures.size(); m++) {
                                fig2 = diag2.figures.get(m);
                                // transform fig1 into fig2, if possible
                                createEquality(fig1,fig2);
                            }
                        }
                    }
                }
            }
        }
        // fill availableRules and newRules
        Rule r;   newRules.clear();
        for (i = 0; i < hEqualities.length; i++) {
            for (j = 0; j < hEqualities[i].list.size(); j++) {
                r = hEqualities[i].list.get(j);
                if (postMode || r.coversAnyCell(problem.missingCells)) {
                    r.evaluate(problem, r.coveredFigures, false);
                    newRules.add(r);
                }
            }
        }
        for (i = 0; i < vEqualities.length; i++) {
            for (j = 0; j < vEqualities[i].list.size(); j++) {
                r = vEqualities[i].list.get(j);
                if (postMode || r.coversAnyCell(problem.missingCells)) {
                    r.evaluate(problem, r.coveredFigures, false);
                    newRules.add(r);
                }
            }
        }
        initializeNewRules(2);   availableRules.addAll(newRules);
    }

    private void createEquality(Figure fig1, Figure fig2) {
        if ( !fig1.getName().equals(fig2.getName()) ) { return; }
        int dx = 0, dy = 0;   float sx = 1, sy = 1, r = 0;
        boolean reflectX = false, different = false;
        // translate
        if ( (Math.abs(fig2.getPositionX()-fig1.getPositionX()) > Figure.POSITION_THRESHOLD)
        || (Math.abs(fig2.getPositionY()-fig1.getPositionY()) > Figure.POSITION_THRESHOLD) ) {
            dx = fig2.getPositionX() - fig1.getPositionX();
            dy = fig2.getPositionY() - fig1.getPositionY();   different = true;
        }
        // scale
        if ( (Math.abs(fig2.getScaleX()-fig1.getScaleX()) > Figure.SCALE_THRESHOLD)
        || (Math.abs(fig2.getScaleY()-fig1.getScaleY()) > Figure.SCALE_THRESHOLD) ) {
            sx = fig2.getScaleX() / fig1.getScaleX();
            sy = fig2.getScaleY() / fig1.getScaleY();   different = true;
        }
        // rotate
        if ( Math.abs(fig2.getRotation()-fig1.getRotation()) > Figure.ROTATION_THRESHOLD ) {
            r = fig2.getRotation() - fig1.getRotation();   different = true;
        }
        // reflectX
        if (fig2.getReflectX() != fig1.getReflectX()) {
            reflectX = true;   different = true;
        }
        // create equality diag2 = T(diag1)
        Transformation T = new Transformation(fig1.getDiagram());
        if (different) { T.setParameters(dx, dy, sx, sy, r, reflectX); }
        saveEquality(fig2.getDiagram(), T);
        // create symmetric equality diag1 = T(diag2)
        T = new Transformation(fig2.getDiagram());
        if (different) { T.setParameters(-dx, -dy, 1/sx, 1/sy, -r, reflectX); }
        saveEquality(fig1.getDiagram(), T);
    }

    private void saveEquality(Diagram result, Transformation T) {
        // save horizontal equality (the result is shifted to the first column)
        Transformation Th = (Transformation) T.clone();
        Th.col -= result.col;
        if (Th.col < 0) { Th.col += problem.cells[0].length; }
        Rule r = new Rule(result.row, 0, Th, problem.numCellFigures, Rule.TYPE_HORIZONTAL);
        int ruleIndex = hEqualities[result.row].list.indexOf(r);
        if (ruleIndex < 0) { hEqualities[result.row].list.add(r); }
        // save vertical equality (the result is shifted to the first row)
        T.row -= result.row;
        if (T.row < 0) { T.row += problem.cells.length; }
        r = new Rule(0, result.col, T, problem.numCellFigures, Rule.TYPE_VERTICAL);
        ruleIndex = vEqualities[result.col].list.indexOf(r);
        if (ruleIndex < 0) { vEqualities[result.col].list.add(r); }
    }

    private void initializeNewRules(int currentLevel) {
        ArrayList<Rule> tmpList = new ArrayList<Rule>(1);   Rule r;
        for (int i = 0; i < newRules.size(); i++) {
            r = newRules.get(i);   tmpList.clear();   tmpList.add(r);
            r.cost = Utils.cost(tmpList, problem.numCellFigures);
            r.level = currentLevel;
        }
    }

    private boolean debug() {
        selectedRules.clear();   availableRules.clear();
        // (0,3) = (0,0)
        Diagram d = new Diagram();   d.row = 0;   d.col = 0;
        Transformation T = new Transformation(d);
        Rule r = new Rule(0,3, T, problem.numCellFigures, Rule.TYPE_VERTICAL);
        // and (0,1) rotate -45
        T = new Transformation(d);   T.col = 1;
        T.setParameters(0,0, 1f,1f, -45f, false);   r.andList.add(T);
        // andNot (0,2)
        T = new Transformation(d);   T.col = 2;   r.andNotList.add(T);
        // evaluate
        r.evaluate(problem, r.coveredFigures, false);
        availableRules.add(r);   selectedRules.add(r);
        updateSelectedRules();   return true;
    }

}
