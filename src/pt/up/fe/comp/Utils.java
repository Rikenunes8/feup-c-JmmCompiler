package pt.up.fe.comp;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp.ast.AstNode.*;

public class Utils {
    static public Type buildType(String typeSignature) {
        if (typeSignature.equals("int[]")) {
            return new Type("int", true);
        } else if (typeSignature.equals("String[]")) {
            return new Type("String", true);
        } else {
            return new Type(typeSignature, false);
        }
    }

    static public Type getType(JmmNode var, SymbolTableBuilder symbolTable) {
        if (var.getKind().equals(THIS_LITERAL.toString()))
            return null;

        if (var.getKind().matches("TrueLiteral|FalseLiteral|AndExp|NotExp|LessExp|Condition"))
            return new Type("boolean", false);

        if (var.getKind().matches("IntegerLiteral|AddExp|SubExp|MultExp|DivExp|ArrayAccess|ArrayAccessExp"))
            return new Type("int", false);

        if (var.getKind().equals(NEW_INT_ARRAY.toString()))
            return new Type("int", true);

        if (var.getKind().equals(NEW_OBJECT.toString()))
            return new Type(var.get("name"), false);

        if (var.getKind().equals(DOT_EXP.toString()))
            return getDotExpType(var, symbolTable);

        String methodSignature = "";
        if (var.getAncestor(METHOD_DECLARATION.toString()).isPresent()) {
            methodSignature = var.getAncestor(METHOD_DECLARATION.toString()).get().get("name");
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

        return null;
    }

    static public Type getDotExpType(JmmNode dotExp, SymbolTableBuilder symbolTable) {
        JmmNode leftNode  = dotExp.getJmmChild(0);
        JmmNode rightNode = dotExp.getJmmChild(1);
        Type leftNodeType = getType(leftNode, symbolTable);

        if (rightNode.getKind().equals(PROPERTY_LENGTH.toString())) {
            if (leftNodeType == null || leftNodeType.isArray())
                return new Type("int", false);
        }
        else if (rightNode.getKind().equals(FUNCTION_CALL.toString())) {
            if (leftNode.getKind().equals(THIS_LITERAL.toString())
                    || (leftNode.getKind().equals(IDENTIFIER_LITERAL.toString()))
                        && leftNodeType != null
                        && leftNodeType.getName().equals(symbolTable.getClassName())) {

                if (symbolTable.hasMethod(rightNode.get("name"))) {
                    return symbolTable.getReturnType(rightNode.get("name"));
                }
            }
        }
        return null;
    }

    static public Boolean isIdentifierDeclared(JmmNode identifier, SymbolTable symbolTable) {
        String methodSignature = "";
        if (identifier.getAncestor(METHOD_DECLARATION.toString()).isPresent()) {
            methodSignature = identifier.getAncestor(METHOD_DECLARATION.toString()).get().get("name");
        }

        if (!methodSignature.isEmpty()) {
            if (symbolTable.getLocalVariables(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a local variable
                return true;
            if (symbolTable.getParameters(methodSignature).stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a method parameter
                return true;
        }

        if (symbolTable.getFields().stream().anyMatch(symbol -> symbol.getName().equals(identifier.get("val")))) //identifier is a field of the class
            return true;

        return false;
    }

    static public boolean isImported(String signature, SymbolTable symbolTable) {
        List<String> lastImports = symbolTable.getImports().stream()
                .map(s -> s.split("\\."))
                .map(strs -> strs[strs.length-1])
                .collect(Collectors.toList());

        if (lastImports.contains(signature))
            return true;
        return false;
    }

    static public boolean isBuiltInType(String type) {
        return isBuiltInType(buildType(type));
    }

    static public boolean isBuiltInType(Type type) {
        return type.getName().matches("int|boolean|String|void");
    }
}
