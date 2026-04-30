package regalloc;

import java.util.*;
import temp.Temp;


public class InterferenceNode {
    Temp value;
    Set<InterferenceNode> edges;

    public InterferenceNode(Temp value){
        this.value = value;
        this.edges = new HashSet<>();
    }

    public void addEdge(InterferenceNode other){
        this.edges.add(other);
        other.edges.add(this);
    }
}
