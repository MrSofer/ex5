/***********/
/* PACKAGE */
/***********/
package ast;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;
import symboltable.*;

public class AstTypeName extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String type;
	public String name;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstTypeName(String type, String name, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		this.type = type;
		this.name = name;
		this.line = line;
	}

	/*************************************************/
	/* The printing message for a type name AST node */
	/*************************************************/
	public void printMe()
	{
		/**************************************/
		/* AST NODE TYPE = AST TYPE NAME NODE */
		/**************************************/
		System.out.format("NAME(%s):TYPE(%s)\n",name,type);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("NAME:TYPE\n%s:%s",name,type));
	}

	public Type semantMe()
	{
		// check that type isn't void
		if (type.equals("void")) {
			System.out.format(">> ERROR [%d:%d] cannot declare var of type void %s\n",line,line,type);
			throw new Error("ERROR(" + line + ")");
		}

		Type t = SymbolTable.getInstance().find(type);
		if (t == null)
		{
			/**************************/
			/* ERROR: undeclared type */
			/**************************/
			System.out.format(">> ERROR [%d:%d] undeclared type %s\n",line,line,type);
			throw new Error("ERROR(" + line + ")");
		}
		else
		{
			/*******************************************************/
			/* Enter var with name=name and type=t to symbol table */
			/*******************************************************/
//			SymbolTable.getInstance().enter(name,t);
		}


		/****************************/
		/* return (existing) type t */
		/****************************/
		return t;
	}	
}
