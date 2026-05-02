/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.Arrays;
import java.util.List;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.Temp;
import mips.*;

public class IrCommandBinopGtIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;

	public IrCommandBinopGtIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	public List<Temp> getUsedTemps() { return Arrays.asList(t1, t2); }
	public Temp getDefinedTemp() { return dst; }

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		String labelEnd        = getFreshLabel("end");
		String labelAssignOne  = getFreshLabel("AssignOne");
		String labelAssignZero = getFreshLabel("AssignZero");

		MipsGenerator.getInstance().bgt(t1, t2, labelAssignOne);
		MipsGenerator.getInstance().ble(t1, t2, labelAssignZero);

		MipsGenerator.getInstance().label(labelAssignOne);
		MipsGenerator.getInstance().li(dst, 1);
		MipsGenerator.getInstance().jump(labelEnd);

		MipsGenerator.getInstance().label(labelAssignZero);
		MipsGenerator.getInstance().li(dst, 0);
		MipsGenerator.getInstance().jump(labelEnd);

		MipsGenerator.getInstance().label(labelEnd);
	}
}
