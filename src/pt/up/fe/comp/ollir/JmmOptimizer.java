package pt.up.fe.comp.ollir;
import java.util.Collections;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTableBuilder)semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        String ollirCode = ollirGenerator.getCode();

        System.out.println("\nOLLIR CODE:\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {

        ollirResult = optimizeGoto(ollirResult);


        return JmmOptimization.super.optimize(ollirResult);
    }

    private OllirResult optimizeGoto(OllirResult ollirResult) {
        System.out.println("Optimize");
        String ollirCode = ollirResult.getOllirCode();
        int loopStartIndex = ollirCode.indexOf("Loop");
        int dotIndex = ollirCode.indexOf(":", loopStartIndex + 4);
        String loopNumber = ollirCode.substring(loopStartIndex + 4,dotIndex);
        int loopEndIndex = ollirCode.indexOf("EndLoop"+loopNumber,ollirCode.indexOf("EndLoop"+loopNumber)+1);
        String loop = ollirCode.substring(loopStartIndex,loopEndIndex);
        //System.out.println(loop);

        /* Body Block*/
        int bodyStartIndex = loop.indexOf("Body"+loopNumber,loop.indexOf("Body"+loopNumber)+1);
        int dotBlockIndex = loop.indexOf(":",bodyStartIndex)+1;
        int bodyEndIndex = loop.indexOf("goto Loop"+loopNumber);
        String body = loop.substring(dotBlockIndex,bodyEndIndex);
        //System.out.println(body);

        /* Condition Block */
        int conditionStartIndex = loop.indexOf(":")+ 1;
        int conditionEndIndex = loop.indexOf("goto EndLoop"+loopNumber);
        String condition = loop.substring(conditionStartIndex,conditionEndIndex);
        condition = condition.replace("Body", "OptLoop");
        //System.out.println(condition);

        String optLoop = "\tgoto OptEndLoop"+loopNumber+";\n";
        optLoop += "OptLoop" + loopNumber + ":";
        optLoop += body;
        optLoop += "OptEndLoop" + loopNumber + ":";
        optLoop += condition;
        System.out.println(optLoop);

        return new OllirResult(ollirCode, ollirResult.getConfig());
    }
}
