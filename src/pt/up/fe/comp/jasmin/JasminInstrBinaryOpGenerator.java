package pt.up.fe.comp.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;

public class JasminInstrBinaryOpGenerator {
    private BinaryOpInstruction instruction;
    private HashMap<String, Descriptor> varTable;

    private int labelCounter;

    private boolean insideCondBranchInstruction = false;
    private String condBranchInstructionLabel = null;
    private String condBranchInstructionFalseLabel = null;
    private boolean completedCondBranchInstruction = false;

    public JasminInstrBinaryOpGenerator() {
        this.labelCounter = 0;
    }

    public JasminInstrBinaryOpGenerator(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        this.instruction = instruction;
        this.varTable = varTable;

        this.labelCounter = 0;
    }

    public String nextLabel() {
        return "label" + this.labelCounter++;
    }

    public void setInstruction(BinaryOpInstruction instruction) {
        this.instruction = instruction;
    }

    public void setVarTable(HashMap<String, Descriptor> varTable) {
        this.varTable = varTable;
    }

    public void resetLabelCounter() {
        this.labelCounter = 0;
    }

    public void setCondBranchInstruction(boolean insideCondBranchInst, String condBranchLabel, String falseLabel) {
        this.insideCondBranchInstruction = insideCondBranchInst;
        this.condBranchInstructionLabel = condBranchLabel;
        this.condBranchInstructionFalseLabel = falseLabel;
    }

    public void setCompletedCondBranchInstruction(boolean completedCondBranchInst) {
        this.completedCondBranchInstruction = completedCondBranchInst;
    }

    public void resetCondBranchInstruction() {
        this.setCondBranchInstruction(false, null, null);
        this.setCompletedCondBranchInstruction(false);
    }

    public boolean insideCondBranchInstruction() {
        return this.insideCondBranchInstruction;
    }

    public String getCondBranchInstructionLabel() {
        return this.condBranchInstructionLabel;
    }

    public boolean completedCondBranchInstruction() {
        return this.completedCondBranchInstruction;
    }

    public String getJasminCode() {
        OperationType opType = this.instruction.getOperation().getOpType();

        if (Arrays.asList(OperationType.ADD, OperationType.SUB, OperationType.MUL, OperationType.DIV).contains(opType))
            return this.getBinaryArithmeticOperationCode();
        else if (Arrays.asList(OperationType.EQ, OperationType.GTE, OperationType.GTH, OperationType.LTE,
                OperationType.LTH, OperationType.NEQ, OperationType.AND, OperationType.ANDB,
                OperationType.OR, OperationType.ORB, OperationType.NOT, OperationType.NOTB, OperationType.XOR).contains(opType))
            return this.getBinaryRelationOperationCode();

        throw new NotImplementedException(this.instruction.getOperation().getOpType());
    }

