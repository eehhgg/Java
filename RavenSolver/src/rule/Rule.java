package rule;

import basic.CoveredFigures;
import basic.Diagram;
import basic.DualDiagram;
import basic.Problem;
import java.util.ArrayList;

public class Rule implements Cloneable {
    public static final int TYPE_HORIZONTAL = 0, TYPE_VERTICAL = 1;
    public CoveredFigures coveredFigures;
    public int row, col;  // result
    public int cost, level;
    public ArrayList<Transformation> andList, andNotList;  // operands
    private int type;
    
    public Rule(int row, int col, Transformation T, int numCellFigures, int type) {
        if ( (row < 0) || (col < 0) || (numCellFigures < 1) ) {
            throw new IllegalArgumentException(row + "," + col + "," + numCellFigures);
        }
        if ( (type != TYPE_HORIZONTAL) && (type != TYPE_VERTICAL) ) {
            throw new IllegalArgumentException("Invalid rule type: " + type);
        }
        this.row = row;   this.col = col;   this.type = type;   cost = -1;   level = -1;
        andList = new ArrayList<Transformation>();   andList.add(T);
        andNotList = new ArrayList<Transformation>();
        coveredFigures = new CoveredFigures(numCellFigures);
    }

    public void evaluate(Problem p, CoveredFigures cf, boolean missingCells) {
        // define direction
        int dRow = 0, dCol = 0, incRow, incCol, numIncs;
        if (type == TYPE_HORIZONTAL) {
            incRow = 0;   incCol = 1;   numIncs = p.cells[0].length - 1;
        } else {
            incRow = 1;   incCol = 0;   numIncs = p.cells.length - 1;
        }
        // propagate
        int i, j, row1, col1;
        DualDiagram dual;   Transformation T;   Diagram d;
        for (i = 0; i <= numIncs; i++) {
            if ( missingCells == coversAnyCell(
            p.missingCells, dRow, dCol, p.cells.length, p.cells[0].length) ) {
                // evaluate an instance
                T = andList.get(0);
                row1 = wrap(T.row + dRow, p.cells.length);
                col1 = wrap(T.col + dCol, p.cells[0].length);
                d = p.cells[row1][col1];   dual = new DualDiagram(T.transform(d));
                for (j = 1; j < andList.size(); j++) {
                    T = andList.get(j);
                    row1 = wrap(T.row + dRow, p.cells.length);
                    col1 = wrap(T.col + dCol, p.cells[0].length);
                    d = p.cells[row1][col1];   dual.addPositive(T.transform(d));
                }
                for (j = 0; j < andNotList.size(); j++) {
                    T = andNotList.get(j);
                    row1 = wrap(T.row + dRow, p.cells.length);
                    col1 = wrap(T.col + dCol, p.cells[0].length);
                    d = p.cells[row1][col1];   dual.addNegative(T.transform(d));
                }
                row1 = wrap(row + dRow, p.cells.length);
                col1 = wrap(col + dCol, p.cells[0].length);
                d = p.cells[row1][col1];
                dual.evaluate(d, cf);
            }
            // define next instance
            dRow += incRow;   dCol += incCol;
        }
    }

    public int getType() {
        return type;
    }

    public boolean coversAnyCell(Diagram[] cells) {
        if (cells == null) { return false; }
        for (int i = 0; i < cells.length; i++) {
            if (coversCell(cells[i])) { return true; }
        }
        return false;
    }

    public int numOperands() {
        return andList.size() + andNotList.size();
    }

    public boolean hasOperand(Transformation t) {
        // no shift will be applied, so numRows and numCols are irrelevant
        return coversCell(t.row,t.col, 0,0, t.row+1, t.col+1);
    }

    public int numParameters() {
        int i, n = 0;
        for (i = 0; i < andList.size(); i++) {
            if (andList.get(i).hasParameters()) { n++; }
        }
        for (i = 0; i < andNotList.size(); i++) {
            if (andNotList.get(i).hasParameters()) { n++; }
        }
        return n;
    }

