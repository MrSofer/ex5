package ast;

import ir.Ir;
import ir.IrCommandStore;
import ir.IrVarTable;
import temp.Temp;
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

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

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

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

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
		System.out.print("AST NODE ASSIGN STMT\n");

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
			System.out.format(">> ERROR [%d:%d] type mismatch for var of type %s := exp of type %s\n",line,line,
					t1 != null ? t1.name : "null", t2 != null ? t2.name : "null");
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
		Temp src = exp.irMe();
		if (var instanceof AstExpVarSimple)
		{
			String varName = ((AstExpVarSimple) var).name;
			String uniqueLabel = IrVarTable.getInstance().find(varName);
			String label = (uniqueLabel != null) ? uniqueLabel : varName;
			Ir.getInstance().AddIrCommand(new IrCommandStore(label, src));
		}
		// Array subscript and field access stores are not yet implemented in IR generation
		return null;
	}
}
