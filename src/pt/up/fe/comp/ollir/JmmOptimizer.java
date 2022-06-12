package pt.up.fe.comp.ollir;
import java.util.Collections;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;

import freemarker.core.builtins.sourceBI;
import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.optimization.ConstPropagationTable;
import pt.up.fe.comp.optimization.ConstantFoldingVisitor;
import pt.up.fe.comp.optimization.ConstantPropagationVisitor;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        if (!semanticsResult.getConfig().containsKey("optimize") || !semanticsResult.getConfig().get("optimize").equals("true"))
            return semanticsResult;

        JmmNode root = semanticsResult.getRootNode();
        int counter = 1;
        while (counter > 0) {
            // Constant Propagation
            var constantPropagation = new ConstantPropagationVisitor();
            ConstPropagationTable table = new ConstPropagationTable(); // (name, const_value)
            constantPropagation.visit(root, table);
            counter = constantPropagation.getCounter();

            // Constant Folding
            var constantFolding = new ConstantFoldingVisitor();
            constantFolding.visit(root);
            counter += constantFolding.getCounter();
        }

        return semanticsResult;
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
        if (!ollirResult.getConfig().containsKey("optimize") || !ollirResult.getConfig().get("optimize").equals("true"))
        return ollirResult;
        
        while(indexOfRegEx(ollirResult.getOllirCode(),"Loop\\d*:")!=-1)
        {
            ollirResult = optimizeGoto(ollirResult);
        }

        return JmmOptimization.super.optimize(ollirResult);
    }

    private OllirResult optimizeGoto(OllirResult ollirResult) {

        System.out.println("Optimize");
        String ollirCode = ollirResult.getOllirCode();

        /*Loop Block*/
        //int loopStartIndex = ollirCode.indexOf("Loop");
        int loopStartIndex = indexOfRegEx(ollirCode, "Loop\\d*:");
        int dotIndex = ollirCode.indexOf(":", loopStartIndex + 4);
        String loopNumber = ollirCode.substring(loopStartIndex + 4,dotIndex);
        // int loopEndIndex = ollirCode.indexOf("EndLoop"+loopNumber,ollirCode.indexOf("EndLoop"+loopNumber)+1);
        int loopEndIndex = indexOfRegEx(ollirCode, "EndLoop"+loopNumber +":");
        String loop = ollirCode.substring(loopStartIndex,loopEndIndex);
        // System.out.println("old loop: " + loop);

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
        condition = condition.replace("Body", "Optloop");
        //System.out.println(condition);

        /* Create Optmize Loop Block*/
        String optLoop = "\tgoto OptEndloop"+loopNumber+";\n";
        optLoop += "Optloop" + loopNumber + ":";
        optLoop += body;
        optLoop += "OptEndloop" + loopNumber + ":";
        optLoop += condition;
        //System.out.println("new loop: " + optLoop);

        ollirCode = ollirCode.replace(loop, optLoop);
        ollirCode = ollirCode.replace("EndLoop"+loopNumber+":", "");
        System.out.println("opt ollir code: " + ollirCode);

        OllirResult optOllirResult = new OllirResult(ollirCode, ollirResult.getConfig());


        return optOllirResult;
    }

    //returns -1 in case the pattern is not found in the string
    private static int indexOfRegEx(String strSource, String strRegExPattern) {
    
        int idx = -1;
        
        //compile pattern from string
        Pattern p =  Pattern.compile(strRegExPattern);
        
        //create a matcher object
        java.util.regex.Matcher m = p.matcher(strSource);
        
        //if pattern is found in the source string
        if(m.find()) {
            
            //get the start index using start method of the Matcher class
            idx = m.start();
        }
        
        return idx;
        
    }
}
