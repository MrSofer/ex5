package ir;

import mips.*;

public class IrCommandReturn extends IrCommand
{
	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().returnFromFunction();
	}
}
