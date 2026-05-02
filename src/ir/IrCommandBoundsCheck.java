package ir;

import mips.*;
import temp.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandBoundsCheck extends IrCommand
{
	Temp ptr;
	Temp idx;

	public IrCommandBoundsCheck(Temp ptr, Temp idx)
	{
		this.ptr = ptr;
		this.idx = idx;
	}

	public Temp getDefinedTemp() { return null; }
	public List<Temp> getUsedTemps() { return Arrays.asList(ptr, idx); }

	public void mipsMe()
	{
		MipsGenerator.getInstance().checkArrayBounds(ptr, idx);
	}
}