    private String getBinaryArithmeticOperationCode() {
        StringBuilder code = new StringBuilder();

        code.append(JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable));
        code.append(JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable));

        String typePrefix = JasminUtils.getElementTypePrefix(this.instruction.getLeftOperand());
        switch (this.instruction.getOperation().getOpType()) {
            case ADD:
                code.append("\t").append(typePrefix).append("add\n"); break;
            case SUB:
                code.append("\t").append(typePrefix).append("sub\n"); break;
            case MUL:
                code.append("\t").append(typePrefix).append("mul\n"); break;
            case DIV:
                code.append("\t").append(typePrefix).append("div\n"); break;
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
        }

        JasminLimits.decrementStack(1);
        return code.toString();
    }

    private String getBinaryRelationOperationCode() {
        StringBuilder code = new StringBuilder();
        OperationType operationType = this.instruction.getOperation().getOpType();

        String typePrefix = JasminUtils.getElementTypePrefix(this.instruction.getLeftOperand());
        switch (operationType) {
            case EQ:
            case GTE:
            case GTH:
            case LTE:
            case LTH:
            case NEQ:
                if (this.insideCondBranchInstruction()) {
                    Element leftOp = this.instruction.getLeftOperand();
                    Element rightOP = this.instruction.getRightOperand();

                    if (JasminUtils.isLiteralZero(leftOp) && JasminUtils.isVariableNotArray(rightOP)) {
                        return code.append(this.getComparisonZeroOptimizedCode(operationType, true)).toString();
                    }
                    if (JasminUtils.isVariableNotArray(leftOp) && JasminUtils.isLiteralZero(rightOP)) {
                        return code.append(this.getComparisonZeroOptimizedCode(operationType,false)).toString();
                    }
                }

                code.append(this.loadInstructionOperands());
                String comparison = this.getComparisonInstructionCode(operationType);
                code.append(this.getBinaryBooleanJumpsCode(comparison, nextLabel(), nextLabel()));
                break;
            case AND:
            case OR:
            case XOR:
            case NOT:
                code.append(this.loadInstructionOperands());
                code.append("\t").append(typePrefix).append(this.getRelationInstructionCode(operationType)).append("\n");
                break;
            case ANDB:
                if (this.insideCondBranchInstruction() && this.condBranchInstructionFalseLabel != null) {
                    code.append(this.loadInstructionLeftOperand());
                    code.append("\tifeq ").append(this.condBranchInstructionFalseLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    code.append(this.loadInstructionRightOperand());
                    code.append("\tifne ").append(this.condBranchInstructionLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    this.completedCondBranchInstruction = true;
                } else {
                    String trueLabel = nextLabel();
                    String falseLabel = nextLabel();

                    code.append(this.loadInstructionLeftOperand());
                    code.append("\tifeq ").append(falseLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    code.append(this.loadInstructionRightOperand());
                    code.append(this.getBinaryBooleanJumpsCode("ifne", trueLabel, falseLabel));
                }
                break;
            case ORB:
                if (this.insideCondBranchInstruction()) {
                    code.append(this.loadInstructionLeftOperand());
                    code.append("\tifne ").append(this.condBranchInstructionLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    code.append(this.loadInstructionRightOperand());
                    code.append("\tifne ").append(this.condBranchInstructionLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    this.completedCondBranchInstruction = true;
                } else {
                    String trueLabel = nextLabel();
                    String falseLabel = nextLabel();

                    code.append(this.loadInstructionLeftOperand());
                    code.append("\tifne ").append(trueLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    code.append(this.loadInstructionRightOperand());
                    code.append(this.getBinaryBooleanJumpsCode("ifne", trueLabel, falseLabel));
                }
                break;
            case NOTB:
                if (this.insideCondBranchInstruction()) {
                    code.append(this.loadInstructionLeftOperand());
                    code.append("\tifeq ").append(this.condBranchInstructionLabel).append("\n");
                    JasminLimits.decrementStack(1);
                    this.completedCondBranchInstruction = true;
                } else {
                    code.append(this.loadInstructionLeftOperand());
                    code.append(this.getBinaryBooleanJumpsCode("ifeq", nextLabel(), nextLabel()));
                }
                break;
            default:
                throw new NotImplementedException(this.instruction.getOperation().getOpType());
        }

        return code.toString();
    }

    private String getComparisonZeroOptimizedCode(OperationType operationType, boolean leftZero) {
        this.completedCondBranchInstruction = true;

        return (leftZero ? this.loadInstructionRightOperand() : this.loadInstructionLeftOperand()) +
                "\t" + this.getComparisonZeroInstructionCode(operationType, leftZero) +
                " " + this.condBranchInstructionLabel + "\n";
    }

    private String loadInstructionOperands() {
        return JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable) +
                JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable);
    }

    private String loadInstructionLeftOperand() {
        return JasminUtils.loadElementCode(this.instruction.getLeftOperand(), this.varTable);
    }

    private String loadInstructionRightOperand() {
        return JasminUtils.loadElementCode(this.instruction.getRightOperand(), this.varTable);
    }

    private String getComparisonInstructionCode(OperationType operationType) {
        JasminLimits.decrementStack(2);

        switch (operationType) {
            case EQ: return "if_icmpeq";
            case GTE: return "if_icmpge";
            case GTH: return "if_icmpgt";
            case LTE: return "if_icmple";
            case LTH: return "if_icmplt";
            case NEQ: return "if_icmpne";
            default: throw new NotImplementedException(operationType);
        }
    }

    private String getComparisonZeroInstructionCode(OperationType operationType, boolean leftZero) {
        JasminLimits.decrementStack(1);

        switch (operationType) {
            case EQ : return "ifeq";
            case GTE: return (leftZero) ? "ifle" : "ifge";
            case GTH: return (leftZero) ? "iflt" : "ifgt";
            case LTE: return (leftZero) ? "ifge" : "ifle";
            case LTH: return (leftZero) ? "ifgt" : "iflt";
            case NEQ: return "ifne";
            default : throw new NotImplementedException(operationType);
        }
    }

    private String getRelationInstructionCode(OperationType operationType) {
        if (operationType != OperationType.NOT) {
            JasminLimits.decrementStack(1);
        }

        switch (operationType) {
            case AND: return "and";
            case OR : return "or" ;
            case XOR: return "xor";
            case NOT: return "neg";
            default: throw new NotImplementedException(operationType);
        }
    }

    public String getBinaryBooleanJumpsCode(String comparison, String trueLabel, String falseLabel) {
        String endLabel = this.nextLabel();

        JasminLimits.incrementStack(1);
        return  "\t" + comparison + " " + trueLabel + "\n" +
                "\t" + falseLabel + ":\n" +
                "\ticonst_0\n" +
                "\tgoto " + endLabel + "\n" +
                "\t" + trueLabel + ":\n" +
                "\ticonst_1\n" +
                "\t" + endLabel + ":\n";
    }
}
