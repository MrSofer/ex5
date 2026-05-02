package ir;

import mips.*;
import temp.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandDivCheck extends IrCommand
{
	Temp divisor;

	public IrCommandDivCheck(Temp divisor)
	{
		this.divisor = divisor;
	}

	public Temp getDefinedTemp() { return null; }
	public List<Temp> getUsedTemps() { return Arrays.asList(divisor); }

	public void mipsMe()
	{
		MipsGenerator.getInstance().checkDivByZero(divisor);
	}
}
