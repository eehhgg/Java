package basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Parser {
    public static void parse(String fileName, ArrayList<Problem> problems,
    ArrayList<Solution> solutions) throws Exception {
        // read file
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line = in.readLine();
        while (line != null) { sb.append(line.replaceAll(" ",""));   line = in.readLine(); }
        // parse problems
        int start = sb.indexOf("problem("), end;   Problem p;
        while (start != -1) {
            end = sb.indexOf("problem(", start+1);
            if (end == -1) { end = sb.length(); }
            parseProblem(sb.substring(start, end), problems, solutions);
            if (end < sb.length()) { start = end; } else { start = -1; }
        }
        // close
        in.close();
    }

    // private methods ---------------------------------------------------------

    private static void parseProblem(String str, ArrayList<Problem> problems,
    ArrayList<Solution> solutions) {
        Problem problem = new Problem();
        // parse id
        int start = str.indexOf("problem("), end = str.indexOf(",",start);
        if ((start == -1) || (end == -1)) {
            throw new RuntimeException("Missing problem id");
        }
        problem.id = str.substring(start+8,end).trim();
        if (containsId(problems, problem.id)) {
            throw new RuntimeException("Duplicated problem id: " + problem.id);
        }
        // parse the number of rows
        start = str.indexOf("matrix(", end);
        end = str.indexOf(",", start+7);
        if ((start == -1) || (end == -1)) {
            throw new RuntimeException("Missing numRows parameter in problem \"" + problem.id + "\"");
        }
        int numRows = Integer.parseInt(str.substring(start+7,end));
        if (numRows < 1) {
            throw new RuntimeException("Invalid numRows parameter in problem \"" + problem.id + "\"");
        }
        // parse matrix
        end = str.indexOf("answers(", end);
        if (end == -1) {
            throw new RuntimeException("Missing answers in problem \"" + problem.id + "\"");
        }
        ArrayList<Diagram> diags = new ArrayList<Diagram>();
        parseDiagrams(str.substring(start,end), diags);
        if ( (diags.isEmpty()) || (diags.size() % numRows != 0) ) {
            throw new RuntimeException("Invalid number of diagrams in problem \"" + problem.id + "\"");
        }
        int i, j, k, numFigures = 0, numCols = diags.size()/numRows;
        problem.cells = new Diagram[numRows][numCols];
        ArrayList<Diagram> missingCells = new ArrayList<Diagram>();
        Diagram diag;
        for (i = 0; i < numRows; i++){
            for (j = 0; j < numCols; j++) {
                problem.cells[i][j] = diags.get(i*numCols + j);
                if (problem.cells[i][j] != null) {   // not null object
                    problem.cells[i][j].row = i;   problem.cells[i][j].col = j;
                    for (k = 0; k < problem.cells[i][j].figures.size(); k++) {
                        problem.cells[i][j].figures.get(k).setIndex(numFigures);
                        numFigures++;
                    }
                } else {
                    diag = new Diagram();   diag.row = i;   diag.col = j;
                    missingCells.add(diag);
                }
            }
        }
        if (missingCells.isEmpty()) {
            throw new RuntimeException("No missing cells in problem \"" + problem.id + "\"");
        }
        problem.numCellFigures = numFigures;
        problem.missingCells = missingCells.toArray(new Diagram[0]);
        // parse answers
        start = end;   end = str.indexOf("solution(", end);
        if (end == -1) {
            throw new RuntimeException("Missing solution in problem \"" + problem.id + "\"");
        }
        diags.clear();
        parseDiagrams(str.substring(start,end), diags);
        if (diags.isEmpty()) {
            throw new RuntimeException("Missing answers in problem \"" + problem.id + "\"");
        }
        problem.answers = new Diagram[diags.size()];
        for (i = 0; i < diags.size(); i++) {
            if (diags.get(i) == null) {
                throw new RuntimeException("Null answer in problem \"" + problem.id + "\"");
            }
            problem.answers[i] = diags.get(i);
            problem.answers[i].row = -1;   problem.answers[i].col = i;
        }
        problems.add(problem);
        // parse solution
        start = str.indexOf("[", end+8);   end = str.indexOf("]", end+8);
        if ( (start == -1) || (end == -1) || (start > end) ) {
            throw new RuntimeException("Invalid solution in problem \"" + problem.id + "\"");
        }
        String[] values = str.substring(start+1, end).split(",");
        if (values.length != problem.missingCells.length) {
            throw new RuntimeException("Invalid solution in problem \"" + problem.id + "\"");
        }
        Solution correct = new Solution(problem);
        for (i = 0; i < values.length; i++) {
            correct.values[i] = Integer.parseInt(values[i]);
        }
        solutions.add(correct);
    }

    private static boolean containsId(ArrayList<Problem> problems, String id) {
        for (int i = 0; i < problems.size(); i++) {
            if (problems.get(i).id.equals(id)) { return true; }
        }
        return false;
    }
    
    private static void parseDiagrams(String str, ArrayList<Diagram> diagrams) {
        int start = str.indexOf("diagram("), end;
        while (start != -1) {
            end = str.indexOf("diagram(", start+8);
            if (end == -1) { end = str.length(); }
            diagrams.add( parseDiagram(str.substring(start, end)) );
            if (end < str.length()) { start = end; } else { start = -1; }
        }
    }

    private static Diagram parseDiagram(String str) {
        if (str.indexOf("diagram([null])") != -1) { return null; }   // null diagram
        Diagram diagram = new Diagram();
        int start = str.indexOf("figure("), end;
        while (start != -1) {
            end = str.indexOf("figure(", start+1);
            if (end == -1) { end = str.length(); }
            parseFigure(str.substring(start, end), diagram);
            if (end < str.length()) { start = end; } else { start = -1; }
        }
        return diagram;
    }

    private static void parseFigure(String str, Diagram diagram) {
        Figure figure = new Figure();
        figure.setId(diagram.figures.size());
        figure.setDiagram(diagram);   diagram.figures.add(figure);
        // separate attributes
        int start = str.indexOf("figure(")+7, end = str.indexOf(")",start);
        if ((start == -1) || (end == -1)) {
            throw new RuntimeException("Invalid figure");
        }
        StringBuilder sb = new StringBuilder(str.substring(start, end));
        int i = 0;   char ch;
        while (i < sb.length()) {
            ch = sb.charAt(i);
            if ( (ch == '[') || (ch == ']') || (ch == ' ') ) { sb.deleteCharAt(i); }
            else { i++; }
        }
        String[] atts = sb.toString().split(",");   i = 0;
        // parse attributes
        boolean dName = false, dPosition = false, dScale = false;
        boolean dRotation = false, dReflectX = false;
        try { while (i < atts.length) {
            if (atts[i].equals("name")) {
                if (dName) { throw new Exception(); }
                figure.setName(atts[i+1]);   i += 2;   dName = true;
            } else if (atts[i].equals("position")) {
                if (dPosition) { throw new Exception(); }
                start = atts[i+1].indexOf(":");
                if (start == -1) { throw new Exception(); }
                figure.setPositionX(Integer.parseInt( atts[i+1].substring(0,start) ));
                figure.setPositionY(Integer.parseInt( atts[i+1].substring(start+1) ));
                i += 2;   dPosition = true;
            } else if (atts[i].equals("scale")) {
                if (dScale) { throw new Exception(); }
                start = atts[i+1].indexOf(":");
                if (start == -1) { throw new Exception(); }
                figure.setScaleX(Float.parseFloat( atts[i+1].substring(0,start) ));
                figure.setScaleY(Float.parseFloat( atts[i+1].substring(start+1) ));
                i += 2;   dScale = true;
            } else if (atts[i].equals("rotate")) {
                if (dRotation) { throw new Exception(); }
                figure.setRotation(Float.parseFloat( atts[i+1] ));
                i += 2;   dRotation = true;
            } else if (atts[i].equals("reflectX")) {
                if (dReflectX) { throw new Exception(); }
                figure.setReflectX(true);   i++;   dReflectX = true;
            } else {
                throw new Exception();
            }
        } } catch (Exception e) {
            throw new RuntimeException("Invalid attribute \"" + atts[i]
                    + "\" in figure:\n" + figure);
        }
        if (!dName) {
            throw new RuntimeException("Undefined name in figure " + figure);
        }
    }
    
}
