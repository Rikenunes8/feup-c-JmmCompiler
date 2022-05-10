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
        return switch (jmmType) {
            case "void" -> "V";
            case "int" -> "i32";
            case "boolean" -> "bool";
            default -> jmmType;
        };
    }
}
