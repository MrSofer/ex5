package ir;

import mips.*;
import temp.*;
import java.util.Arrays;
import java.util.List;

public class IrCommandStringConcat extends IrCommand
{
	Temp dst;
	Temp str1;
	Temp str2;

	public IrCommandStringConcat(Temp dst, Temp str1, Temp str2)
	{
		this.dst  = dst;
		this.str1 = str1;
		this.str2 = str2;
	}

	public Temp getDefinedTemp() { return dst; }
	public List<Temp> getUsedTemps() { return Arrays.asList(str1, str2); }

	public void mipsMe()
	{
		MipsGenerator.getInstance().stringConcat(dst, str1, str2);
	}
}
