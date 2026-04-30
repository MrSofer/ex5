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

            List<Temp> liveTemps = new ArrayList<>(currNode.in);

            for (int i = 0; i < liveTemps.size(); i++) {
                for (int j = i + 1; j < liveTemps.size(); j++) {

                    Temp t1 = liveTemps.get(i);
                    Temp t2 = liveTemps.get(j);

                    InterferenceNode node1 = this.allNodes.get(t1);
                    InterferenceNode node2 = this.allNodes.get(t2);

                    // Draw the bidirectional edge
                    node1.addEdge(node2);
                }
            }
        }
    }
}
