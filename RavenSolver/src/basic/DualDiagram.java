package basic;

import java.util.ArrayList;
import java.util.Arrays;

public class DualDiagram {
    // each diagram is used as a list of equal figures
    private ArrayList<Diagram> positive, negative;
    private boolean[] positiveUpdates;

    public DualDiagram(Diagram d) {
        positive = new ArrayList<Diagram>();
        negative = new ArrayList<Diagram>();
        // initialize positive (grouping equal figures)
        int i, j;   boolean added;   Figure f;   Diagram newDiag;
        for (i = 0; i < d.figures.size(); i++) {
            f = d.figures.get(i);   added = false;   j = 0;
            while (!added && (j < positive.size())) {
                if (positive.get(j).figures.get(0).equals(f)) {
                    positive.get(j).figures.add(f);   added = true;
                } else { j++; }
            }
            if (!added) {
                newDiag = new Diagram();   newDiag.figures.add(f);
                positive.add(newDiag);
            }
        }
        // positive updates array (the positive list can only shrink, so this
        // array will be sufficient for all future updates)
        positiveUpdates = new boolean[positive.size()];
    }

    public void addPositive(Diagram d) {
        Arrays.fill(positiveUpdates, false);   int i;
        for (i = 0; i < d.figures.size(); i++) {
            addPositive(d.figures.get(i));
        }
        // update positive (backwards, because indices are altered on removals)
        for (i = positive.size()-1; i >= 0; i--) {
            if (!positiveUpdates[i]) {
                // turn negative
                negative.add(positive.remove(i));
            }
        }
    }

    public void addNegative(Diagram d) {
        for (int i = 0; i < d.figures.size(); i++) {
            addNegative(d.figures.get(i));
        }
    }

    public void evaluate(Diagram result, CoveredFigures cf) {
        int i, j;   Diagram d;   Figure f;   boolean covered;
        // positive
        for (i = 0; i < positive.size(); i++) {
            d = positive.get(i);   f = d.figures.get(0);   covered = false;
            // cover the result
            for (j = 0; j < result.figures.size(); j++) {
                if (result.figures.get(j).equals(f)) {
                    cf.addPositive(result.figures.get(j));   covered = true;
                }
            }
            // cover the operands
            if (covered) { cf.addPositive(d); }
            else { cf.addFalsePositive(d); }
        }
        // negative
        for (i = 0; i < negative.size(); i++) {
            d = negative.get(i);   f = d.figures.get(0);   covered = true;
            for (j = 0; j < result.figures.size(); j++) {
                if (result.figures.get(j).equals(f)) {
                    cf.addFalseNegative(result.figures.get(j));   covered = false;
                }
            }
            if (covered) { cf.addNegative(d); }
            else { cf.addFalseNegative(d); }
        }
    }

    // private methods ---------------------------------------------------------

    private void addPositive(Figure f) {
        int i;
        // search positive
        for (i = 0; i < positive.size(); i++) {
            if (positive.get(i).figures.get(0).equals(f)) {
                positive.get(i).figures.add(f);
                positiveUpdates[i] = true;   return;
            }
        }
        // search negative
        for (i = 0; i < negative.size(); i++) {
            if (negative.get(i).figures.get(0).equals(f)) {
                negative.get(i).figures.add(f);   return;
            }
        }
        // new figure
        Diagram d = new Diagram();   d.figures.add(f);   negative.add(d);
    }

    private void addNegative(Figure f) {
        int i;
        for (i = 0; i < positive.size(); i++) {
            if (positive.get(i).figures.get(0).equals(f)) {
                // turn negative
                positive.get(i).figures.add(f);
                negative.add(positive.remove(i));   return;
            }
        }
        for (i = 0; i < negative.size(); i++) {
            if (negative.get(i).figures.get(0).equals(f)) {
                negative.get(i).figures.add(f);   return;
            }
        }
        // if the figure was not already present in the dual diagram it is ignored
    }

}
