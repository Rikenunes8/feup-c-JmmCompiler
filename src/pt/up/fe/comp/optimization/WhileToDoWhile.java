package pt.up.fe.comp.optimization;

import pt.up.fe.comp.Utils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.ollir.OllirUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhileToDoWhile {
    private OllirResult ollirResult;

    public WhileToDoWhile(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
    }

    public void optimize() {
        while (ollirResult.getOllirCode().matches(".*Loop\\d*:.*")) {
            ollirResult = optimizeGoto();
        }
    }

    private OllirResult optimizeGoto() {

        System.out.println("Optimize");
        String ollirCode = ollirResult.getOllirCode();

        // Loop Block
        int loopStartIndex = OllirUtils.indexOfRegEx(ollirCode, "Loop\\d*:");
        int colonIndex = ollirCode.indexOf(":", loopStartIndex + 4);
        String loopNumber = ollirCode.substring(loopStartIndex + 4, colonIndex);
        String endLoopLabel = "EndLoop" + loopNumber + ":";
        int loopEndIndex = ollirCode.indexOf(endLoopLabel) + endLoopLabel.length();
        String loop = ollirCode.substring(loopStartIndex, loopEndIndex);

        // Body Block
        int bodyStartIndex = loop.indexOf("Body" + loopNumber + ":");
        int colonBlockIndex = loop.indexOf(":", bodyStartIndex);
        int bodyEndIndex = loop.indexOf("goto Loop" + loopNumber);
        String body = loop.substring(colonBlockIndex + 1, bodyEndIndex);
        //System.out.println(body);

        // Condition Block
        int conditionStartIndex = loop.indexOf(":") + 1;
        int conditionEndIndex = loop.indexOf("goto EndLoop" + loopNumber);
        String condition = loop.substring(conditionStartIndex, conditionEndIndex);
        condition = condition.replace("Body", "LoopOpt");
        //System.out.println(condition);

        // Create Optimize Loop Block
        // if the original while is executed at least the first time
        // -> do not include first goto and associated label [goto EndLoopOpt + EndLoopOpt:]
        String optLoop = "\tgoto EndLoopOpt"+loopNumber+";\n";
        optLoop += "\t\tLoopOpt" + loopNumber + ":";
        optLoop += body;
        optLoop += "EndLoopOpt" + loopNumber + ":\n";
        optLoop += condition;
        //System.out.println("new loop: " + optLoop);

        ollirCode = ollirCode.replace(loop, optLoop);
        // ollirCode = ollirCode.replace("EndLoop"+loopNumber+":", "");
        System.out.println("opt ollir code: \n" + ollirCode);

        return new OllirResult(ollirCode, ollirResult.getConfig());
    }

    private String variableAssignmentInLastBasicBlock(List<String> lines, int whileLineIdx, String variable) {

        for (int i = whileLineIdx - 1; i > 0; i--) {
            // EndLoopx:
            // EndIfx:
            // if (..) goto LoopOptx;
            if (lines.get(i).matches(".*EndLoop\\d*:.*") || lines.get(i).contains("If")) return null;
            if (lines.get(i).contains(variable + " :=.")) return lines.get(i);
        }

        return null;
    }

    private boolean executedAtLeastOnce(String condition, List<String> lines, int whileLineIdx) {
        // evaluate condition -> see used variables or keep literals
        // if (i.i32 <.bool 10.i32) goto LoopOpt0
        // x && y; x < y; true; !x; x; !.bool x.bool

        String expression = condition.substring(condition.indexOf("(") + 1, condition.lastIndexOf(")"));
        String[] parts = expression.split(" ");

        if (parts.length < 1 || parts.length > 3) return false;

        // find last assignment of that variables
        // see if the condition would be true
        if (parts.length == 3) {
            if (parts[1].equals("<.bool")) {
                Integer leftValue = findValue(parts[0], lines, whileLineIdx);
                Integer rightValue = findValue(parts[2], lines, whileLineIdx);

                return leftValue != null && rightValue != null && leftValue < rightValue;
            } else {
                // x && y

            }
        } else if (parts.length == 2) {
            String assignment = variableAssignmentInLastBasicBlock(lines, whileLineIdx, parts[1]);
            return assignment != null && assignment.endsWith(":=.bool 0.bool");
        } else {
            if (Objects.equals(parts[0], "1.bool")) {
                return true;
            } else {
                String assignment = variableAssignmentInLastBasicBlock(lines, whileLineIdx, parts[0]);
                return assignment != null && assignment.endsWith(":=.bool 1.bool");
            }
        }

        return false;
    }

    private Integer findValue(String variable, List<String> lines, int whileLineIdx) {
        String var = variable.substring(0, variable.indexOf("."));
        
        if (Utils.isInteger(var)) {
            return Integer.parseInt(var);
        } else {
            String assignment = variableAssignmentInLastBasicBlock(lines, whileLineIdx, variable);
            if (assignment == null || assignment.split(" ").length != 3) return null;

            String assignedValue = assignment.split(" ")[2];
            assignedValue = assignedValue.substring(0, assignedValue.indexOf("."));

            return (Utils.isInteger(assignedValue))
                    ? Integer.parseInt(assignedValue) : null;
        }
    }

}
