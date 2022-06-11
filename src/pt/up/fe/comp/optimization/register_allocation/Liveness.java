package pt.up.fe.comp.optimization.register_allocation;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.optimization.register_allocation.useless.Web;
import pt.up.fe.specs.util.classmap.FunctionClassMap;

import java.util.*;

public class Liveness {
    private final List<Instruction> instructions;
    private FunctionClassMap<Instruction, Boolean> instructionMap;
    private final Map<Instruction, UseDef> useDefMap;
    private final Map<Instruction, InOut> inOutMap;
    private Set<Web> webs;

    public Liveness(List<Instruction> instructions) {
        this.instructions = instructions;
        this.useDefMap = new HashMap<>();
        this.inOutMap = new HashMap<>();
        this.setInstructionsMap();
        this.buildUseDef();
        this.buildInOut();
    }
    public void show() {
        for (var inst : instructions) {
            System.out.println("BB" + String.valueOf(inst.getId()) + " ----------------");
            inst.show();
            var use = useDefMap.get(inst).getUse();
            var def = useDefMap.get(inst).getDef();
            System.out.println("use: " + String.join(" ", use));
            System.out.println("def: " + String.join(" ", def));
            var in = inOutMap.get(inst).getIn();
            var out = inOutMap.get(inst).getOut();
            System.out.println("in: " + String.join(" ", in));
            System.out.println("out: " + String.join(" ", out));
            System.out.println("--------------------------\n");
        }
    }

    private void buildInOut() {
        for (var inst : instructions) {
            inOutMap.put(inst, new InOut());
        }
        boolean update;
        do {
            System.out.println("DO_WHILE");
            update = false;
            for (var inst : instructions) {
                var inOut = this.inOutMap.get(inst);
                var useDef = this.useDefMap.get(inst);

                // IN
                Set<String> use = new HashSet<>(useDef.getUse());
                Set<String> out = new HashSet<>(inOut.getOut());
                Set<String> def = new HashSet<>(useDef.getDef());
                out.removeAll(def);
                use.addAll(out);

                // OUT
                Set<String> newOut = new HashSet<>();
                for (Node node : inst.getSuccessors()) {
                    if (node.getNodeType() != NodeType.INSTRUCTION) continue;
                    newOut.addAll(this.inOutMap.get((Instruction) node).getIn());
                }

                InOut newInOut = new InOut(use, newOut);
                InOut oldInOut = this.inOutMap.replace(inst, newInOut);

                if (!oldInOut.equals(newInOut)) update = true;
            }
        } while (update);
    }

    private void setInstructionsMap() {
        this.instructionMap = new FunctionClassMap<>();
        this.instructionMap.put(AssignInstruction.class, this::getDefUse);
        this.instructionMap.put(CallInstruction.class, this::getDefUse);
        this.instructionMap.put(GetFieldInstruction.class, this::getDefUse);
        this.instructionMap.put(PutFieldInstruction.class, this::getDefUse);
        this.instructionMap.put(BinaryOpInstruction.class, this::getDefUse);
        this.instructionMap.put(UnaryOpInstruction.class, this::getDefUse);
        this.instructionMap.put(SingleOpInstruction.class, this::getDefUse);
        this.instructionMap.put(CondBranchInstruction.class, this::getDefUse);
        this.instructionMap.put(GotoInstruction.class, this::getDefUse);
        this.instructionMap.put(ReturnInstruction.class, this::getDefUse);
    }

    private Boolean getDefUse(AssignInstruction instruction)  {
        String def = ((Operand)instruction.getDest()).getName();
        this.useDefMap.get(instruction).addDef(def);
        Instruction rhs = instruction.getRhs();
        switch (rhs.getInstType()) {
            case CALL:
                for (var element : ((CallInstruction)rhs).getListOfOperands())
                    this.addUseToMap(instruction, element);
                break;
            case BINARYOPER:
                this.addUseToMap(instruction, ((BinaryOpInstruction)rhs).getLeftOperand());
                this.addUseToMap(instruction, ((BinaryOpInstruction)rhs).getRightOperand());
                break;
            case UNARYOPER:
                this.addUseToMap(instruction, ((UnaryOpInstruction)rhs).getOperand());
                break;
            case NOPER:
                this.addUseToMap(instruction, ((SingleOpInstruction)rhs).getSingleOperand());
                break;
        }
        return true;
    }
    private Boolean getDefUse(CallInstruction instruction)  {
        for (var element : instruction.getListOfOperands()) {
            this.addUseToMap(instruction, element);
        }
        return true;
    }
    private Boolean getDefUse(GetFieldInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getSecondOperand()); // TODO is this ok?
        return true;
    }
    private Boolean getDefUse(PutFieldInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getThirdOperand());
        return true;
    }
    private Boolean getDefUse(BinaryOpInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getLeftOperand());
        this.addUseToMap(instruction, instruction.getRightOperand());
        return true;
    }
    private Boolean getDefUse(UnaryOpInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getOperand());
        return true;
    }
    private Boolean getDefUse(SingleOpInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getSingleOperand());
        return true;
    }
    private Boolean getDefUse(CondBranchInstruction instruction)  {
        for (var element : instruction.getOperands()) {
            this.addUseToMap(instruction, element);
        }
        return true;
    }
    private Boolean getDefUse(GotoInstruction instruction)  {
        return true;
    }
    private Boolean getDefUse(ReturnInstruction instruction)  {
        this.addUseToMap(instruction, instruction.getOperand());
        return true;
    }
    private void addUseToMap(Instruction instruction, Element element) {
        if (element == null) return;
        if (element.isLiteral()) return;
        Operand operand = (Operand)element;
        if (operand.isParameter()) return;
        String name = operand.getName();
        var useDef = this.useDefMap.get(instruction);
        useDef.addUse(name);
    }

    private void buildUseDef() {
        for (var inst : instructions) {
            this.useDefMap.put(inst, new UseDef());
            this.instructionMap.apply(inst);
        }
    }

}
