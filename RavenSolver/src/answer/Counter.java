package answer;

import java.util.Arrays;

public class Counter {
    private int[] digits;
    private int max;

    public Counter(int n, int max) {
        if ( (n < 1) || (max < 1) ) { throw new IllegalArgumentException(); }
        digits = new int[n];   this.max = max;   clear();
    }

    public void clear() {
        Arrays.fill(digits, 0);
    }

    public boolean inc() {
        int i = 0;   boolean c = true;
        while ( c && (i < digits.length) ) {
            digits[i]++;
            if (digits[i] > max) { digits[i] = 0;   c = true;   i++; }
            else { c = false; }
        }
        return c;
    }

    public int getDigit(int i) {
        return digits[i];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(digits[0]+1);
        for (int i = 1; i < digits.length; i++) {
            sb.append(",").append(digits[i]+1);
        }
        return sb.toString();
    }
    
}
