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

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> NIL\n");

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
		System.out.print("AST NODE NIL\n");

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
}
