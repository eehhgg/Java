package optimization;

public class TabuList {
    private int[] tabuList;
    private int head;
    private boolean isFull;

    public TabuList(int size) {
        if (size < 1) { throw new IllegalArgumentException(); }
        tabuList = new int[size];
        head = 0;   // element to be overwritten
        isFull = false;
    }

    public boolean contains(int n) {
        int i, last = isFull ? tabuList.length-1 : head-1;
        for (i = 0; i <= last; i++) {
            if (tabuList[i] == n) { return true; }
        }
        return false;
    }

    public void add(int n) {
        tabuList[head] = n;   head++;
        if (head == tabuList.length) { head = 0;   isFull = true; }
    }

}
