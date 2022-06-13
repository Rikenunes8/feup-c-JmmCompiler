package pt.up.fe.comp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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

    public static boolean isDebug(String s) {
        return s.equals("true");
    }
}
