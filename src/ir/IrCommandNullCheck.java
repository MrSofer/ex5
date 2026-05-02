package ir;

import mips.*;
import temp.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandNullCheck extends IrCommand
{
	Temp ptr;

	public IrCommandNullCheck(Temp ptr)
	{
		this.ptr = ptr;
	}

	public Temp getDefinedTemp() { return null; }
	public List<Temp> getUsedTemps() { return Arrays.asList(ptr); }

	public void mipsMe()
	{
		MipsGenerator.getInstance().checkNullPtr(ptr);
	}
}
