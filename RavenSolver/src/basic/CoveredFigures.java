package basic;

import java.util.Arrays;

public class CoveredFigures {
    private boolean[] positive, negative, falsePositive, falseNegative;
    private int nPositive, nNegative, nFalsePositive, nFalseNegative;
    private int nAbsolutePositive, nAbsoluteNegative;
    private int nAbsoluteFalsePositive, nAbsoluteFalseNegative, length;

    public CoveredFigures(int n) {
        if (n < 1) { throw new IllegalArgumentException(); }
        length = n;   positive = new boolean[n];   negative = new boolean[n];
        falsePositive = new boolean[n];   falseNegative = new boolean[n];   clear();
    }

    public void clear() {
        Arrays.fill(positive, false);   Arrays.fill(negative, false);
        Arrays.fill(falsePositive, false);   Arrays.fill(falseNegative, false);
        nPositive = 0;   nNegative = 0;
        nFalsePositive = 0;   nFalseNegative = 0;
        nAbsolutePositive = 0;   nAbsoluteNegative = 0;
        nAbsoluteFalsePositive = 0;   nAbsoluteFalseNegative = 0;
    }

    public int length() {
        return length;
    }

    public int getCoverage() {
        return nPositive + nNegative;
    }

    public int getPositiveCoverage() {
        return nPositive;
    }

    public int getNegativeCoverage() {
        return nNegative;
    }

    public int getAbsoluteCoverage() {
        return nAbsolutePositive + nAbsoluteNegative;
    }

    public int getAbsolutePositiveCoverage() {
        return nAbsolutePositive;
    }

    public int getAbsoluteNegativeCoverage() {
        return nAbsoluteNegative;
    }

    public int getErrors() {
        return nFalsePositive + nFalseNegative;
    }

    public int getFalsePositives() {
        return nFalsePositive;
    }

    public int getFalseNegatives() {
        return nFalseNegative;
    }

    public int getAbsoluteErrors() {
        return nAbsoluteFalsePositive + nAbsoluteFalseNegative;
    }

    public int getAbsoluteFalsePositives() {
        return nAbsoluteFalsePositive;
    }

    public int getAbsoluteFalseNegatives() {
        return nAbsoluteFalseNegative;
    }

    public void addPositive(Diagram d) {
        for (int i = 0; i < d.figures.size(); i++) { addPositive(d.figures.get(i).getIndex()); }
    }

    public void addNegative(Diagram d) {
        for (int i = 0; i < d.figures.size(); i++) { addNegative(d.figures.get(i).getIndex()); }
    }

    public void addPositive(Figure f) {
        addPositive(f.getIndex());
    }

    public void addNegative(Figure f) {
        addNegative(f.getIndex());
    }

    public void addFalsePositive(Diagram d) {
        for (int i = 0; i < d.figures.size(); i++) { addFalsePositive(d.figures.get(i).getIndex()); }
    }

    public void addFalseNegative(Diagram d) {
        for (int i = 0; i < d.figures.size(); i++) { addFalseNegative(d.figures.get(i).getIndex()); }
    }

    public void addFalsePositive(Figure f) {
        addFalsePositive(f.getIndex());
    }

    public void addFalseNegative(Figure f) {
        addFalseNegative(f.getIndex());
    }

    public void add(CoveredFigures cf) {
        if (cf.length() != length) {
            throw new IllegalArgumentException("Incompatible arrays");
        }
        for (int i = 0; i < length; i++) {
            if (cf.positive[i]) { addPositive(i); }
            else if (cf.negative[i]) { addNegative(i); }
            else if (cf.falsePositive[i]) { addFalsePositive(i); }
            else if (cf.falseNegative[i]) { addFalseNegative(i); }
        }
    }

    public void copy(CoveredFigures cf) {
        if (cf.length() != length) {
            throw new IllegalArgumentException("Incompatible arrays");
        }
        System.arraycopy(cf.positive,0 , positive,0, length);
        System.arraycopy(cf.negative,0 , negative,0, length);
        System.arraycopy(cf.falsePositive,0 , falsePositive,0, length);
        System.arraycopy(cf.falseNegative,0 , falseNegative,0, length);
        nPositive = cf.nPositive;   nNegative = cf.nNegative;
        nFalsePositive = cf.nFalsePositive;   nFalseNegative = cf.nFalseNegative;
        nAbsolutePositive = cf.nAbsolutePositive;
        nAbsoluteNegative = cf.nAbsoluteNegative;
        nAbsoluteFalsePositive = cf.nAbsoluteFalsePositive;
        nAbsoluteFalseNegative = cf.nAbsoluteFalseNegative;
    }

    public int getAdditionalCoverage(CoveredFigures cf) {
        int i, count = 0;
        if (cf.length() != length) {
            throw new IllegalArgumentException("Incompatible arrays");
        }
        if (cf.getCoverage() == 0) { return getCoverage(); }
        for (i = 0; i < length; i++) {
            if ( covers(i) && !cf.covers(i) ) { count++; }
        }
        return count;
    }

    public int getAdditionalPositiveCoverage(CoveredFigures cf) {
        int i, count = 0;
        if (cf.length() != length) {
            throw new IllegalArgumentException("Incompatible arrays");
        }
        if (cf.getPositiveCoverage() == 0) { return getPositiveCoverage(); }
        for (i = 0; i < length; i++) {
            if ( positive[i] && !cf.positive[i] ) { count++; }
        }
        return count;
    }

    // private methods ---------------------------------------------------------

    private boolean covers(int i) {
        if (i < 0) { return false; }
        return (positive[i] || negative[i]);
    }

    private boolean error(int i) {
        if (i < 0) { return false; }
        return (falsePositive[i] || falseNegative[i]);
    }

    private void addPositive(int i) {
        if (i < 0) { return; }
        if (!positive[i]) {
            positive[i] = true;   nPositive++;
            if (negative[i]) { negative[i] = false;   nNegative--; }
            else if (falsePositive[i]) { falsePositive[i] = false;   nFalsePositive--; }
            else if (falseNegative[i]) { falseNegative[i] = false;   nFalseNegative--; }
        }
        nAbsolutePositive++;
    }

    private void addNegative(int i) {
        if (i < 0) { return; }
        if (!covers(i)) {
            negative[i] = true;   nNegative++;
            if (falsePositive[i]) { falsePositive[i] = false;   nFalsePositive--; }
            else if (falseNegative[i]) { falseNegative[i] = false;   nFalseNegative--; }
        }
        nAbsoluteNegative++;
    }

    private void addFalsePositive(int i) {
        if ( (i >= 0) && !covers(i) && !falsePositive[i] ) {
            falsePositive[i] = true;   nFalsePositive++;
            if (falseNegative[i]) { falseNegative[i] = false;   nFalseNegative--; }
        }
        nAbsoluteFalsePositive++;
    }

    private void addFalseNegative(int i) {
        if ( (i >= 0) && !covers(i) && !error(i)) {
            falseNegative[i] = true;   nFalseNegative++;
        }
        nAbsoluteFalseNegative++;
    }

}
