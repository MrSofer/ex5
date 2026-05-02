package ir;

import mips.*;
import java.util.List;

public class IrCommandSaveGlobals extends IrCommand
{
	List<String> labels;

	public IrCommandSaveGlobals(List<String> labels)
	{
		this.labels = labels;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().saveGlobals(labels);
	}
}
