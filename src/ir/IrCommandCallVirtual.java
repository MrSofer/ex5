package ir;
import mips.*;
import temp.*;
import java.util.*;

public class IrCommandCallVirtual extends IrCommand {
    Temp objPtr;
    int vtableIndex;

    public IrCommandCallVirtual(Temp objPtr, int vtableIndex) {
        this.objPtr = objPtr;
        this.vtableIndex = vtableIndex;
    }

    public List<Temp> getUsedTemps() { return Arrays.asList(objPtr); }

    public void mipsMe() {
        MipsGenerator.getInstance().callVirtual(objPtr, vtableIndex);
    }
}
