package ir;

import mips.*;
import java.util.List;

public class IrCommandRestoreGlobals extends IrCommand
{
	List<String> labels;

	public IrCommandRestoreGlobals(List<String> labels)
	{
		this.labels = labels;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().restoreGlobals(labels);
	}
}
