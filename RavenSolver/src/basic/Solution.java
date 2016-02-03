package basic;

public class Solution {
    public String id;
    public int[] values;

    public Solution(Problem p) {
        id = p.id;   values = new int[p.missingCells.length];
    }
}
