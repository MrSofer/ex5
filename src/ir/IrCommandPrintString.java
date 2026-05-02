package ir;
import mips.*;

public class IrCommandPrintString extends IrCommand {
    String value;
    public IrCommandPrintString(String value) { this.value = value; }
    public void mipsMe() { MipsGenerator.getInstance().printString(value); }
}
