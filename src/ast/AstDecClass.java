package ast;

import types.*;
import symboltable.*;

public class AstDecClass extends AstDec
{
	/********/
	/* NAME */
	/********/
	public String name;
	public String father;

	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDecList dataMembers;
	public int line;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecClass(String name, String father, AstDecList dataMembers)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
		this.line = 0;
	}

	public AstDecClass(String name, String father, AstDecList dataMembers, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
		this.line = line;
	}

	/*********************************************************/
	/* The printing message for a class declaration AST node */
	/*********************************************************/
	public void printMe()
	{
		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (dataMembers != null) dataMembers.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(serialNumber,
			String.format("CLASS\n%s",name));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (dataMembers != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, dataMembers.serialNumber);
        }
	}
	
	public Type semantMe()
	{	
		/************************************************/
		/* [1] Look up father class if it exists */
		/************************************************/
		TypeClass fatherClass = null;
		if (father != null)
		{
			Type fatherType = SymbolTable.getInstance().find(father);
			if (fatherType == null)
			{
				throw new Error("ERROR(" + line + ")");
			}
			if (!fatherType.isClass())
			{
				throw new Error("ERROR(" + line + ")");
			}
			fatherClass = (TypeClass) fatherType;
		}


		/************************************************/
		/* [2] Create and enter class type early so it can self-reference */
		/************************************************/
		TypeClass t = new TypeClass(fatherClass,name, null);
		SymbolTable.getInstance().enter(name,t);
		TypeClass previousClass = SymbolTable.getInstance().getCurrentClass();
		SymbolTable.getInstance().setCurrentClass(t);

		/*************************/
		/* [2] Begin Class Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [3] Semant Data Members, collect them, and check shadowing */
		/***************************/
		TypeClassVarDecList memberList = null; // This will hold *only* the child's new/overriding members
		if (dataMembers != null) {
			// Process members and enter them into the current scope
			dataMembers.semantMe();
			
			// Now collect the members into TypeClassVarDecList
			for (AstDecList it = dataMembers; it != null; it = it.tail)
			{
				String memberName = null;
                int memberLine = 0;
                Type memberType = null;

				if (it.head instanceof AstDecVar)
				{
					AstDecVar varDec = (AstDecVar) it.head;
					memberName = varDec.name;
                    memberLine = varDec.line;
					memberType = SymbolTable.getInstance().find(varDec.name);

                    // *** ILLEGAL SHADOWING CHECK: A variable cannot shadow a member in parent class ***
                    if (fatherClass != null) {
                        Type fatherMemberType = findMemberInClass(fatherClass, memberName);
                        if (fatherMemberType != null) {
                            throw new Error("ERROR(" + memberLine + ")");
                        }
                    }
				}
				else if (it.head instanceof AstDecFunc funcDec)
				{
					memberName = funcDec.name;
                    memberLine = funcDec.line;
					memberType = SymbolTable.getInstance().find(funcDec.name);

                    // *** ILLEGAL SHADOWING CHECK: A method cannot shadow a variable in parent class ***
                    if (fatherClass != null) {
                        Type fatherMemberType = findMemberInClass(fatherClass, memberName);
                        if (fatherMemberType != null && !(fatherMemberType instanceof TypeFunction)) {
                            // The member is found in the father class AND it's not a function (i.e., it's a variable)
                            throw new Error("ERROR(" + memberLine + ")");
                        }
                    }

					// illegal overloading check
					if (memberList != null && memberList.containsFunction(memberName)) {
						throw new Error("ERROR(" + memberLine + ")");
					}
				}

                // Add to the member list with name and type (Child's *own* members)
                if (memberType != null && memberName != null) {
                    memberList = new TypeClassVarDecList(
                        new TypeClassVarDec(memberType, memberName),
                        memberList);
                }
			}
		}

		t.dataMembers = memberList;

		/*****************************************************/
		/* [4] Validate method overriding (not overloading) */
		/*****************************************************/
		// This step remains the same, checking child methods against father methods
		if (fatherClass != null && dataMembers != null) {
			// Walk through the AST member list to check each method with its line number
			for (AstDecList it = dataMembers; it != null; it = it.tail) {
				if (it.head instanceof AstDecFunc) {
					AstDecFunc funcDec = (AstDecFunc) it.head;
					Type funcType = SymbolTable.getInstance().find(funcDec.name);
					
					if (funcType instanceof TypeFunction) {
						TypeFunction childMethod = (TypeFunction) funcType;
						
						// Look for same method name in father class
						TypeFunction fatherMethod = findMethodInClass(fatherClass, funcDec.name);
						if (fatherMethod != null) {
							// Check if signatures match (return type and parameters)
							if (!methodSignaturesMatch(childMethod, fatherMethod)) {
								throw new Error("ERROR(" + funcDec.line + ")");
							}
						}
					}
				}
			}
		}

		// check method overloading (not allowed)

		/*****************/
		/* [5] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();
		SymbolTable.getInstance().setCurrentClass(previousClass);

		/*********************************************************/
		/* [6] Return value is irrelevant for class declarations */
		/*********************************************************/
		return null;		
	}

    // *** Helper function to find any member (variable or method) in the inheritance chain ***
    /**
	 * Find a member (variable or method) by name in a class or its ancestors
	 * Returns the member's Type (TypeInt, TypeFunction, etc.) if found, otherwise null.
	 */
	private Type findMemberInClass(TypeClass cls, String memberName) {
		if (cls == null) return null;

		// Search in current class member list
		for (TypeClassVarDecList it = cls.dataMembers; it != null; it = it.tail) {
			if (it.head.name.equals(memberName)) {
				return it.head.t; // Found the member's type
			}
		}

		// Search in father class recursively
		return findMemberInClass(cls.father, memberName);
	}

	/**
	 * Find a method by name in a class or its ancestors
	 */
	private TypeFunction findMethodInClass(TypeClass cls, String methodName) {
		if (cls == null) return null;
		
		// Search in current class
		for (TypeClassVarDecList it = cls.dataMembers; it != null; it = it.tail) {
			if (it.head.name.equals(methodName) && it.head.t instanceof TypeFunction) {
				return (TypeFunction) it.head.t;
			}
		}
		
		// Search in father class
		return findMethodInClass(cls.father, methodName);
	}
	
	/**
	 * Check if two method signatures match (return type and parameters)
	 */
	private boolean methodSignaturesMatch(TypeFunction m1, TypeFunction m2) {
		// Check return type
		if (!typesEqual(m1.returnType, m2.returnType)) {
			return false;
		}
		
		// Check parameter list
		return parameterListsMatch(m1.params, m2.params);
	}
	
	private boolean typesEqual(Type t1, Type t2) {
		if (t1 == t2) return true;
		if (t1 == null || t2 == null) return false;
		return t1.name.equals(t2.name);
	}
	
	private boolean parameterListsMatch(TypeList p1, TypeList p2) {
		// Both null - match
		if (p1 == null && p2 == null) return true;
		
		// One null, one not - no match
		if (p1 == null || p2 == null) return false;
		
		// Check heads match
		if (!typesEqual(p1.head, p2.head)) return false;
		
		// Recursively check tails
		return parameterListsMatch(p1.tail, p2.tail);
	}

	private void printMembers(TypeClassVarDecList memberList) {
		if (memberList.tail != null)
			printMembers(memberList.tail);
	}

	public void preRegisterParams() {
		String prev = AstDecFunc.getCurrentClassContext();
		AstDecFunc.setCurrentClassContext(name);
		if (dataMembers != null) {
			for (AstDecList it = dataMembers; it != null; it = it.tail) {
				if (it.head instanceof AstDecFunc f) {
					String rootOwner = VtableRegistry.getInstance().getRootMethodOwner(name, f.name);
					String rootPrefix = (rootOwner != null ? rootOwner : name) + "_" + f.name;
					AstDecFunc.setOverrideParamPrefix(rootPrefix);
					f.preRegisterParams();
					AstDecFunc.setOverrideParamPrefix(null);
				}
			}
		}
		AstDecFunc.setCurrentClassContext(prev);
	}

	public temp.Temp irMe() {
		String prev = AstDecFunc.getCurrentClassContext();
		AstDecFunc.setCurrentClassContext(name);
		if (dataMembers != null) {
			for (AstDecList it = dataMembers; it != null; it = it.tail) {
				if (it.head instanceof AstDecFunc f) {
					String rootOwner = VtableRegistry.getInstance().getRootMethodOwner(name, f.name);
					String rootPrefix = (rootOwner != null ? rootOwner : name) + "_" + f.name;
					AstDecFunc.setOverrideParamPrefix(rootPrefix);
					f.irMe();
					AstDecFunc.setOverrideParamPrefix(null);
				}
			}
		}
		AstDecFunc.setCurrentClassContext(prev);
		return null;
	}
}