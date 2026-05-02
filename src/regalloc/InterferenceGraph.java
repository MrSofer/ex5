package regalloc;

import java.util.*;

import temp.Temp;

public class InterferenceGraph {

    public Map<Temp, InterferenceNode> allNodes;

    public void setControlGraph(ControlFlowGraph CFG){

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

    public boolean ColorGraph(){
        Stack<InterferenceNode> coloringStack = new Stack<>();

        List<InterferenceNode> activeNodes = new ArrayList<>(allNodes.values());
        System.out.println("active node intiial size: "+activeNodes.size());
        while (!activeNodes.isEmpty()) {
            InterferenceNode nodeToRemove = null;

            for (InterferenceNode node : activeNodes) {
                System.out.println(node.edges.size());
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
        System.out.println("coloring stack initial size: "+coloringStack.size());
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
                    System.out.println("colored with color: "+color);
                    break;
                }
            }
            for (InterferenceNode neighbor : poppedNode.edges) {
                neighbor.edges.add(poppedNode);
            }
        }

        return true;
    }

    /**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static InterferenceGraph instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected InterferenceGraph() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static InterferenceGraph getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new InterferenceGraph();


		}
		return instance;
	}

}
