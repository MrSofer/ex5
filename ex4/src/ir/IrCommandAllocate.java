/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class IrCommandAllocate extends IrCommand
{
	private String varName;
	
	public IrCommandAllocate(String varName)
	{
		this.varName = varName;
	}

	public String getVarName()
	{
		return varName;
	}
}
