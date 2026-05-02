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
	private Type cachedObjectType = null;

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
			cachedObjectType = objectType;
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

		if ("PrintString".equals(funcName))
		{
			if (params != null && params.head instanceof AstExpString strExp) {
				Ir.getInstance().AddIrCommand(new IrCommandPrintString(strExp.value));
			} else if (params != null) {
				Temp t = params.head.irMe();
				Ir.getInstance().AddIrCommand(new IrCommandPrintStringVar(t));
			}
			return TempFactory.getInstance().getFreshTemp();
		}

		// Method call on object
		if (object != null && cachedObjectType instanceof TypeClass tc) {
			// Determine param prefix using root ancestor's label
			String rootOwner = ast.VtableRegistry.getInstance().getRootMethodOwner(tc.name, funcName);
			String mipsMethod = (rootOwner != null ? rootOwner : tc.name) + "_" + funcName;

			// Get ALL caller's globals (params + locals) for save/restore
			java.util.List<String> callerGlobals = AstDecFunc.getCurrentFuncAllGlobals();
			if (!callerGlobals.isEmpty())
				Ir.getInstance().AddIrCommand(new IrCommandSaveGlobals(new java.util.ArrayList<>(callerGlobals)));

			Temp objPtr = object.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandNullCheck(objPtr));

			// Store this pointer using root param label
			String thisLabel = mipsMethod + "_param_this";
			Ir.getInstance().AddIrCommand(new IrCommandStore(thisLabel, objPtr));

			// Evaluate ALL args first, then store (Fix 1)
			// Only needed when some arg contains a call (to avoid clobbering param globals)
			java.util.List<String> paramLabels = FuncParamTable.getInstance().getParams(mipsMethod);
			if (argListContainsCall(params)) {
				java.util.List<Temp> argTemps = new java.util.ArrayList<>();
				for (AstExpList argIt = params; argIt != null; argIt = argIt.tail)
					argTemps.add(argIt.head.irMe());
				for (int i = 0; i < argTemps.size() && i + 1 < paramLabels.size(); i++)
					Ir.getInstance().AddIrCommand(new IrCommandStore(paramLabels.get(i + 1), argTemps.get(i)));
			} else {
				int i = 1; // 0 is 'this'
				for (AstExpList argIt = params; argIt != null; argIt = argIt.tail) {
					Temp argTemp = argIt.head.irMe();
					if (i < paramLabels.size())
						Ir.getInstance().AddIrCommand(new IrCommandStore(paramLabels.get(i), argTemp));
					i++;
				}
			}

			// Dispatch: virtual if class has a vtable slot, static otherwise
			int vtableIdx = ast.VtableRegistry.getInstance().getMethodIndex(tc.name, funcName);
			boolean useVirtual = vtableIdx >= 0;
			if (useVirtual) {
				Ir.getInstance().AddIrCommand(new ir.IrCommandCallVirtual(objPtr, vtableIdx));
			} else {
				Ir.getInstance().AddIrCommand(new IrCommandCall(mipsMethod, callerGlobals));
			}

			if (!callerGlobals.isEmpty())
				Ir.getInstance().AddIrCommand(new IrCommandRestoreGlobals(new java.util.ArrayList<>(callerGlobals)));

			Temp retval = TempFactory.getInstance().getFreshTemp();
			if (useVirtual) {
				Ir.getInstance().AddIrCommand(new IrCommandLoad(retval, "_virtual_retval"));
			} else {
				Ir.getInstance().AddIrCommand(new IrCommandLoad(retval, mipsMethod + "_retval"));
			}
			return retval;
		}

		/*******************************************************/
		/* General function call: store arguments into the     */
		/* function's parameter global variables, then jal.   */
		/*******************************************************/
		String mipsName = "func_" + funcName;
		java.util.List<String> paramLabels =
				FuncParamTable.getInstance().getParams(mipsName);

		// Get ALL caller's globals (params + locals) for save/restore (Fix 2)
		java.util.List<String> allCallerGlobals = AstDecFunc.getCurrentFuncAllGlobals();
		if (!allCallerGlobals.isEmpty())
			Ir.getInstance().AddIrCommand(new IrCommandSaveGlobals(new java.util.ArrayList<>(allCallerGlobals)));

		// Evaluate all args FIRST, then store (Fix 1) - only when args contain calls
		if (argListContainsCall(params)) {
			java.util.List<Temp> argTemps = new java.util.ArrayList<>();
			for (AstExpList argIt = params; argIt != null; argIt = argIt.tail)
				argTemps.add(argIt.head.irMe());
			for (int i = 0; i < argTemps.size() && i < paramLabels.size(); i++)
				Ir.getInstance().AddIrCommand(new IrCommandStore(paramLabels.get(i), argTemps.get(i)));
		} else {
			int i = 0;
			for (AstExpList argIt = params; argIt != null; argIt = argIt.tail) {
				Temp argTemp = argIt.head.irMe();
				if (i < paramLabels.size())
					Ir.getInstance().AddIrCommand(new IrCommandStore(paramLabels.get(i), argTemp));
				i++;
			}
		}

		Ir.getInstance().AddIrCommand(new IrCommandCall(mipsName, allCallerGlobals));

		if (!allCallerGlobals.isEmpty())
			Ir.getInstance().AddIrCommand(new IrCommandRestoreGlobals(new java.util.ArrayList<>(allCallerGlobals)));

		/*******************************************************/
		/* Load return value from the function's retval global */
		/*******************************************************/
		Temp retval = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoad(retval, mipsName + "_retval"));

		return retval;
	}

	private static boolean containsCall(AstExp exp) {
		if (exp == null) return false;
		if (exp instanceof AstExpCall) return true;
		if (exp instanceof AstExpBinop b) return containsCall(b.left) || containsCall(b.right);
		if (exp instanceof AstExpVarSubscript s) return containsCall(s.subscript);
		return false;
	}

	private static boolean argListContainsCall(AstExpList params) {
		for (AstExpList it = params; it != null; it = it.tail) {
			if (containsCall(it.head)) return true;
		}
		return false;
	}
}
