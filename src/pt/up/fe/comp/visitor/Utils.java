package pt.up.fe.comp.visitor;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class Utils {
    static public Type buildType(String typeSignature) {
        if (typeSignature.equals("int[]")) {
            return new Type("int", true);
        } else {
            return new Type(typeSignature, false);
        }
    }

    // TODO idk what to do with Identifiers and missing expressions.
    static public Type getType(JmmNode expression) {
        try {
            String type = expression.get("type");
            return buildType(type);
        }
        catch (NullPointerException e) {
            switch (expression.getKind()) {
                case "AndExp":
                case "LessExp":
                case "NotExp":
                case "TrueLiteral":
                case "FalseLiteral":
                    return new Type("boolean", false);
                case "AddExp":
                case "SubExp":
                case "MultExp":
                case "DivExp":
                case "PropertyLength":
                case "IntegerLiteral":
                case "IdentifierLiteral":
                    return new Type("int", false);
                default:
                    return null;
            }
        }
    }
}
