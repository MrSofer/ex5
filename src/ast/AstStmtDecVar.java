package ast;

import temp.Temp;
import types.*;

public class AstStmtDecVar extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDecVar var;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstStmtDecVar(AstDecVar var)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.var = var;
	}
	
	public void printMe()
	{
		var.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STMT\nDEC\nVAR"));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	public Type semantMe()
	{
		return var.semantMe();
	}

	@Override
	public boolean hasReturnStatement() {
		return false;
	}

	public Temp irMe() { return var.irMe(); }
}
