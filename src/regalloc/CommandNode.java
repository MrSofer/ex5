package regalloc;

import temp.Temp;
import java.util.*;
import ir.*;

public class CommandNode {
    IrCommand nodeCommand;
    List<CommandNode> successors,predecessors;
    Set<Temp> in,out;

    public CommandNode(IrCommand ircmd){
        this.nodeCommand = ircmd;
        this.successors = new ArrayList<CommandNode>();
        this.predecessors = new ArrayList<CommandNode>();
        this.in = new HashSet<Temp>();
        this.out = new HashSet<Temp>();
    }
}
