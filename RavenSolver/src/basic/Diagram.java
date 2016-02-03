package basic;

import java.util.ArrayList;

public class Diagram implements Cloneable {
    public int row, col;
    public ArrayList<Figure> figures;

    public Diagram() {
        row = -1;   col = -1;   figures = new ArrayList<Figure>();
    }

    public String getLabel() {
        return "diagram (" + row + "," + col + ")";
    }

    @Override
    public Object clone() {
        Diagram d;
        try { d = (Diagram) super.clone(); }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unable to clone diagram");
        }
        d.figures = new ArrayList<Figure>();   Figure f;
        for (int i = 0; i < figures.size(); i++) {
            f = (Figure) figures.get(i).clone();
            f.setDiagram(d);   d.figures.add(f);
        }
        return d;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getLabel()).append(":\n");
        if (figures.isEmpty()) { sb.append("(empty)");   return sb.toString(); }
        sb.append(figures.get(0));
        for (int i = 1; i < figures.size(); i++) {
            sb.append("\n").append(figures.get(i));
        }
        return sb.toString();
    }

}
