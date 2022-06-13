package pt.up.fe.comp.ollir;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;


public class OllirUtils {
    public static String getCode(Symbol symbol) {
        return symbol.getName() + "." + getCode(symbol.getType());
    }

    public static String getType(String code) {
        return code.substring(code.lastIndexOf(".") + 1);
    }

    public static String getCode(Type type) {
        if (type == null) return "";
        StringBuilder code = new StringBuilder();
        if (type.isArray()) {
            code.append("array.");
        }
        code.append(getOllirType(type.getName()));

        return code.toString();
    }

    public static String getOllirType(String jmmType) {
        switch (jmmType) {
            case "void": return "V";
            case "int": return "i32";
            case "boolean": return "bool";
            default: return jmmType;
        }
    }


    public static boolean isConstant(String e) {
        return e.matches("\\d.*");
    }
    public static boolean isVariable(String e) {
        return !e.contains("(") && !isArithExpr(e) && !isParam(e) && !isConstant(e) && !isArrayAccess(e);
    }
    public static boolean isArrayAccess(String e) {
        return e.contains("[");
    }
    public static boolean isArithExpr(String e) {
        return e.matches(".*(&&|<|\\+|-|\\*|/|!).*");
    }
    public static boolean isInvoke(String e) {
        return e.startsWith("invokevirtual(") || e.startsWith("invokespecial(") || e.startsWith("invokestatic(");
    }
    public static boolean isLength(String e) {
        return e.startsWith("arraylength(");
    }
    public static boolean isFieldAccess(String e) {
        return isGetfield(e) || isPutfield(e);
    }
    public static boolean isGetfield(String e) {
        return e.startsWith("getfield(");
    }
    public static boolean isPutfield(String e) {
        return e.startsWith("putfield(");
    }
    public static boolean isNew(String e) {
        return e.startsWith("new(");
    }
    public static boolean isParam(String e) {
        return e.startsWith("$");
    }
    public static boolean isComplex(String e) {
        return isInvoke(e) || isLength(e) || isNew(e) || isFieldAccess(e) || isArithExpr(e);
    }

    //returns -1 in case the pattern is not found in the string
    public static int indexOfRegEx(String strSource, String strRegExPattern) {
        int idx = -1;
        Pattern p =  Pattern.compile(strRegExPattern);
        Matcher m = p.matcher(strSource);
        
        if(m.find()) idx = m.start();
        
        return idx;
    }
}
