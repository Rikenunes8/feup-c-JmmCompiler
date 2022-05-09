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
        StringBuilder code = new StringBuilder();

        if (type.isArray()) {
            code.append("array.");
        }
        code.append(getOllirType(type.getName()));

        return code.toString();
    }

    public static String getOllirType(String jmmType) {
        switch (jmmType) {
            case "void":
                return "V";
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            default:
                return jmmType;
        }
    }

    // TODO is this being used?
    public static String getOllirExpression(JmmNode node) {
       
        switch(node.getKind()){
            case "IdentifierLiteral":
                return node.get("val") + ".i32";
            case "TrueLiteral":
                return "1.bool";
            case "FalseLiteral":
                return "0.bool";
            case "IntegerLiteral":
                return node.get("val") + ".i32";
            default:
                return "INVALID EXPRESSION";
        }
    }
}
