package pt.up.fe.comp.visitor;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    static public Type buildType(String typeSignature) {
        if (typeSignature.equals("int[]")) {
            return new Type("int", true);
        } else {
            return new Type(typeSignature, false);
        }
    }

    static public Type getType(JmmNode var, SymbolTable symbolTable) {

        if (var.getKind().matches("TrueLiteral|FalseLiteral|AndExp|NotExp|LessExp|Condition"))
            return new Type("boolean", false);

        if (var.getKind().matches("IntegerLiteral|AddExp|SubExp|MultExp|DivExp|ArrayAccess"))
            return new Type("int", false);

        if (var.getKind().equals("NewIntArray"))
            return new Type("int", true);

        String methodSignature = "";
        if (var.getAncestor("PublicMethod").isPresent()) {
            methodSignature = var.getAncestor("PublicMethod").get().get("name");
        }
        else if (var.getAncestor("PublicMain").isPresent()) {
            methodSignature = "main";
        }
        if (!methodSignature.isEmpty()) {
            List<Symbol> localVariables = symbolTable.getLocalVariables(methodSignature).stream()
                    .filter(symbol -> symbol.getName().equals(var.get("val")))
                    .collect(Collectors.toList());
            if (!localVariables.isEmpty()) //var is a local variable
                return localVariables.get(0).getType();

            List<Symbol> methodParameters = symbolTable.getParameters(methodSignature).stream()
                    .filter(symbol -> symbol.getName().equals(var.get("val")))
                    .collect(Collectors.toList());
            if (!methodParameters.isEmpty()) //var is a method parameter
                return methodParameters.get(0).getType();
        }

        List<Symbol> classFields = symbolTable.getFields().stream()
                .filter(symbol -> symbol.getName().equals(var.get("val")))
                .collect(Collectors.toList());
        if (!classFields.isEmpty()) //var is a field of the class
            return classFields.get(0).getType();

        return new Type(null, false);
    }
}
