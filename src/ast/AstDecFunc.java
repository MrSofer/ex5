package ast;

import ir.*;
import temp.Temp;
import types.*;
import symboltable.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AstDecFunc extends AstDec
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String returnTypeName;
	public String name;
	public AstTypeNameList params;
	public AstStmtList body;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecFunc(
		String returnTypeName,
		String name,
		AstTypeNameList params,
		AstStmtList body)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.returnTypeName = returnTypeName;
		this.name = name;
		this.params = params;
		this.body = body;
		this.line = 0;
	}

	public AstDecFunc(
		String returnTypeName,
		String name,
		AstTypeNameList params,
		AstStmtList body,
		int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.returnTypeName = returnTypeName;
		this.name = name;
		this.params = params;
		this.body = body;
		this.line = line;
	}

	/************************************************************/
	/* The printing message for a function declaration AST node */
	/************************************************************/
	public void printMe()
	{
		/*************************************************/
		/* AST NODE TYPE = AST NODE FUNCTION DECLARATION */
		/*************************************************/
		System.out.format("FUNC(%s):%s\n",name,returnTypeName);

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		if (body   != null) body.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("FUNC(%s)\n:%s\n",name,returnTypeName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
		if (body   != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}

	public Type semantMe()
	{
		Type t;
		Type returnType = null;
		TypeList type_list = null;

		if (overridesGlobalFunc(name)) {
			System.out.format(">> ERROR [%d:%d] function definition overrides library func %s\n",line,line,name);
			throw new Error("ERROR(" + line + ")");
		}

		/*******************/
		/* [0] return type */
		/*******************/
		returnType = SymbolTable.getInstance().find(returnTypeName);
		if (returnType == null)
		{
			System.out.format(">> ERROR [%d:%d] non existing return type %s\n",line,line,returnTypeName);
			throw new Error("ERROR(" + line + ")");
		}

		/***************************/
		/* [1] Process Input Params types first */
		// they dont enter scope here! we need to enter after starting the scope
		/***************************/
		if (params != null) {
			type_list = params.semantMe();
		}

		if (duplicateParamName(params)) {
			System.out.format(">> ERROR [%d:%d] duplicate param names in func %s\n",line,line,name);
			throw new Error("ERROR(" + line + ")");
		}

		/***************************************************/
		/* [2] Enter the Function Type to the Symbol Table EARLY */
		/*     so recursive calls can find it */
		/***************************************************/
		SymbolTable.getInstance().enter(name,new TypeFunction(returnType,name,type_list));

		/****************************/
		/* [3] Begin Function Scope */
		/****************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [4] Enter params into scope */
		/***************************/
		for (AstTypeNameList it = params; it  != null; it = it.tail)
		{
			t = SymbolTable.getInstance().find(it.head.type);
			if (t != null)
			{
				SymbolTable.getInstance().enter(it.head.name,t);
			}
		}

		/*************************************************/
		/* [5] Set current function return type context */
		/*************************************************/
		SymbolTable.getInstance().setCurrentFunctionReturnType(returnType);

		/*******************/
		/* [6] Semant Body */
		/*******************/
		body.semantMe();

		/************************************************/
		/* [7] Clear current function return type context */
		/************************************************/
		SymbolTable.getInstance().setCurrentFunctionReturnType(null);

		/*****************/
		/* [8] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

		/************************************************************/
		/* [9] Return value is irrelevant for function declarations */
		/************************************************************/
		return null;		
	}

	private boolean duplicateParamName(AstTypeNameList params) {
		Set<String> set = new HashSet<>();
		for (AstTypeNameList param = params; param != null; param = param.tail) {
			if (set.contains(param.head.name)) {
				return true;
			}
			else
			{
				set.add(param.head.name);
			}
		}
		return false;
	}

	private boolean overridesGlobalFunc(String funcName) {
		return "PrintInt".equals(funcName) || "PrintString".equals(funcName);
	}

	private static String currentMipsFuncName = null;
	public static String getCurrentMipsFuncName() { return currentMipsFuncName; }

	private static String currentClassContext = null;
	public static String getCurrentClassContext() { return currentClassContext; }
	public static void setCurrentClassContext(String ctx) { currentClassContext = ctx; }

	private static String overrideParamPrefix = null;
	public static void setOverrideParamPrefix(String prefix) { overrideParamPrefix = prefix; }

	private static List<String> currentFuncAllGlobals = null;
	public static void addCurrentFuncGlobal(String label) {
		if (currentFuncAllGlobals != null) currentFuncAllGlobals.add(label);
	}
	public static List<String> getCurrentFuncAllGlobals() {
		return currentFuncAllGlobals != null ? new ArrayList<>(currentFuncAllGlobals) : new ArrayList<>();
	}

	public void preRegisterParams()
	{
		String prefix = currentClassContext;
		String mipsName;
		if (name.equals("main"))      mipsName = "user_main";
		else if (prefix != null)      mipsName = prefix + "_" + name;
		else                          mipsName = "func_" + name;

		String paramPrefix = (overrideParamPrefix != null) ? overrideParamPrefix : mipsName;

		List<String> paramLabels = new ArrayList<>();
		if (prefix != null) paramLabels.add(paramPrefix + "_param_this");
		for (AstTypeNameList it = params; it != null; it = it.tail)
			paramLabels.add(paramPrefix + "_param_" + it.head.name);
		FuncParamTable.getInstance().register(mipsName, paramLabels);
		// Also register under paramPrefix key so callers using rootOwner key can find params
		if (!paramPrefix.equals(mipsName)) {
			FuncParamTable.getInstance().register(paramPrefix, paramLabels);
		}
	}

	public Temp irMe()
	{
		String prefix = currentClassContext;
		String mipsName;
		if (name.equals("main"))      mipsName = "user_main";
		else if (prefix != null)      mipsName = prefix + "_" + name;
		else                          mipsName = "func_" + name;

		String paramPrefix = (overrideParamPrefix != null) ? overrideParamPrefix : mipsName;

		currentMipsFuncName = mipsName;
		currentFuncAllGlobals = new ArrayList<>();

		Ir.getInstance().AddIrCommand(new IrCommandLabel(mipsName));
		IrVarTable.getInstance().beginScope();

		Ir.getInstance().AddIrCommand(new IrCommandAllocate(mipsName + "_retval"));

		if (prefix != null) {
			String thisLabel = paramPrefix + "_param_this";
			Ir.getInstance().AddIrCommand(new IrCommandAllocate(thisLabel));
			ClassContext.getInstance().enterClass(
				(types.TypeClass) symboltable.SymbolTable.getInstance().find(prefix),
				thisLabel);
			addCurrentFuncGlobal(thisLabel);
		}

		for (AstTypeNameList it = params; it != null; it = it.tail)
		{
			String uniqueLabel = paramPrefix + "_param_" + it.head.name;
			IrVarTable.getInstance().allocateWithLabel(it.head.name, uniqueLabel);
			Ir.getInstance().AddIrCommand(new IrCommandAllocate(uniqueLabel));
			addCurrentFuncGlobal(uniqueLabel);
		}

		if (body != null) body.irMe();
		IrVarTable.getInstance().endScope();

		if (prefix != null) ClassContext.getInstance().leaveClass();

		currentFuncAllGlobals = null;
		currentMipsFuncName = null;
		Ir.getInstance().AddIrCommand(new IrCommandReturn());
		return null;
	}
}
