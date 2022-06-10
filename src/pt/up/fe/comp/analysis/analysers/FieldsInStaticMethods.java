package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.stream.Collectors;

import static pt.up.fe.comp.ast.AstNode.*;
import static pt.up.fe.comp.ast.AstNode.IDENTIFIER_LITERAL;

public class FieldsInStaticMethods extends SemanticAnalyserVisitor{
    public FieldsInStaticMethods() {
        super();
        addVisit(IDENTIFIER_LITERAL, this::visitIdentifierLiteral);
    }

    private Boolean visitIdentifierLiteral(JmmNode identifier, SymbolTableBuilder symbolTableBuilder) {
        if (!identifier.getAncestor(METHOD_DECLARATION.toString()).isPresent()) return true;
        JmmNode method = identifier.getAncestor(METHOD_DECLARATION.toString()).get();
        if (method.get("static").equals("false")) return true;
        String name = identifier.get("val");
        var fields = symbolTableBuilder.getFields().stream().map(Symbol::getName).collect(Collectors.toList());
        if (!fields.contains(name)) return true;
        var params = symbolTableBuilder.getParameters(method.get("name")).stream().map(Symbol::getName).collect(Collectors.toList());
        var locals = symbolTableBuilder.getLocalVariables(method.get("name")).stream().map(Symbol::getName).collect(Collectors.toList());
        if (!params.contains(name) && !locals.contains(name))
            addReport(identifier, "can not use the static filed " + name + " in a static method");
        return true;
    }
}
