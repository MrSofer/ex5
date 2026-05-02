package ir;

import mips.*;
import java.util.List;

public class IrCommandCall extends IrCommand
{
	String funcName;
	List<String> callerParams;

	public IrCommandCall(String funcName, List<String> callerParams)
	{
		this.funcName = funcName;
		this.callerParams = callerParams;
	}

	public String getFuncName() { return funcName; }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		MipsGenerator.getInstance().callFunction(funcName, callerParams);
	}
}