    public int indexIn(ArrayList<Rule> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == this) { return i; }
        }
        return -1;
    }

    public static String toString(ArrayList<Rule> rules) {
        if (rules.isEmpty()) { return "(empty)\n"; }
        StringBuilder sb = new StringBuilder();
        Rule r;   int i, numCellFigures = rules.get(0).coveredFigures.length();
        CoveredFigures cf = new CoveredFigures(numCellFigures);
        for (i = 0; i < rules.size(); i++) {
            r = rules.get(i);   cf.add(r.coveredFigures);   sb.append(r).append("\n");
        }
        sb.append("Covered ").append(cf.getCoverage());
        sb.append("/").append(numCellFigures).append(" cell figures\n");
        return sb.toString();
    }
    
    @Override
    public Object clone() {
        Rule r;
        try { r = (Rule) super.clone(); }
        catch (Exception e) { throw new RuntimeException("Unable to clone Rule"); }
        // give a clean CoveredFigures object
        r.coveredFigures = new CoveredFigures(coveredFigures.length());
        // Transformation objects are shared, because they are never modified
        r.andList = new ArrayList<Transformation>(andList);
        r.andNotList = new ArrayList<Transformation>(andNotList);
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if ( !(o instanceof Rule) ) { return false; }
        Rule r = (Rule) o;
        if ( (row != r.row) || (col != r.col) ) { return false; }
        if ( (andList.size() != r.andList.size()) ) { return false; }
        if ( (andNotList.size() != r.andNotList.size()) ) { return false; }
        // compare andList
        int i;   ArrayList<Transformation> tmpList;
        tmpList = new ArrayList<Transformation>(andList);
        for (i = 0; i < r.andList.size(); i++) {
            if (!tmpList.remove(r.andList.get(i))) { return false; }
        }
        // compare andNotList
        if (!andNotList.isEmpty()) {
            tmpList = new ArrayList<Transformation>(andNotList);
            for (i = 0; i < r.andNotList.size(); i++) {
                if (!tmpList.remove(r.andNotList.get(i))) { return false; }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.coveredFigures != null ? this.coveredFigures.hashCode() : 0);
        hash = 17 * hash + this.row;
        hash = 17 * hash + this.col;
        hash = 17 * hash + this.type;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type == TYPE_HORIZONTAL) { sb.append("hRule"); } else { sb.append("vRule"); }
        sb.append(": (").append(row).append(",").append(col).append(") equals ");
        if (andList.isEmpty()) { sb.append("(empty)");   return sb.toString(); }
        sb.append(andList.get(0));
        int i;
        for (i = 1; i < andList.size(); i++) {
            sb.append("\n\tand ").append(andList.get(i));
        }
        for (i = 0; i < andNotList.size(); i++) {
            sb.append("\n\tand not ").append(andNotList.get(i));
        }
        sb.append("\n\tcoverage: ").append(coveredFigures.getCoverage()).append(", ");
        sb.append(coveredFigures.getPositiveCoverage()).append("(+), ");
        sb.append(coveredFigures.getNegativeCoverage()).append("(-); errors: ");
        sb.append(coveredFigures.getFalsePositives()).append("(+), ");
        sb.append(coveredFigures.getFalseNegatives()).append("(-)");
        return sb.toString();
    }

    // private methods ---------------------------------------------------------

    private boolean coversCell(Diagram cell) {
        int i;
        if (type == TYPE_HORIZONTAL) {
            // match row
            if (row == cell.row) { return true; }
            for (i = 0; i < andList.size(); i++) {
                if (andList.get(i).row == cell.row) { return true; }
            }
            for (i = 0; i < andNotList.size(); i++) {
                if (andNotList.get(i).row == cell.row) { return true; }
            }
        } else {
            // match column
            if (col == cell.col) { return true; }
            for (i = 0; i < andList.size(); i++) {
                if (andList.get(i).col == cell.col) { return true; }
            }
            for (i = 0; i < andNotList.size(); i++) {
                if (andNotList.get(i).col == cell.col) { return true; }
            }
        }
        return false;
    }

    private boolean coversAnyCell(Diagram[] cells, int dRow, int dCol, int numRows, int numCols) {
        if (cells == null) { return false; }
        for (int i = 0; i < cells.length; i++) {
            if (coversCell(cells[i].row,cells[i].col, dRow,dCol, numRows,numCols)) { return true; }
        }
        return false;
    }

    private boolean coversCell(int cellRow, int cellCol, int dRow, int dCol, int numRows, int numCols) {
        int i;
        if ( sameCell(row+dRow,col+dCol, cellRow,cellCol, numRows,numCols) ) { return true; }
        for (i = 0; i < andList.size(); i++) {
            if ( sameCell(andList.get(i).row+dRow,andList.get(i).col+dCol,
                    cellRow,cellCol, numRows,numCols) ) { return true; }
        }
        for (i = 0; i < andNotList.size(); i++) {
            if ( sameCell(andNotList.get(i).row+dRow,andNotList.get(i).col+dCol,
                    cellRow,cellCol, numRows,numCols) ) { return true; }
        }
        return false;
    }

    private boolean sameCell(int row1, int col1, int row2, int col2, int numRows, int numCols) {
        row1 = wrap(row1, numRows);   col1 = wrap(col1, numCols);
        row2 = wrap(row2, numRows);   col2 = wrap(col2, numCols);
        return ( (row1 == row2) && (col1 == col2) );
    }

    private int wrap(int v, int max) {
        if (v < 0) { v += max; } else if (v >= max) { v -= max; }
        return v;
    }

}
