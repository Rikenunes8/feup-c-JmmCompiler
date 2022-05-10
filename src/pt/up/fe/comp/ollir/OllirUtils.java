package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;


// TODO Possible change things
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
}
