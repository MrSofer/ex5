package ast;

import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstExpCall extends AstExp
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String funcName;
	public AstExpVar object;
	public AstExpList params;
	public int line;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/

	public AstExpCall(String funcName, AstExpVar object, AstExpList params, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.funcName = funcName;
		this.object = object;
		this.params = params;
		this.line = line;
	}

	/************************************************/
	/* The printing message for a call exp AST node */
	/************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST CALL EXP */
		/********************************/

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		if (object != null) object.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("CALL(%s)\nWITH",funcName));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
		if (object != null) AstGraphviz.getInstance().logEdge(serialNumber,object.serialNumber);
	}

	public Type semantMe()
	{
		Type funcType = null;

		// Method call on object
		if (object != null) {
			Type objectType = object.semantMe();
			if (objectType == null) {
				return null;
			}

			// Look up method in the class
			if (objectType.isClass()) {
				TypeClass classType = (TypeClass) objectType;
				funcType = findMethodInClass(classType, funcName);
			}
		} else {
			// check current class and go up
			funcType = findMethodInClass(SymbolTable.getInstance().getCurrentClass(), funcName);

			// otherwise, global function call - look up in symbol table
			if (funcType == null) {
				funcType = symboltable.SymbolTable.getInstance().find(funcName);
			}
		}

		if (funcType == null)
		{
			throw new Error("ERROR(" + line + ")");
		}

		// If it's a TypeFunction, return its return type
		if (funcType instanceof TypeFunction ft)
		{
			/* check param compatability */
			// get func object
			TypeList currentType = ft.params;
			for (AstExpList param = params; param != null; param = param.tail) {
				if (currentType == null) {
					throw new Error("ERROR(" + line + ")");
				}

				Type currentParamType = param.head.semantMe();
				if (!canTypeSub(currentType.head, currentParamType)) {
					throw new Error("ERROR(" + line + ")");
				}

				currentType = currentType.tail;
			}
			if (currentType != null) {
				throw new Error("ERROR(" + line + ")");
			}


			return ((TypeFunction) funcType).returnType;
		}




		// Otherwise return the type itself (for built-in functions like PrintInt)
		return funcType;
	}

	/**
	 * Find a method by name in a class or its ancestors
	 */
	private Type findMethodInClass(TypeClass cls, String methodName) {
		if (cls == null) return null;

		// Search in current class
		for (TypeClassVarDecList it = cls.dataMembers; it != null; it = it.tail) {
			if (it.head.name.equals(methodName)) {
				return it.head.t;
			}
		}

		// Search in father class
		return findMethodInClass(cls.father, methodName);
	}

	private boolean canTypeSub(Type expectedParam, Type givenParam) {
		boolean typeNameMatch = givenParam.name.equals(expectedParam.name);
		boolean nilForClass = expectedParam.isClass() && givenParam.name.equals("nil");
		boolean nilForArray = expectedParam.isArray() && givenParam.name.equals("nil");
		boolean subtypeForClass = false;
		if (expectedParam.isClass() && givenParam instanceof TypeClass classType) {
			for (TypeClass father = classType.father; father != null; father = father.father) {
				if (father.name.equals(expectedParam.name)) {
					subtypeForClass = true;
					break;
				}
			}
		}
		return typeNameMatch || nilForClass || nilForArray || subtypeForClass;
	}

	public Temp irMe()
	{
		if ("PrintInt".equals(funcName))
		{
			Temp t = null;
			if (params != null) { t = params.head.irMe(); }
			Ir.getInstance().AddIrCommand(new IrCommandPrintInt(t));
			return TempFactory.getInstance().getFreshTemp();
		}

		/*******************************************************/
		/* General function call: store arguments into the     */
		/* function's parameter global variables, then jal.   */
		/*******************************************************/
		String mipsName = funcName;
		java.util.List<String> paramLabels =
				FuncParamTable.getInstance().getParams(mipsName);

		int i = 0;
		for (AstExpList argIt = params; argIt != null; argIt = argIt.tail)
		{
			Temp argTemp = argIt.head.irMe();
			if (i < paramLabels.size())
			{
				Ir.getInstance().AddIrCommand(
						new IrCommandStore(paramLabels.get(i), argTemp));
			}
			i++;
		}

		Ir.getInstance().AddIrCommand(new IrCommandCall(mipsName));

		/*******************************************************/
		/* Load return value from the function's retval global */
		/*******************************************************/
		Temp retval = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoad(retval, mipsName + "_retval"));

		return retval;
	}
}
