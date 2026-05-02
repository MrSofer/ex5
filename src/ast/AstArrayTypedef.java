package ast;

import types.*;
import symboltable.*;

public class AstArrayTypedef extends AstDec
{
    public String name;
    public String elementTypeName;
    public int line;
    
    public AstArrayTypedef(String name, String elementTypeName, int line){
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        this.name = name;
        this.elementTypeName = elementTypeName;
        this.line = line;
    }

    public void printMe()
	{
        /*****************/
		/* AST NODE TYPE */
		/*****************/
		System.out.format("ARRAY TYPEDEF %s = %s[]\n", name, elementTypeName);

        /**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("ARRAY\nTYPEDEF\n%s=%s[]", name, elementTypeName));
	}

	public Type semantMe()
	{
		Type elementType = null;
	
		/****************************/
		/* [1] Check If Element Type exists */
		/****************************/

		if (elementTypeName.equals( "void")) {
			System.out.format(">> ERROR [%d:%d] cannot declare array type of type void\n",line,line);
			throw new Error("ERROR(" + line + ")");
		}

		elementType = SymbolTable.getInstance().find(elementTypeName);
		if (elementType == null)
		{
			throw new Error("ERROR(" + line + ")");
		}
		
		/**************************************/
		/* [2] Check That Name does NOT exist */
		/**************************************/
		if (SymbolTable.getInstance().find(name) != null)
		{
			System.out.format(">> ERROR [%d:%d] type %s already exists\n",line,line,name);
			throw new Error("ERROR(" + line + ")");
		}

		/************************************************/
		/* [3] Create array type and enter to Symbol Table */
		/************************************************/
		TypeArray arrayType = new TypeArray(name, elementType);
		SymbolTable.getInstance().enter(name, arrayType);

		/************************************************************/
		/* [4] Return value is irrelevant for type declarations */
		/************************************************************/
		return null;		
	}
}
