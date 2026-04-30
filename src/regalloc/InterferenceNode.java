package regalloc;

import java.util.*;
import temp.Temp;


public class InterferenceNode {
    Temp value;
    Set<InterferenceNode> edges;
    int assignedColor;

    public InterferenceNode(Temp value){
        this.value = value;
        this.edges = new HashSet<>();
        this.assignedColor = -1;
    }

    public void addEdge(InterferenceNode other){
        this.edges.add(other);
        other.edges.add(this);
    }

    public void removeEdge(InterferenceNode node){
        this.edges.remove(node);
    }
}
