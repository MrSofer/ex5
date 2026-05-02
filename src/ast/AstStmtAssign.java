package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstStmtAssign extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstExpVar var;
	public AstExp exp;
	public int line;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstExpVar var, AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.line = 0;
	}
	
	public AstStmtAssign(AstExpVar var, AstExp exp, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.line = line;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"ASSIGN\nleft := right\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public Type semantMe()
	{
		Type t1 = null;
		Type t2 = null;
		
		if (var != null) t1 = var.semantMe();
		if (exp != null) t2 = exp.semantMe();
		
		// Check type compatibility
		boolean typesMatch = false;
		
		// Same object reference
		if (t1 == t2) {
			typesMatch = true;
		}
		// Both null
		else if (t1 == null && t2 == null) {
			typesMatch = true;
		}
		// Both have same name (covers int, string, void, and named types)
		else if (t1 != null && t2 != null && t1.name.equals(t2.name)) {
			typesMatch = true;
		}
		// One is nil and the other is a class or array
		else if (t1 == TypeNil.getInstance() && t2 != null && (t2.isClass() || t2.isArray())) {
			typesMatch = true;
		}
		else if (t2 == TypeNil.getInstance() && t1 != null && (t1.isClass() || t1.isArray())) {
			typesMatch = true;
		}
		// Subclass can be assigned to superclass
		else if (t1 != null && t2 != null && t1.isClass() && t2.isClass()) {
			TypeClass c1 = (TypeClass) t1;
			TypeClass c2 = (TypeClass) t2;
			// Check if t2 is a subclass of t1
			TypeClass current = c2;
			while (current != null) {
				if (current.name.equals(c1.name)) {
					typesMatch = true;
					break;
				}
				current = current.father;
			}
		}
		// Both are arrays - check element types
		else if (t1 != null && t2 != null && t1.isArray() && t2.isArray()) {
			types.TypeArray a1 = (types.TypeArray) t1;
			types.TypeArray a2 = (types.TypeArray) t2;
			// Arrays match if their element types match (regardless of name format)
			if (a1.elementType == a2.elementType ||
					(a1.elementType != null && a2.elementType != null &&
					 a1.elementType.name.equals(a2.elementType.name))) {
				typesMatch = true;
			}
		}

		if (!typesMatch)
		{
			throw new Error("ERROR(" + line + ")");
		}
		return null;
	}

	@Override
	public boolean hasReturnStatement() {
		return false;
	}

	public Temp irMe()
	{
		if (var instanceof AstExpVarSubscript subscriptVar)
		{
			/*****************************************************/
			/* Array subscript store: arr[idx] := exp            */
			/* 1. Load base pointer                              */
			/* 2. Load index, scale by 4, compute address        */
			/* 3. Store value at computed address                */
			/*****************************************************/
			Temp baseTemp = subscriptVar.var.irMe();
			Temp idxTemp  = subscriptVar.subscript.irMe();

			Temp four = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(new IRcommandConstInt(four, 4));
			Temp offset = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(
					new IrCommandPtrMul(offset, idxTemp, four));

			Temp addr = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(
					new IrCommandPtrAdd(addr, baseTemp, offset));

			Temp src = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandStoreIndirect(addr, src));
		}
		else if (var instanceof AstExpVarField fieldVar)
		{
			Temp objPtr = fieldVar.var.irMe();
			Temp src = exp.irMe();
		types.TypeClass tc = fieldVar.getCachedObjectType();
			if (tc != null) {
				int fieldOffset = ast.ClassContext.getFieldOffset(tc, fieldVar.fieldName);
				if (fieldOffset >= 0) {
					Temp offTemp = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IRcommandConstInt(offTemp, fieldOffset));
					Temp addr = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IrCommandPtrAdd(addr, objPtr, offTemp));
					Ir.getInstance().AddIrCommand(new IrCommandStoreIndirect(addr, src));
				}
			}
		}
		else if (var instanceof AstExpVarSimple simpleVar)
		{
			String varName = simpleVar.name;
			if (ast.ClassContext.getInstance().isInClassMethod()) {
				types.TypeClass tc = ast.ClassContext.getInstance().getCurrentClassType();
				int fieldOffset = ast.ClassContext.getFieldOffset(tc, varName);
				if (fieldOffset >= 0) {
					Temp src = exp.irMe();
					Temp thisPtr = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IrCommandLoad(thisPtr, ast.ClassContext.getInstance().getThisLabel()));
					Temp offTemp = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IRcommandConstInt(offTemp, fieldOffset));
					Temp addr = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IrCommandPtrAdd(addr, thisPtr, offTemp));
					Ir.getInstance().AddIrCommand(new IrCommandStoreIndirect(addr, src));
					return null;
				}
			}
			Temp src = exp.irMe();
			String uniqueLabel = IrVarTable.getInstance().find(varName);
			String label = (uniqueLabel != null) ? uniqueLabel : varName;
			Ir.getInstance().AddIrCommand(new IrCommandStore(label, src));
		}
		return null;
	}
}
