package ast;

import ir.Ir;
import ir.IrCommandAllocate;
import ir.IrCommandStore;
import ir.IrVarTable;
import temp.Temp;
import types.*;
import symboltable.*;

import java.util.Objects;

public class AstDecVar extends AstDec
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String type;
	public String name;
	public AstExp initialValue;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecVar(String type, String name, AstExp initialValue, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.type = type;
		this.name = name;
		this.initialValue = initialValue;
		this.line = line;
	}

	/************************************************************/
	/* The printing message for a variable declaration AST node */
	/************************************************************/
	public void printMe()
	{
		/****************************************/
		/* AST NODE TYPE = AST VAR DECLARATION */
		/***************************************/
		if (initialValue != null) System.out.format("VAR-DEC(%s):%s := initialValue\n",name,type);
		if (initialValue == null) System.out.format("VAR-DEC(%s):%s                \n",name,type);

		/**************************************/
		/* RECURSIVELY PRINT initialValue ... */
		/**************************************/
		if (initialValue != null) initialValue.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("VAR\nDEC(%s)\n:%s",name,type));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (initialValue != null) AstGraphviz.getInstance().logEdge(serialNumber,initialValue.serialNumber);
			
	}

	public Type semantMe()
	{
		Type t;
	
		/****************************/
		/* [1] Check If Type exists */
		/****************************/
		t = SymbolTable.getInstance().find(type);
		if (t == null)
		{
			System.out.format(">> ERROR [%d:%d] non existing type %s\n",line,line,type);
			throw new Error("ERROR(" + line + ")");
		}

		// check that type isn't void
		if (type.equals( "void")) {
			System.out.format(">> ERROR [%d:%d] cannot declare var of type void %s\n",line,line,type);
			throw new Error("ERROR(" + line + ")");
		}
		
		/**************************************/
		/* [2] Check That Name does NOT exist in current scope */
		/**************************************/
		if (SymbolTable.getInstance().findInCurrentScope(name) != null)
		{
			System.out.format(">> ERROR [%d:%d] variable %s already exists in scope\n",line,line,name);
			throw new Error("ERROR(" + line + ")");
		}

		/************************************************/
		/* [3] Check initial value type if present */
		/************************************************/
		if (initialValue != null) {
			Type initType = initialValue.semantMe();
			
			// Check type compatibility
			boolean typesMatch = false;
			
			if (t == initType) {
				typesMatch = true;
			} else if (t != null && initType != null && t.name.equals(initType.name)) {
				typesMatch = true;
			} else if (initType == TypeNil.getInstance() && t != null && (t.isClass() || t.isArray())) {
				typesMatch = true;
			} else if (t != null && initType != null && t.isArray() && initType.isArray()) {
				// Both are arrays - check element types
				TypeArray ta = (TypeArray) t;
				TypeArray initArray = (TypeArray) initType;
				if (ta.elementType == initArray.elementType ||
				    (ta.elementType != null && initArray.elementType != null && 
				     ta.elementType.name.equals(initArray.elementType.name))) {
					typesMatch = true;
				}
			} else if (t != null && initType != null && t.isClass() && initType.isClass()) {
				// Check if initType is a subclass of t
				TypeClass tc = (TypeClass) t;
				TypeClass initClass = (TypeClass) initType;
				TypeClass current = initClass;
				while (current != null) {
					if (current.name.equals(tc.name)) {
						typesMatch = true;
						break;
					}
					current = current.father;
				}
			}
			
			if (!typesMatch) {
				System.out.format(">> ERROR [%d:%d] type mismatch for variable initialization\n",line,line);
				throw new Error("ERROR(" + line + ")");
			}
		}

		/************************************************/
		/* [4] Enter the Identifier to the Symbol Table */
		/************************************************/
		SymbolTable.getInstance().enter(name,t);

		/************************************************************/
		/* [5] Return value is irrelevant for variable declarations */
		/************************************************************/
		return null;		
	}

	public Temp irMe()
	{
		String uniqueLabel = IrVarTable.getInstance().allocate(name);
		Ir.getInstance().AddIrCommand(new IrCommandAllocate(uniqueLabel));

		if (initialValue != null)
		{
			Ir.getInstance().AddIrCommand(new IrCommandStore(uniqueLabel,initialValue.irMe()));
		}
		return null;
	}
}
