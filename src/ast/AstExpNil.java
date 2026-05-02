package ast;

public class AstExpNil extends AstExp
{
	public int line;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpNil(int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.line = line;
	}

	/************************************************/
	/* The printing message for an nil exp AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST NIL EXP */
		/*******************************/

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"NIL");
	}

	public types.Type semantMe()
	{
		return types.TypeNil.getInstance();
	}

	public temp.Temp irMe()
	{
		temp.Temp t = temp.TempFactory.getInstance().getFreshTemp();
		ir.Ir.getInstance().AddIrCommand(new ir.IRcommandConstInt(t, 0));
		return t;
	}
}
