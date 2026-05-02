package ir;
import temp.*;
import mips.*;
import java.util.*;

public class IrCommandPrintStringVar extends IrCommand {
    Temp t;
    public IrCommandPrintStringVar(Temp t) { this.t = t; }
    public List<Temp> getUsedTemps() { return Arrays.asList(t); }
    public void mipsMe() { MipsGenerator.getInstance().printStringFromReg(t); }
}
