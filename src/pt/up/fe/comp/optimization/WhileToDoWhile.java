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

    public OllirResult optimize() {
        while (OllirUtils.indexOfRegEx(ollirResult.getOllirCode(),"Loop\\d*:")!=-1) {
            ollirResult = optimizeGoto();
        }
        return ollirResult;
    }

    private OllirResult optimizeGoto() {

        System.out.println("Optimize");
        String ollirCode = ollirResult.getOllirCode();

        // Loop Block
        int loopStartIndex = OllirUtils.indexOfRegEx(ollirCode, "Loop\\d*:");
        int colonIndex = ollirCode.indexOf(":", loopStartIndex + 4);
        String loopNumber = ollirCode.substring(loopStartIndex + 4, colonIndex);
        String endLoopLabel = "EndLoop" + loopNumber + ":";
        int loopEndIndex = ollirCode.indexOf(endLoopLabel) + endLoopLabel.length() + 1;
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
        String condition = loop.substring(conditionStartIndex, conditionEndIndex).trim() + "\n";
        condition = condition.replace("Body", "LoopOpt");
        //System.out.println(condition);

        List<String> lines = List.of(ollirCode.split("\n"));
        int whileLineIdx = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("Loop"+loopNumber+":")) {
                whileLineIdx = i;
                break;
            }
        }

        boolean executedOnce = executedAtLeastOnce(condition, lines, whileLineIdx);

        // Create Optimize Loop Block
        // if the original while is executed at least the first time
        // -> do not include first goto and associated label [goto EndLoopOpt + EndLoopOpt:]

        String optLoop = "";
        if (!executedOnce) optLoop += "\tgoto EndLoopOpt"+loopNumber+";\n\t  ";
        optLoop += "LoopOpt" + loopNumber + ":";
        optLoop += body;
        if (!executedOnce) optLoop += "EndLoopOpt" + loopNumber + ":\n\t\t";
        optLoop += condition;
        //System.out.println("new loop: " + optLoop);

        ollirCode = ollirCode.replace(loop, optLoop);
        // ollirCode = ollirCode.replace("EndLoop"+loopNumber+":", "");
        System.out.println("opt ollir code: \n" + ollirCode);

        return new OllirResult(ollirCode, ollirResult.getConfig());
    }

    private String variableAssignmentInLastBasicBlock(List<String> lines, int whileLineIdx, String variable) {

        for (int i = whileLineIdx - 1; i > 0; i--) {
            if (lines.get(i).matches(".*EndLoop\\d*:.*")
                    || lines.get(i).matches(".*EndIf\\d*:.*")
                    || lines.get(i).matches(".*LoopOpt\\d*;.*")) return null;
            if (lines.get(i).contains(variable + " :=.")) return lines.get(i);
        }

        return null;
    }

    private boolean executedAtLeastOnce(String condition, List<String> lines, int whileLineIdx) {

        String expression = condition.substring(condition.indexOf("(") + 1, condition.lastIndexOf(")"));
        String[] parts = expression.split(" ");

        if (parts.length < 1 || parts.length > 3) return false;

        if (parts.length == 3) {
            if (parts[1].equals("<.bool")) {
                Integer leftValue = findValue(parts[0], lines, whileLineIdx);
                Integer rightValue = findValue(parts[2], lines, whileLineIdx);

                return leftValue != null && rightValue != null && leftValue < rightValue;
            } else if (parts[1].equals("&&.bool")) {
                Integer leftValue = findValue(parts[0], lines, whileLineIdx);
                Integer rightValue = findValue(parts[2], lines, whileLineIdx);

                return leftValue != null && rightValue != null && leftValue == 1 && rightValue == 1;
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