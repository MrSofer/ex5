package ast;

import types.*;

public abstract class AstStmt extends AstNode
{
	/***********************************************/
	/* The default semantic action for an AST node */
	/***********************************************/
	public Type semantMe()
	{
		return null;
	}
	
	/**
	 * Check if this statement contains a return statement
	 * Default is false, subclasses override as needed
	 */
	public abstract boolean hasReturnStatement();
}
