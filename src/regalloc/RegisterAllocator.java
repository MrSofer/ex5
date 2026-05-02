package regalloc;

import ir.*;
import temp.Temp;

import java.util.Set;

public class RegisterAllocator {

    private IrCommandList irCommands;

    public RegisterAllocator(IrCommandList irCommands)
    {
        this.irCommands = irCommands;
    }

    public boolean allocate()
    {
        // 1. Build Control Flow Graph (CFG) (done by klierchoo and talso)
            ControlFlowGraph CFG = new ControlFlowGraph();
            CFG.build(irCommands);
        // 2. Perform Liveness Analysis (IN and OUT sets)
            Set<Temp> LivenessResults = CFG.LivenessAnalysis();
        // 3. Build Interference Graph
            InterferenceGraph interferenceGraph = InterferenceGraph.getInstance();
            interferenceGraph.setControlGraph(CFG);
        // 4. Simplify and Color the graph (using $t0 - $t9)
        if (!interferenceGraph.ColorGraph()) {
            System.out.println("Register Allocation Failed");
            System.exit(0);
        }
        System.out.println("colored graph!!");
        // 5. Update physicalRegister field in Temp objects

        return true;
    }
}