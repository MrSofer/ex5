package ir;

import temp.*;
import mips.*;
import java.util.*;

public class IrCommandPtrAdd extends IrCommand {
    public Temp dst, base, offset;
    public IrCommandPtrAdd(Temp dst, Temp base, Temp offset) {
        this.dst = dst; this.base = base; this.offset = offset;
    }
    public Temp getDefinedTemp() { return dst; }
    public List<Temp> getUsedTemps() { return Arrays.asList(base, offset); }
    public void mipsMe() { MipsGenerator.getInstance().ptrAdd(dst, base, offset); }
}
