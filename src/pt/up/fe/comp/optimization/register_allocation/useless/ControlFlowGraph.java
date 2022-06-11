package pt.up.fe.comp.optimization.register_allocation.useless;

import org.specs.comp.ollir.*;

import java.util.*;

public class ControlFlowGraph {
    private Method method;
    private Map<Instruction, Boolean> leaders;
    private final List<BasicBlock> cfg = new ArrayList<>();

    public ControlFlowGraph(Method method) {
        this.method = method;
        this.buildLeaders();
        this.buildBasicBlocks();
        this.connectBasicBlocks();
    }

    private void connectBasicBlocks() {
        for (var bb : cfg) {
            var first = bb.getFirstInstruction();
            var last = bb.getLastInstruction();
            for (var inst : first.getPredecessors()) {
                if (inst.getNodeType() != NodeType.BEGIN) {
                    var prev = findBasicBlock(inst.getId());
                    if (prev != null) bb.addPredecessor(prev);
                }
            }
            for (var inst : last.getSuccessors()) {
                if (inst.getNodeType() != NodeType.END) {
                    var next = findBasicBlock(inst.getId());
                    if (next != null) bb.addSuccessor(next);
                }
            }
        }
    }

    private BasicBlock findBasicBlock(int instructionId) {
        for (var bb : cfg) {
            if (instructionId >= bb.getStart() && instructionId <= bb.getEnd()) return bb;
        }
        return null;
    }

    private void buildBasicBlocks() {
        int id = 1;
        List<Instruction> block = null;
        for (var inst : method.getInstructions()) {
            if (leaders.get(inst)) {
                if (block != null) cfg.add(new BasicBlock(id++, block));
                block = new ArrayList<>();
            }
            assert block != null;
            block.add(inst);
        }
        cfg.add(new BasicBlock(id, block));
    }

    private void buildLeaders() {
        leaders = new HashMap<>();
        List<Instruction> instructions = method.getInstructions();
        int nInstructions = instructions.size();

        for (var inst : instructions) leaders.put(inst, false);
        leaders.replace(method.getInstr(0), true);

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < nInstructions; i++) {
                Instruction src = method.getInstr(i);
                if (src.getInstType() == InstructionType.BRANCH) {
                    Instruction dest1 = method.getLabels().get(((CondBranchInstruction)src).getLabel());
                    Instruction dest2 = i+1 < nInstructions ? method.getInstr(i+1) : null;
                    if (dest1 != null && Boolean.FALSE.equals(leaders.replace(dest1, true)))
                        changed = true;
                    if (dest2 != null && Boolean.FALSE.equals(leaders.replace(dest2, true)))
                        changed = true;
                }
                else if (src.getInstType() == InstructionType.GOTO) {
                    Instruction dest1 = method.getLabels().get(((GotoInstruction)src).getLabel());
                    Instruction dest2 = i+1 < nInstructions ? method.getInstr(i+1) : null;
                    if (dest1 != null && Boolean.FALSE.equals(leaders.replace(dest1, true)))
                        changed = true;
                    if (dest2 != null && Boolean.FALSE.equals(leaders.replace(dest2, true)))
                        changed = true;
                }
            }
        } while (changed);
    }

    public List<BasicBlock> getCfg() {
        return cfg;
    }
}
