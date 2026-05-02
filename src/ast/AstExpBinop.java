package ast;

import types.*;
import ir.*;
import temp.*;

public class AstExpBinop extends AstExp
{
	int op;
	public AstExp left;
	public AstExp right;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.line = 0;
	}

	public AstExpBinop(AstExp left, AstExp right, int op, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.line = line;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop="";
		
		/*********************************/
		/* CONVERT OP to a printable sop */
		/*********************************/
		if (op == 0) {sop = "+";}
		if (op == 1) {sop = "-";}
		if (op == 3) {sop = "/";}
		if (op == 4) {sop = "<";}
		if (op == 6) {sop = "=";}

		/**********************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*********************************/

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.printMe();
		if (right != null) right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("BINOP(%s)",sop));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	public Type semantMe()
	{
		Type t1 = null;
		Type t2 = null;
		
		if (left  != null) t1 = left.semantMe();
		if (right != null) t2 = right.semantMe();
		
		if ((t1 == TypeInt.getInstance()) && (t2 == TypeInt.getInstance()))
		{
			if (op == 3 && right instanceof AstExpInt astExpInt && astExpInt.value == 0) {
				throw new Error("ERROR(" + line + ")");
			}
			return TypeInt.getInstance();
		}
		// String operations: + for concatenation, = for comparison
		if ((t1 == TypeString.getInstance()) && (t2 == TypeString.getInstance()))
		{
			// op == 0 is PLUS, op == 6 is EQ
			if (op == 0) {
				return TypeString.getInstance(); // concatenation returns string
			} else if (op == 6) {
				return TypeInt.getInstance(); // comparison returns int
			} else {
				// Other string operations not supported
				throw new Error("ERROR(" + line + ")");
			}
		}

		if (op != 6) {
			throw new Error("ERROR(" + line + ")");
		}

		// nil can be compared with classes or arrays
		if (t1 == TypeNil.getInstance() || t2 == TypeNil.getInstance())
		{
			Type otherType = (t1 == TypeNil.getInstance()) ? t2 : t1;
			// nil can be compared with classes, arrays, or another nil
			if (otherType != null && (otherType == TypeNil.getInstance() || otherType.isClass() || otherType.isArray()))
			{
				return TypeInt.getInstance(); // comparisons return int
			}
		}

		// arrays can be compared if they're the same type
		if (t1 != null && t2 != null && t1.isArray() && t2.isArray())
		{
			if (t1.name.equals(t2.name))
			{
				return TypeInt.getInstance();
			}
		}

		// classes can be compared if they're compatible
		if (t1 != null && t2 != null && t1.isClass() && t2.isClass())
		{
			TypeClass c1 = (TypeClass) t1;
			TypeClass c2 = (TypeClass) t2;

			if (c1.isAncestor(c2) || c2.isAncestor(c1)) {
				return TypeInt.getInstance();
			}
		}

		throw new Error("ERROR(" + line + ")");
	}

	public Temp irMe() {
		Temp resultName = TempFactory.getInstance().getFreshTemp();
		Temp leftName = left.irMe();
		Temp rightName = right.irMe();

		IrCommand cmd = switch (op) {
			case 0 -> new IrCommandBinopAddIntegers(resultName, leftName, rightName);
			case 1 -> new IrCommandBinopMinIntegers(resultName, leftName, rightName);
			case 2 -> new IrCommandBinopMulIntegers(resultName, leftName, rightName);
			case 3 -> new IrCommandBinopDivIntegers(resultName, leftName, rightName);
			case 4 -> new IrCommandBinopLtIntegers(resultName, leftName, rightName);
			case 5 -> new IrCommandBinopGtIntegers(resultName, leftName, rightName);
			case 6 -> new IrCommandBinopEqIntegers(resultName, leftName, rightName);
			default -> null;
		};

		Ir.getInstance().AddIrCommand(cmd);
		return resultName;
	}
}
