package rule;

import basic.Diagram;
import basic.Figure;

/**
 * Represents the right hand side of an equality (i.e., &quot;= T(d)&quot;).
 */
public class Transformation implements Cloneable {
    public int row, col;
    private TransformationParameters P;

    public Transformation(Diagram d) {
        row = d.row;   col = d.col;   P = null;
    }

    public void setParameters(int dx, int dy, float sx, float sy, float r, boolean reflectX) {
        if (!hasParameters()) { P = new TransformationParameters(); }
        P.setValues(dx, dy, sx, sy, r, reflectX);
    }

    public boolean hasParameters() {
        return (P != null);
    }

    public Diagram transform(Diagram d) {
        if (!hasParameters()) { return d; }
        Diagram r = new Diagram();   Figure f;
        for (int i = 0; i < d.figures.size(); i++) {
            f = (Figure) (d.figures.get(i).clone());
            P.transform(f);   r.figures.add(f);
        }
        return r;
    }

    public void transform(Transformation t) {
        if (!hasParameters()) { return; }
        if (!t.hasParameters()) {
            t.P = (TransformationParameters) P.clone();   return;
        }
        P.transform(t.P);
    }

    @Override
    public Object clone() {
        Transformation t;
        try { t = (Transformation) super.clone(); }
        catch (Exception e) { throw new RuntimeException("Unable to clone Transformation"); }
        if (hasParameters()) { t.P = (TransformationParameters) P.clone(); }
        return t;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if ( !(o instanceof Transformation) ) { return false; }
        Transformation t = (Transformation) o;
        if ( (row != t.row) || (col != t.col) ) { return false; }
        if (!hasParameters()) {
            if (t.hasParameters()) { return false; }
        } else {
            if (!P.equals(t.P)) { return false; }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.row;
        hash = 59 * hash + this.col;
        hash = 59 * hash + (this.P != null ? this.P.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(row).append(",").append(col).append(")");
        if (hasParameters()) { sb.append(" ").append(P); }
        return sb.toString();
    }

}
