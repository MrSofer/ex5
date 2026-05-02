package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;

public class AstExpVarSubscript extends AstExpVar {
	public AstExpVar var;
	public AstExp subscript;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarSubscript(AstExpVar var, AstExp subscript, int line)
	{
		System.out.print("====================== var -> var [ exp ]\n");
		this.var = var;
		this.subscript = subscript;
		this.line = line;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSRIPT ... */
		/****************************************/
		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();
	}

	public types.Type semantMe()
	{
		// Get the type of the var being subscripted
		types.Type varType = null;
		if (var != null) {
			varType = var.semantMe();
		}
		
		// Check that subscript is an integer
		types.Type subscriptType = null;
		if (subscript != null) {
			subscriptType = subscript.semantMe();
		}

		if (subscriptType != types.TypeInt.getInstance()) {
			System.out.format(">> ERROR [%d:%d] array subscript must be integral\n",line,line);
			throw new Error("ERROR(" + line + ")");
		}

		// if subscript is a plain value, ensure it's positive
		if (subscript instanceof AstExpInt intExp && intExp.value < 0) {
			System.out.format(">> ERROR [%d:%d] array subscript must be non-negative\n",line,line);
			throw new Error("ERROR(" + line + ")");
		}
		
		// If the var is an array, return the element type
		if (varType instanceof types.TypeArray) {
			types.TypeArray arrayType = (types.TypeArray) varType;
			return arrayType.elementType;
		}
		System.out.format(">> ERROR [%d:%d] subscript must be on array type\n",line,line);
		throw new Error("ERROR(" + line + ")");
	}

	public Temp irMe()
	{
		/****************************/
		/* [1] Load base pointer    */
		/****************************/
		Temp baseTemp = var.irMe();

		/****************************/
		/* [2] Load subscript index */
		/****************************/
		Temp idxTemp = subscript.irMe();

		/*****************************/
		/* [3] Scale index by 4      */
		/*****************************/
		Temp four = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(four, 4));
		Temp offset = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandBinopMulIntegers(offset, idxTemp, four));

		/*****************************/
		/* [4] Compute element addr  */
		/*****************************/
		Temp addr = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandBinopAddIntegers(addr, baseTemp, offset));

		/*****************************/
		/* [5] Load value at addr    */
		/*****************************/
		Temp dst = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadIndirect(dst, addr));
		return dst;
	}
}
