package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;

public class AstExpNew extends AstExp
{
    public String typeName;
    public AstExp arraySize;
    public int line;

    public AstExpNew(String typeName, AstExp arraySize, int line){
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        this.typeName = typeName;
        this.arraySize = arraySize;
        this.line = line;
    }
    
    public void printMe()
	{
        /*****************/
		/* AST NODE TYPE */
		/*****************/
		System.out.format("AST NODE EXP NEW %s\n", typeName);

        /******************/
		/* RECURSIVE PRINT*/
		/******************/
        if (arraySize != null) arraySize.printMe();

        /**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("NEW\n%s", typeName));

        /****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
        if (arraySize != null) AstGraphviz.getInstance().logEdge(serialNumber, arraySize.serialNumber);
    }

    public types.Type semantMe()
    {
        // Look up the type
        types.Type t = symboltable.SymbolTable.getInstance().find(typeName);
        if (t == null)
        {
            System.out.format(">> ERROR [%d:%d] non existing type %s\n",line,line,typeName);
            throw new Error("ERROR(" + line + ")");
        }

        // If there's an array size, this is an array allocation
        if (arraySize != null)
        {
            // if subscript is a plain value, ensure it's positive
            if (arraySize instanceof AstExpInt intExp && intExp.value <= 0) {
                System.out.format(">> ERROR [%d:%d] array size must be positive\n",line,line);
                throw new Error("ERROR(" + line + ")");
            }
            // Semant the array size expression
            types.Type sizeType = arraySize.semantMe();
            // Size should be int
            if (sizeType != types.TypeInt.getInstance())
            {
                System.out.format(">> ERROR [%d:%d] array size must be int\n",line,line);
                throw new Error("ERROR(" + line + ")");
            }
            // For array allocation like "new int[10]", we need to return a TypeArray
            // Create an anonymous array type with the element type
            // This is a simplified approach - in reality, we'd want to look up
            // a registered array type like IntArray
            return new types.TypeArray(typeName + "[]", t);
        }
        else
        {
            // This is a class allocation - return the class type
            return t;
        }
    }

    public Temp irMe()
    {
        if (arraySize != null)
        {
            /******************************************/
            /* Array allocation: sbrk((size+1)*4) bytes */
            /* Layout: [size, elem0, elem1, ...]       */
            /******************************************/
            Temp sizeTemp = arraySize.irMe();

            // Allocate (size+1) words: one extra for the size header
            Temp one = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IRcommandConstInt(one, 1));
            Temp sizePlusOne = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(
                    new IrCommandBinopAddIntegers(sizePlusOne, sizeTemp, one));

            Temp four = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IRcommandConstInt(four, 4));

            Temp byteSize = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(
                    new IrCommandPtrMul(byteSize, sizePlusOne, four));

            Temp result = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IrCommandAllocateHeap(byteSize, result));

            // Store array size in first word
            Ir.getInstance().AddIrCommand(new IrCommandStoreIndirect(result, sizeTemp));

            return result;
        }
        // Class allocation
        types.Type t = symboltable.SymbolTable.getInstance().find(typeName);
        if (t instanceof types.TypeClass tc) {
            int totalFields = ast.ClassContext.countNonMethodFields(tc);
            int byteSize2 = (totalFields > 0 ? totalFields : 1) * 4;
            Temp sizeTemp2 = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IRcommandConstInt(sizeTemp2, byteSize2));
            Temp result = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IrCommandAllocateHeap(sizeTemp2, result));
            // Initialize fields
            ast.ClassContext.emitFieldInit(tc, result);
            return result;
        }
        return null;
    }
}