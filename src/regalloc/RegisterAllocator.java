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
        // Check if any function has more than 8 parameters (not counting 'this')
        for (IrCommandList it = irCommands; it != null; it = it.tail) {
            if (it.head instanceof IrCommandLabel) {
                String label = ((IrCommandLabel)it.head).getLabelName();
                java.util.List<String> params = ir.FuncParamTable.getInstance().getParams(label);
                if (params != null && params.size() >= 11) {
                    throw new Error("Register Allocation Failed");
                }
            }
        }
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
            throw new Error("Register Allocation Failed");
        }
        // 5. Update physicalRegister field in Temp objects

        return true;
    }
}