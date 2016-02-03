
import java.util.GregorianCalendar;

public class Clock {
    public int hour, min, sec;

    public Clock() {
        reset();
    }

    public Clock(String str) {
        // parse hour
        int token1 = 0, token2 = str.indexOf(":");
        hour = Integer.parseInt( str.substring(token1,token2) );
        if (hour < 0) { throw new IllegalArgumentException(); }
        // parse min
        token1 = token2 + 1;
        token2 = str.indexOf(":", token1);
        min = Integer.parseInt( str.substring(token1,token2) );
        if ( (min < 0) || (min > 59) ) { throw new IllegalArgumentException(); }
        // parse sec
        token1 = token2 + 1;
        sec = Integer.parseInt( str.substring(token1) );
        if ( (sec < 0) || (sec > 59) ) { throw new IllegalArgumentException(); }
    }

    public void copy(Clock clk) {
        hour = clk.hour;   min = clk.min;   sec = clk.sec;
    }

    public void substract(Clock clk) {
        int sec1 = hour*3600 + min*60 + sec;
        int sec2 = clk.hour*3600 + clk.min*60 + clk.sec;
        if (sec1 < sec2) { sec1 += 24*3600; }   // next day
        int dif = sec1 - sec2;
        hour = dif / 3600;  dif %= 3600;
        min = dif / 60;     dif %= 60;
        sec = dif;
    }

    public void add(Clock clk) {
        int sec1 = hour*3600 + min*60 + sec;
        int sec2 = clk.hour*3600 + clk.min*60 + clk.sec;
        int dif = sec1 + sec2;
        hour = dif / 3600;  dif %= 3600;
        min = dif / 60;     dif %= 60;
        sec = dif;
    }

    public void reset() {
        hour = 0;   min = 0;   sec = 0;
    }

    public void getNow() {
        GregorianCalendar cal = new GregorianCalendar();
        hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
        min = cal.get(GregorianCalendar.MINUTE);
        sec = cal.get(GregorianCalendar.SECOND);
    }

    @Override
    public String toString() {
        return hour + ":" + twoDigits(min) + ":" + twoDigits(sec);
    }

    private static String twoDigits(int i) {
        if (i < 10) { return "0" + i; }
        return "" + i;
    }

}
