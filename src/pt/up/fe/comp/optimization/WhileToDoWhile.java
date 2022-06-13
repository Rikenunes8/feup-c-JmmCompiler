package pt.up.fe.comp.optimization;

import pt.up.fe.comp.Utils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class WhileToDoWhile {
    private final OllirResult ollirResult;

    public WhileToDoWhile(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
    }

    public OllirResult optimize() {
        String ollirCode = this.ollirResult.getOllirCode();
        while (SpecsStrings.matches(ollirCode, Pattern.compile(".*Loop\\d*:.*"))) {
            ollirCode = this.optimizeSingleWhile(ollirCode);

            // TODO
            System.out.println("Single while optimized: ");
            System.out.println(ollirCode);
        }
        return new OllirResult(ollirCode, this.ollirResult.getConfig());
    }

    private String optimizeSingleWhile(String ollirCode) {
        System.out.println("Optimize");

        // Loop Block
        int loopStartIndex = Utils.indexOfRegEx(ollirCode, "Loop\\d*:");
        int colonIndex = ollirCode.indexOf(":", loopStartIndex + 4);
        String loopNumber = ollirCode.substring(loopStartIndex + 4, colonIndex);
        String endLoopLabel = "EndLoop" + loopNumber + ":";
        int loopEndIndex = ollirCode.indexOf(endLoopLabel) + endLoopLabel.length() + 1;
        String oldWhile = ollirCode.substring(loopStartIndex, loopEndIndex);

        // Body Block
        int bodyStartIndex = oldWhile.indexOf("Body" + loopNumber + ":");
        int colonBlockIndex = oldWhile.indexOf(":", bodyStartIndex);
        int bodyEndIndex = oldWhile.indexOf("goto Loop" + loopNumber);
        String body = oldWhile.substring(colonBlockIndex + 1, bodyEndIndex).trim() + "\n";

        // Condition Block
        int conditionStartIndex = oldWhile.indexOf(":") + 1;
        int conditionEndIndex = oldWhile.indexOf("goto EndLoop" + loopNumber);
        String condition = oldWhile.substring(conditionStartIndex, conditionEndIndex).trim() + "\n";
        condition = condition.replace("Body", "LoopOpt");

        List<String> lines = List.of(ollirCode.split("\n"));
        int whileLineIdx = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("Loop"+loopNumber+":")) {
                whileLineIdx = i;
                break;
            }
        }

        boolean executedOnce = this.executedAtLeastOnce(condition, lines, whileLineIdx);

        // Create Optimize Loop Block
        // if the original while is executed at least the first time
        // -> do not include first goto and associated label [goto EndLoopOpt + EndLoopOpt:]

        String optWhile = "";
        if (!executedOnce) optWhile += "\tgoto EndLoopOpt"+loopNumber+";\n\t  ";
        optWhile += "LoopOpt" + loopNumber + ":\n";
        optWhile += "\t\t" + body;
        if (!executedOnce) optWhile += "\t  " + "EndLoopOpt" + loopNumber + ":\n";
        optWhile += "\t\t" + condition;

        ollirCode = ollirCode.replace(oldWhile, optWhile);

        return ollirCode;
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
            return assignment != null && assignment.endsWith(":=.bool 0.bool;");
        } else {
            if (Objects.equals(parts[0], "1.bool")) {
                return true;
            } else {
                String assignment = variableAssignmentInLastBasicBlock(lines, whileLineIdx, parts[0]);
                return assignment != null && assignment.endsWith(":=.bool 1.bool;");
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

            return Utils.isInteger(assignedValue) ? Integer.parseInt(assignedValue) : null;
        }
    }

}