package ir;

import temp.*;
import mips.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandAllocateHeap extends IrCommand
{
	Temp size;
	Temp result;

	public IrCommandAllocateHeap(Temp size, Temp result)
	{
		this.size   = size;
		this.result = result;
	}

	public Temp getDefinedTemp() { return result; }
	public List<Temp> getUsedTemps() { return Arrays.asList(size); }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().allocateHeap(size, result);
	}
}
