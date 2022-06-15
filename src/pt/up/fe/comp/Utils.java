package pt.up.fe.comp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static int sizeOfDelimiter = 40;
    public static boolean debug = false;
    public static boolean optimize = false;

    static public boolean isInteger(String string) {
        try { Integer.parseInt(string); }
        catch (NumberFormatException e) { return false; }
        return true;
    }

    //returns -1 in case the pattern is not found in the string
    public static int indexOfRegEx(String strSource, String strRegExPattern) {
        int idx = -1;
        Pattern p =  Pattern.compile(strRegExPattern);
        Matcher m = p.matcher(strSource);

        if(m.find()) idx = m.start();

        return idx;
    }
    public static void setUtils(Map<String, String> config) {
        optimize = config.getOrDefault("optimize", "false").equals("true");
        debug = config.getOrDefault("debug", "false").equals("true");
    }

    public static void printHeader(String s) {
        if (s.length() > sizeOfDelimiter-2) return;
        boolean oneMore = s.length() % 2 != 0;
        var n = (sizeOfDelimiter - s.length()-2) / 2;
        String header = "-".repeat(n) + " " + s + " " + "-".repeat(n) + (oneMore ? "-" : "");
        System.out.println("\n" + header);
    }
    public static void printFooter() {
        System.out.println("-".repeat(sizeOfDelimiter) + "\n");
    }
}
