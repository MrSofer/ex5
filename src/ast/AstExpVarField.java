package ast;

import types.*;

public class AstExpVarField extends AstExpVar {
	public AstExpVar var;
	public String fieldName;
	public int line;
	private types.TypeClass cachedObjectType = null;

	public types.TypeClass getCachedObjectType() { return cachedObjectType; }
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarField(AstExpVar var, String fieldName)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.var = var;
		this.fieldName = fieldName;
		this.line = 0;
	}

	public AstExpVarField(AstExpVar var, String fieldName, int line)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.var = var;
		this.fieldName = fieldName;
		this.line = line;
	}

	/*************************************************/
	/* The printing message for a field var AST node */
	/*************************************************/
	public void printMe()
	{
		/*********************************/
		/* AST NODE TYPE = AST FIELD VAR */
		/*********************************/
		System.out.format("FIELD\nNAME\n(___.%s)\n",fieldName);

		/**********************************************/
		/* RECURSIVELY PRINT VAR, then FIELD NAME ... */
		/**********************************************/
		if (var != null) var.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("FIELD\nVAR\n___.%s",fieldName));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var  != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	public Type semantMe()
	{
		Type t = null;
		TypeClass tc = null;
		
		/******************************/
		/* [1] Recursively semant var */
		/******************************/
		if (var != null) t = var.semantMe();
		if (t != null && t.isClass()) cachedObjectType = (TypeClass) t;
		
		/*********************************/
		/* [2] Make sure type is a class */
		/*********************************/
		if (t.isClass() == false)
		{
			System.out.format(">> ERROR [%d:%d] access %s field of a non-class variable\n",line,line,fieldName);
			throw new Error("ERROR(" + line + ")");
		}
		else
		{
			tc = (TypeClass) t;
		}
		
		/************************************/
		/* [3] Look for fieldName inside tc and its ancestors */
		/************************************/
		TypeClass currentClass = tc;
		while (currentClass != null)
		{
			for (TypeClassVarDecList it = currentClass.dataMembers; it != null; it=it.tail)
			{
				if (it.head.name.equals(fieldName))
				{
					return it.head.t;
				}
			}
			// Move to parent class
			currentClass = currentClass.father;
		}
		
		/*********************************************/
		/* [4] fieldName does not exist in class var or its ancestors */
		/*********************************************/
		System.out.format(">> ERROR [%d:%d] field %s does not exist in class\n",line,line,fieldName);
		throw new Error("ERROR(" + line + ")");
	}

	public temp.Temp irMe()
	{
		temp.Temp objPtr = var.irMe();
		if (cachedObjectType != null) {
			int offset = ClassContext.getFieldOffset(cachedObjectType, fieldName);
			if (offset >= 0) {
				temp.Temp offTemp = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IRcommandConstInt(offTemp, offset));
				temp.Temp addr = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandPtrAdd(addr, objPtr, offTemp));
				temp.Temp result = temp.TempFactory.getInstance().getFreshTemp();
				ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLoadIndirect(result, addr));
				return result;
			}
		}
		return objPtr;
	}
}
