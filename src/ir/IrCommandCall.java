package ir;

import mips.*;

public class IrCommandCall extends IrCommand
{
	String funcName;

	public IrCommandCall(String funcName)
	{
		this.funcName = funcName;
	}

	public String getFuncName() { return funcName; }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().callFunction(funcName);
	}
}
