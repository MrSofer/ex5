package ir;
import mips.*;
import temp.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandLoadAddr extends IrCommand {
    Temp dst;
    String label;

    public IrCommandLoadAddr(Temp dst, String label) {
        this.dst = dst;
        this.label = label;
    }

    public Temp getDefinedTemp() { return dst; }
    public List<Temp> getUsedTemps() { return Arrays.asList(); }

    public void mipsMe() {
        MipsGenerator.getInstance().loadAddress(dst, label);
    }
}
