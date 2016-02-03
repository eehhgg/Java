package basic;

public class Problem {
    public String id;
    public Diagram[][] cells;
    public Diagram[] missingCells, answers;
    public int numCellFigures;

    public Problem() {
        id = null;   cells = null;   missingCells= null;
        answers = null;   numCellFigures = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i, j;
        sb.append("problem: ").append(id).append("\nmatrix:\n");
        if (cells != null) {
            for (i = 0; i < cells.length; i++) {
            for (j = 0; j < cells[i].length; j++) {
                sb.append(cells[i][j]).append("\n");
            } }
        } else { sb.append("(empty)\n"); }
        sb.append("answers:\n");
        if (answers != null) {
            for (i = 0; i < answers.length; i++) {
                sb.append(answers[i]).append("\n");
            }
        } else { sb.append("(empty)\n"); }
        return sb.toString();
    }

}
