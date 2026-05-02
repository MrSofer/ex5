/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

public class IrVarTable
{
	/*********************************************/
	/* Stack of scopes: each scope maps          */
	/* variable name -> unique MIPS label        */
	/*********************************************/
	private final Deque<HashMap<String, String>> scopes = new ArrayDeque<>();

	/************************************/
	/* Counter for generating unique IDs */
	/************************************/
	private static int counter = 0;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected IrVarTable()
	{
		scopes.push(new HashMap<>());
	}

	/*************************************/
	/* Push a new scope on the stack ... */
	/*************************************/
	public void beginScope()
	{
		scopes.push(new HashMap<>());
	}

	/************************************/
	/* Pop the top scope off the stack */
	/************************************/
	public void endScope()
	{
		if (scopes.size() > 1)
		{
			scopes.pop();
		}
	}

	/**************************************************************/
	/* Allocate a unique MIPS label for a variable declaration.  */
	/* Registers the mapping in the current (innermost) scope.   */
	/* Returns the unique label.                                  */
	/**************************************************************/
	public String allocate(String name)
	{
		String uniqueLabel = name + "_" + counter++;
		scopes.peek().put(name, uniqueLabel);
		return uniqueLabel;
	}

	/**************************************************************/
	/* Register a pre-determined label for a variable name in    */
	/* the current (innermost) scope.  Used for function params  */
	/* whose labels are fixed before IR generation begins.       */
	/**************************************************************/
	public void allocateWithLabel(String name, String uniqueLabel)
	{
		scopes.peek().put(name, uniqueLabel);
	}

	/**************************************************************/
	/* Find the unique label for a variable, searching from the  */
	/* innermost scope outward.  Returns null if not found.      */
	/**************************************************************/
	public String find(String name)
	{
		for (HashMap<String, String> scope : scopes)
		{
			if (scope.containsKey(name))
			{
				return scope.get(name);
			}
		}
		return null;
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static IrVarTable instance = null;

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static IrVarTable getInstance()
	{
		if (instance == null)
		{
			instance = new IrVarTable();
		}
		return instance;
	}
}
