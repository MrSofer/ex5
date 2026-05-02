package ast;

import temp.Temp;
import types.*;

public class AstDecList extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDec head;
	public AstDecList tail;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecList(AstDec head, AstDecList tail)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.head = head;
		this.tail = tail;
	}

	/********************************************************/
	/* The printing message for a declaration list AST node */
	/********************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST DEC LIST */
		/********************************/

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"DEC\nLIST\n");
				
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber,head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber,tail.serialNumber);
	}

	public Type semantMe()
	{
		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/

		// THIS IS JUST FOR THIS EX. SINCE MAIN IS THE ONLY FUNC DEC.
		if (head != null) head.semantMe();
		if (tail != null) tail.semantMe();

		return null;
	}

	public Temp irMe()
	{
		// WORKS BECAUSE MAIN IS THE ONLY FUNC DEC.
		if (head != null && head instanceof AstDecFunc) {
			if (tail != null) tail.irMe();
			if (head != null) head.irMe();
		}
		else {
			if (head != null) head.irMe();
			if (tail != null) tail.irMe();
		}
		return null;
	}

	public Temp irMeTopLevel()
	{
		// Step 0a: Pre-register all class data members for field init (needed before global_init)
		for (AstDecList it = this; it != null; it = it.tail) {
			if (it.head instanceof AstDecClass c) {
				ClassRegistry.getInstance().register(c.name, c.dataMembers);
			}
		}

		// Step 0b: Emit vtable data (vtables already built in Main.java before preRegisterParams)
		VtableRegistry.getInstance().emitVtables();

		// Step 1: global_init function wrapping all global var declarations
		ir.Ir.getInstance().AddIrCommand(new ir.IrCommandLabel("global_init"));
		for (AstDecList it = this; it != null; it = it.tail) {
			if (it.head != null && !(it.head instanceof AstDecFunc) && !(it.head instanceof AstDecClass)) {
				it.head.irMe();
			}
		}
		ir.Ir.getInstance().AddIrCommand(new ir.IrCommandReturn());

		// Step 2: emit class methods
		for (AstDecList it = this; it != null; it = it.tail) {
			if (it.head instanceof AstDecClass c) c.irMe();
		}

		// Step 3: emit regular functions
		for (AstDecList it = this; it != null; it = it.tail) {
			if (it.head instanceof AstDecFunc f) f.irMe();
		}
		return null;
	}
}
