package regalloc;

import ir.*;
import temp.Temp;

import java.util.*;

public class ControlFlowGraph {
    List<CommandNode> allNodes;
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

            if (currNode.nodeCommand instanceof IrCommandJumpLabel) {
                IrCommandJumpLabel irJumpCmd = (IrCommandJumpLabel) currNode.nodeCommand;
                CommandNode target = JumpMap.get(irJumpCmd.getLabelName());
                currNode.successors.add(target);
                target.predecessors.add(currNode);
            } else if (currNode.nodeCommand instanceof IrCommandJumpIfEqToZero) {
                IrCommandJumpIfEqToZero irBranchCmd = (IrCommandJumpIfEqToZero) currNode.nodeCommand;
                if (i < allNodes.size() - 1) {
                    CommandNode nextNode = allNodes.get(i+1);
                    currNode.successors.add(nextNode);
                    nextNode.predecessors.add(currNode);
                }
                CommandNode target = JumpMap.get(irBranchCmd.getLabelName());
                currNode.successors.add(target);
                target.predecessors.add(currNode);
            } else {
                if (i < allNodes.size() - 1) {
                    CommandNode nextNode = allNodes.get(i+1);
                    currNode.successors.add(nextNode);
                    nextNode.predecessors.add(currNode);
                }
            }
        }

        this.allNodes = allNodes;
    }

    public Set<Temp> LivenessAnalysis() {

        // 1. Initialize with ALL nodes (you need to make allNodes accessible!)
        Queue<CommandNode> WorkList = new LinkedList<>(this.allNodes);

        while (!WorkList.isEmpty()) {
            CommandNode currNode = WorkList.poll();

            // 2. CAPTURE THE OLD 'IN' STATE BEFORE TOUCHING ANYTHING
            Set<Temp> oldIn = new HashSet<>(currNode.in);

            // 3. Calculate OUT
            for (CommandNode successor : currNode.successors) {
                currNode.out.addAll(successor.in);
            }

            // 4. Calculate IN: IN = (OUT - DEF) + USE
            currNode.in = new HashSet<>(currNode.out); // Reset IN to be exactly OUT

            Temp def = currNode.nodeCommand.getDefinedTemp();
            if (def != null) {
                currNode.in.remove(def);
            }

            List<Temp> uses = currNode.nodeCommand.getUsedTemps();
            if (uses != null) {
                for (Temp u : uses) {
                    if (u != null) currNode.in.add(u);
                }
            }

            // 5. THE TRIGGER: Did 'IN' actually change?
            if (!currNode.in.equals(oldIn)) {
                // Only if it grew do we wake up the predecessors!
                for (CommandNode pred : currNode.predecessors) {
                    if (!WorkList.contains(pred)) {
                        WorkList.add(pred);
                    }
                }
            }
        }
        return (this.first != null) ? this.first.in : new HashSet<>();
    }

}
