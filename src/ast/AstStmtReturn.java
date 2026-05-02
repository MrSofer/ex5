package ast;

import ir.*;
import temp.Temp;

public class AstStmtReturn extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstExp exp;

	public int line;
	
	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtReturn(AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.exp = exp;
		this.line = 0;
	}
	
	public AstStmtReturn(AstExp exp, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.exp = exp;
		this.line = line;
	}

	/********************************************************/
	/* The printing message for a return statement AST node */
	/********************************************************/
	public void printMe()
	{
		/***********************************/
		/* AST NODE TYPE = AST RETURN STMT */
		/***********************************/

		/*****************************/
		/* RECURSIVELY PRINT exp ... */
		/*****************************/
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"RETURN");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (exp != null) AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public types.Type semantMe()
	{
		types.Type expectedReturnType = symboltable.SymbolTable.getInstance().getCurrentFunctionReturnType();
		
		if (expectedReturnType == null) {
			// Not inside a function, shouldn't happen in valid programs
			return null;
		}
		
		// Check void functions
		if (expectedReturnType == types.TypeVoid.getInstance()) {
			if (exp != null) {
				throw new Error("ERROR(" + line + ")");
			}
			return null;
		}
		
		// Check non-void functions
		if (exp == null) {
			throw new Error("ERROR(" + line + ")");
		}
		
		types.Type actualReturnType = exp.semantMe();
		
		// Check type compatibility
		if (actualReturnType == null) {
			throw new Error("ERROR(" + line + ")");
		}
		
		// Allow nil for reference types
		if (actualReturnType == types.TypeNil.getInstance()) {
			if (expectedReturnType.isClass() || expectedReturnType instanceof types.TypeArray) {
				return null;
			}
			throw new Error("ERROR(" + line + ")");
		}
		
		// Check for type match
		if (!typesMatch(expectedReturnType, actualReturnType)) {
			throw new Error("ERROR(" + line + ")");
		}
		
		return null;
	}
	
	private boolean typesMatch(types.Type expected, types.Type actual) {
		if (expected == actual) return true;
		if (expected.name.equals(actual.name)) return true;
		
		// Check if actual is a subclass of expected
		if (expected.isClass() && actual.isClass()) {
			types.TypeClass actualClass = (types.TypeClass) actual;
			types.TypeClass expectedClass = (types.TypeClass) expected;
			
			// Walk up the inheritance chain
			types.TypeClass current = actualClass;
			while (current != null) {
				if (current.name.equals(expectedClass.name)) {
					return true;
				}
				current = current.father;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean hasReturnStatement()
	{
		return true;
	}

	public Temp irMe()
	{
		if (exp != null)
		{
			Temp retTemp = exp.irMe();
			String funcName = AstDecFunc.getCurrentMipsFuncName();
			if (funcName != null)
			{
				Ir.getInstance().AddIrCommand(
						new IrCommandStore(funcName + "_retval", retTemp));
				Ir.getInstance().AddIrCommand(
						new IrCommandStore("_virtual_retval", retTemp));
			}
		}
		Ir.getInstance().AddIrCommand(new IrCommandReturn());
		return null;
	}
}
