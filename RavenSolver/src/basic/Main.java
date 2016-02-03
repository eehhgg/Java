package basic;

import answer.PostAnswerSelector;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            ArrayList<Problem> problems = new ArrayList<Problem>();
            ArrayList<Solution> solutions = new ArrayList<Solution>();
            Parser.parse("problems.txt", problems, solutions);
            solve(problems, solutions);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // private methods ---------------------------------------------------------

    private static void solve(ArrayList<Problem> problems,
    ArrayList<Solution> solutions) throws Exception {
        boolean[] correctD2 = new boolean[problems.size()];
        boolean[] correctD3 = new boolean[problems.size()];
        boolean[] correctD4 = new boolean[problems.size()];
        StringBuilder report = new StringBuilder();
        report.append(problems.size()).append(" RPM problems parsed\n\n\n");
        PostAnswerSelector aSel;   int i;
        for (i = 0; i < problems.size(); i++) {
            report.append("----------------------------------------------------------------------\n");
            report.append("Solving problem ").append(problems.get(i).id).append("\n\n");
            System.out.println("Solving problem " + problems.get(i).id);
            aSel = new PostAnswerSelector(problems.get(i));
            System.out.println("\tLevel 2");   aSel.solveNextLevel();   report.append(aSel);
            correctD2[i] = aSel.isCorrect(solutions.get(i));
            report.append( (correctD2[i] ? "Correct\n\n" : "Incorrect\n\n") );
            System.out.println("\tLevel 3");   aSel.solveNextLevel();   report.append(aSel);
            correctD3[i] = aSel.isCorrect(solutions.get(i));
            report.append( (correctD3[i] ? "Correct\n\n" : "Incorrect\n\n") );
            /*System.out.println("\tLevel 4");   aSel.solveNextLevel();   report.append(aSel);
            correctD4[i] = aSel.isCorrect(solutions.get(i));
            report.append( (correctD4[i] ? "Correct\n\n" : "Incorrect\n\n") );*/
            report.append("\n");
        }
        // report success rate
        report.append("----------------------------------------------------------------------\n");
        int correct2 = 0, correct3 = 0, correctAny = 0, correctAll = 0;
        for (i = 0; i < problems.size(); i++) {
            if (correctD2[i]) { correct2++; }
            if (correctD3[i]) { correct3++; }
            //if (correctD4[i]) { correct4++; }
            if (correctD2[i] || correctD3[i]) { correctAny++; }
            if (correctD2[i] && correctD3[i]) { correctAll++; }
        }
        float pct = 100f * correct2 / problems.size();
        report.append("Correct in level 2: " + correct2 + " (" + pct + "%)\n");
        appendBooleanArray(correctD2, report);   report.append("\n");
        pct = 100f * correct3 / problems.size();
        report.append("Correct in level 3: " + correct3 + " (" + pct + "%)\n");
        appendBooleanArray(correctD3, report);   report.append("\n");
        /*pct = 100f * correct4 / problems.size();
        report.append("Correct in level 4: " + correct4 + " (" + pct + "%)\n");
        appendBooleanArray(correctD4, report);   report.append("\n");*/
        pct = 100f * correctAny / problems.size();
        report.append("Correct in any: " + correctAny + " (" + pct + "%)\n");
        pct = 100f * correctAll / problems.size();
        report.append("Correct in all: " + correctAll + " (" + pct + "%)\n");
        save(report.toString(), "out.txt");
    }

    private static void appendBooleanArray(boolean[] array, StringBuilder sb) {
        if (array.length == 0) { return; }
        for (int i = 0; i < array.length; i++) {
            if ( (i > 0) && (i % 12 == 0) ) { sb.append(","); }
            if (array[i]) { sb.append("1,"); } else { sb.append("0,"); }
        }
    }

    private static void save(String str, String fileName) throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write(str);   out.close();
    }

}
