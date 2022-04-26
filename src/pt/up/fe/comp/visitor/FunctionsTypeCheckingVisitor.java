package pt.up.fe.comp.visitor;

import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;
import java.util.List;

public class FunctionsTypeCheckingVisitor extends PreorderJmmVisitor<JmmAnalyser, Boolean> {
    public FunctionsTypeCheckingVisitor() {
        addVisit("FunctionalCall", this::checkFunctionArgs);
    }

    private Boolean checkFunctionArgs(JmmNode jmmNode, JmmAnalyser jmmAnalyser) {
        String name = jmmNode.get("name");
        List<String> args = new ArrayList<>();
        return true;
    }
}
