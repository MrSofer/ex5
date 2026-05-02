package ir;

import temp.*;
import mips.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandLoadIndirect extends IrCommand
{
	Temp dst;
	Temp base;

	public IrCommandLoadIndirect(Temp dst, Temp base)
	{
		this.dst  = dst;
		this.base = base;
	}

	public Temp getDefinedTemp() { return dst; }
	public List<Temp> getUsedTemps() { return Arrays.asList(base); }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().loadIndirect(dst, base);
	}
}
