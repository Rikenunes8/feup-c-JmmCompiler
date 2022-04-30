package pt.up.fe.comp.analysis.analysers;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.stream.Collectors;

import static pt.up.fe.comp.Utils.*;

public class FunctionArgsVisitor extends SemanticAnalyserVisitor {

    public FunctionArgsVisitor() {
        super();
        addVisit("FunctionCall", this::visitFunctionCall);
    }

    private Boolean visitFunctionCall(JmmNode jmmNode, SymbolTableBuilder symbolTable) {
        String name = jmmNode.get("name");
        JmmNode left = jmmNode.getJmmParent().getJmmChild(0);

        if (left.getKind().equals("ThisLiteral")) {
            if (!symbolTable.getMethods().contains(name)) {
                if (symbolTable.getSuper() == null)
                    this.addReport(jmmNode, "method "+name+" does not exist in class "+symbolTable.getClassName());
                return true;
            }

            List<Type> params = symbolTable.getParameters(name).stream().map(Symbol::getType).collect(Collectors.toList());
            List<Type> args = jmmNode.getChildren().stream().map(arg -> getType(arg, symbolTable)).collect(Collectors.toList());
            if (params.size() != args.size()) {
                this.addReport(jmmNode,"method "+name+" in class "+symbolTable.getClassName()+" cannot be applied to given types");
                return true;
            }
            for (int i = 0; i < args.size(); i++) {
                Type argType = args.get(i);
                Type paramType = params.get(i);
                if (!argType.equals(paramType)) {
                    this.addReport(jmmNode, "error: incompatible types: "+argType.print()+" cannot be converted to "+paramType.print());
                }
            }
            return true;
        }
        else {
            if (isIdentifierDeclared(left, symbolTable)) {
                String typeName = getType(left, symbolTable).getName();
                if (!typeName.matches("boolean|int"))
                    return true;
                this.addReport(jmmNode, "Primitive type ("+typeName+") "+left.get("val")+" has no methods");
            }
            else {
                if (isImported(left.get("val"), symbolTable))
                    return true;

                this.addReport(jmmNode, "Unable to find "+left.get("val"));
            }
            return false;
        }
    }
}
