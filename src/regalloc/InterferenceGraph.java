package regalloc;

import java.util.*;
import temp.Temp;

public class InterferenceGraph {

    Map<Temp, InterferenceNode> allNodes;

    public InterferenceGraph(ControlFlowGraph CFG){

        this.allNodes = new HashMap<>();

        for (CommandNode node : CFG.allNodes){
            for (Temp currTemp : node.in) {this.allNodes.putIfAbsent(currTemp, new InterferenceNode(currTemp));}

            Temp defTemp = node.nodeCommand.getDefinedTemp();
            if (defTemp != null) {
                this.allNodes.putIfAbsent(defTemp, new InterferenceNode(defTemp));
            }
        }

        for (CommandNode currNode : CFG.allNodes) {

            // Add pairwise edges for all temps live at entry of this node
            List<Temp> liveInTemps = new ArrayList<>(currNode.in);
            for (int i = 0; i < liveInTemps.size(); i++) {
                for (int j = i + 1; j < liveInTemps.size(); j++) {
                    Temp t1 = liveInTemps.get(i);
                    Temp t2 = liveInTemps.get(j);
                    InterferenceNode node1 = this.allNodes.get(t1);
                    InterferenceNode node2 = this.allNodes.get(t2);
                    // Draw the bidirectional edge
                    node1.addEdge(node2);
                }
            }

            // Add pairwise edges for all temps live at exit of this node.
            // This is needed to catch pairs that are simultaneously live at a branch
            // point but end up in different successors' in-sets.
            List<Temp> liveOutTemps = new ArrayList<>(currNode.out);
            for (int i = 0; i < liveOutTemps.size(); i++) {
                for (int j = i + 1; j < liveOutTemps.size(); j++) {
                    Temp t1 = liveOutTemps.get(i);
                    Temp t2 = liveOutTemps.get(j);
                    InterferenceNode node1 = this.allNodes.get(t1);
                    InterferenceNode node2 = this.allNodes.get(t2);
                    if (node1 != null && node2 != null) {
                        node1.addEdge(node2);
                    }
                }
            }

            // Add edges between the defined temp and every temp live at exit.
            // The defined temp is already removed from in[node] by liveness analysis,
            // so it would never get any edges from the in-set loops above.
            Temp def = currNode.nodeCommand.getDefinedTemp();
            if (def != null) {
                InterferenceNode defNode = this.allNodes.get(def);
                for (Temp t : currNode.out) {
                    if (!t.equals(def)) {
                        InterferenceNode tNode = this.allNodes.get(t);
                        if (tNode != null) {
                            defNode.addEdge(tNode);
                        }
                    }
                }
            }
        }
    }

    public boolean ColorGraph(){
        Stack<InterferenceNode> coloringStack = new Stack<>();

        List<InterferenceNode> activeNodes = new ArrayList<>(allNodes.values());

        while (!activeNodes.isEmpty()) {
            InterferenceNode nodeToRemove = null;

            for (InterferenceNode node : activeNodes) {
                if (node.edges.size() < 10) {
                    nodeToRemove = node;
                    break; // Found one! Stop hunting.
                }
            }

            if (nodeToRemove == null) {
                return false;
            }

            activeNodes.remove(nodeToRemove);
            coloringStack.push(nodeToRemove);

            for (InterferenceNode neighbor : nodeToRemove.edges) {
                neighbor.removeEdge(nodeToRemove);
            }
        }

        while (! coloringStack.isEmpty()) {
            InterferenceNode poppedNode = coloringStack.pop();

            Set<Integer> used = new HashSet<>();
            for (InterferenceNode neighbor : poppedNode.edges){
                if (neighbor.assignedColor != -1){
                    used.add(neighbor.assignedColor);
                }
            }

            for (int color = 0 ; color < 10 ; color++){
                if (!used.contains(color)){
                    poppedNode.assignedColor = color;
                    break;
                }
            }
            for (InterferenceNode neighbor : poppedNode.edges) {
                neighbor.edges.add(poppedNode);
            }
        }

        return true;
    }

}
