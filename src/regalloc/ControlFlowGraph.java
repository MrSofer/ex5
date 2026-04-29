package regalloc;

import ir.*;
import temp.Temp;

import java.util.*;

public class ControlFlowGraph {
    CommandNode first,last;

    public void build(IrCommandList ir){
        List<CommandNode> allNodes = new ArrayList<>();
        HashMap<String,CommandNode> JumpMap = new HashMap<>();
        IrCommandList currIrList = ir;
        IrCommand currIr;
        CommandNode currNode = null;
        while (currIrList != null){
            currIr = currIrList.head;
            currNode = new CommandNode(currIr);
            if (first == null) { first = currNode; }
            if (currIr instanceof IrCommandLabel currLabel){
                JumpMap.put(currLabel.getLabelName(),currNode);
            }
            allNodes.add(currNode);
            currIrList = currIrList.tail;

        }
        if (currNode != null) {last = currNode;}

        for (int i = 0 ; i < allNodes.size() ; i++) {
            currNode = allNodes.get(i);

            switch (currNode.nodeCommand) {
                case IrCommandJumpLabel irJumpCmd -> {
                    CommandNode target = JumpMap.get(irJumpCmd.getLabelName());
                    currNode.successors.add(target);
                    target.predecessors.add(currNode);
                }
                case IrCommandJumpIfEqToZero irBranchCmd -> {
                    if (i < allNodes.size() - 1) {
                        CommandNode nextNode = allNodes.get(i+1);
                        currNode.successors.add(nextNode);
                        nextNode.predecessors.add(currNode);
                    }
                    CommandNode target = JumpMap.get(irBranchCmd.getLabelName());
                    currNode.successors.add(target);
                    target.predecessors.add(currNode);
                }
                default -> {
                    if (i < allNodes.size() - 1) {
                        CommandNode nextNode = allNodes.get(i+1);
                        currNode.successors.add(nextNode);
                        nextNode.predecessors.add(currNode);
                    }
                }
            }
        }
    }

    public Set<Temp> LivenessAnalysis(){
        CommandNode currNode = null;
        Queue<CommandNode> WorkList = new LinkedList<>();
        WorkList.add(this.last);
        while (!WorkList.isEmpty()){
            currNode = WorkList.remove();
            //1. receive and unionize all successors.in to curr.out
            for (CommandNode successor: currNode.successors ){
                currNode.out.addAll(successor.in);
            }
            currNode.in = new HashSet<>(currNode.out);
            //2. apply in/out rule
            switch (currNode.nodeCommand){
                case IrCommandBinopMulIntegers irMul -> {
                    currNode.in.remove(irMul.getDefinedTemp());
                    currNode.in.addAll(irMul.getUsedTemps());
                }
                case IrCommandBinopAddIntegers irAdd -> {
                    currNode.in.remove(irAdd.getDefinedTemp());
                    currNode.in.addAll(irAdd.getUsedTemps());
                }
                case IrCommandJumpIfEqToZero irJump -> {
                    currNode.in.add(irJump.getT());
                }

                default -> {continue;}
            }
            currNode = WorkList.remove();
        }

        return (currNode != null) ? currNode.in : new HashSet<Temp>();
    }

}
