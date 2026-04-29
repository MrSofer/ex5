/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import temp.Temp;
import java.util.List;
/*******************/
/* PROJECT IMPORTS */
/*******************/

public abstract class IrCommand
{
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int label_counter=0;
	public    static String getFreshLabel(String msg)
	{
		return String.format("Label_%d_%s",label_counter++,msg);
	}
	public Temp getDefinedTemp(){return null;}
	public List<Temp> getUsedTemps() {return null;}
	/***************/
	/* MIPS me !!! */
	/***************/
	public abstract void mipsMe();
}
