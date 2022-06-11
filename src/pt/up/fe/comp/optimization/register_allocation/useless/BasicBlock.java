package pt.up.fe.comp.optimization.register_allocation.useless;

import org.specs.comp.ollir.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicBlock {
    private int id;
    private int start;
    private int end;
    private List<BasicBlock> successors;
    private List<BasicBlock> predecessors;
    private List<Instruction> instructions;


    public BasicBlock(int id, List<Instruction> instructions) {
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.id = id;
        this.instructions = instructions;
        this.start = instructions.get(0).getId();
        this.end = instructions.get(instructions.size()-1).getId();
    }
    public void show() {
        System.out.println("BB"+id);
        System.out.println("Pred: " + predecessors.stream().map(bb -> String.valueOf(bb.id)).collect(Collectors.joining(" ")));
        System.out.println("Succ: " + successors.stream().map(bb -> String.valueOf(bb.id)).collect(Collectors.joining(" ")));
        for (var inst : instructions) {
            inst.show();
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
    public List<Instruction> getInstructions() {
        return instructions;
    }
    public Instruction getFirstInstruction() {
        return instructions.get(0);
    }
    public Instruction getLastInstruction() {
        return instructions.get(instructions.size()-1);
    }

    public void addPredecessor(BasicBlock bb) {
        predecessors.add(bb);
    }
    public void addSuccessor(BasicBlock bb) {
        successors.add(bb);
    }

    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }
    public List<BasicBlock> getSuccessors() {
        return successors;
    }
}
