package ir;
import temp.*;
import mips.*;
import java.util.*;

public class IrCommandLoadString extends IrCommand {
    Temp dst;
    String value;
    public IrCommandLoadString(Temp dst, String value) { this.dst = dst; this.value = value; }
    public Temp getDefinedTemp() { return dst; }
    public void mipsMe() { MipsGenerator.getInstance().loadString(dst, value); }
}
