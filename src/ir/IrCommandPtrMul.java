package ir;

import temp.*;
import mips.*;
import java.util.*;

public class IrCommandPtrMul extends IrCommand {
    public Temp dst, t1, t2;
    public IrCommandPtrMul(Temp dst, Temp t1, Temp t2) {
        this.dst = dst; this.t1 = t1; this.t2 = t2;
    }
    public Temp getDefinedTemp() { return dst; }
    public List<Temp> getUsedTemps() { return Arrays.asList(t1, t2); }
    public void mipsMe() { MipsGenerator.getInstance().ptrMul(dst, t1, t2); }
}
