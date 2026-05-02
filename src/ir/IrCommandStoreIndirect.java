package ir;

import temp.*;
import mips.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandStoreIndirect extends IrCommand
{
	Temp base;
	Temp src;

	public IrCommandStoreIndirect(Temp base, Temp src)
	{
		this.base = base;
		this.src  = src;
	}

	public List<Temp> getUsedTemps() { return Arrays.asList(base, src); }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().storeIndirect(base, src);
	}
}
