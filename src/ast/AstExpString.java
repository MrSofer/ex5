package ast;

import types.*;

public class AstExpString extends AstExp
{
	public String value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpString(String value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== exp -> STRING( %s )\n", value);
		this.value = value;
	}

	/******************************************************/
	/* The printing message for a STRING EXP AST node */
	/******************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST STRING EXP */
		/*******************************/
		System.out.format("AST NODE STRING( %s )\n",value);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STRING\n%s",value.replace('"','\'')));
	}

	public Type semantMe()
	{
		return TypeString.getInstance();
	}

	public temp.Temp irMe()
	{
		temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
		String strValue = value;
		if (strValue.startsWith("\"") && strValue.endsWith("\"") && strValue.length() >= 2) {
			strValue = strValue.substring(1, strValue.length() - 1);
		}
		ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoadString(t, strValue));
		return t;
	}
}
