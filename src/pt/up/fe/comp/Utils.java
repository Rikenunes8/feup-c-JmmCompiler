package pt.up.fe.comp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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
        System.out.println(config);
        if (!optimize) optimize = config.getOrDefault("optimize", "false").equals("true");
        if (!debug) debug = config.getOrDefault("debug", "false").equals("true");
    }
}
