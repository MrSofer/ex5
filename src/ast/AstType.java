package ast;

public class AstType extends AstNode
{
	public int type;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstType(int type, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== type\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.type = type;
		this.line = line;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String stringType="";
		
		/*********************************/
		/* CONVERT op to a printable stringType */
		/*********************************/
		if (type == 0) {stringType = "int";}
		if (type == 1) {stringType = "string";}
		if (type == 2) {stringType = "void";}
		if (type == 3) {stringType = "id";}
		
		/*************************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*************************************/
		System.out.print("AST NODE TYPE\n");

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("TYPE(%s)",stringType));
		
	}
}
