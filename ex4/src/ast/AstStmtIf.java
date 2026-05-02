package ast;

import ir.*;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstStmtIf extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;
	public AstStmtList elseBody;
	public int line;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtIf(AstExp cond, AstStmtList body)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.cond = cond;
		this.body = body;
		this.elseBody = null;
		this.line = 0;
	}
	
	public AstStmtIf(AstExp cond, AstStmtList body, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.cond = cond;
		this.body = body;
		this.elseBody = null;
		this.line = line;
	}

	public AstStmtIf(AstExp cond, AstStmtList body, AstStmtList elseBody)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.cond = cond;
		this.body = body;
		this.elseBody = elseBody;
		this.line = 0;
	}
	
	public AstStmtIf(AstExp cond, AstStmtList body, AstStmtList elseBody, int line)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.cond = cond;
		this.body = body;
		this.elseBody = elseBody;
		this.line = line;
	}

	/****************************************************/
	/* The printing message for an if statment AST node */
	/****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE STMT IF\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (cond != null) cond.printMe();
		if (body != null) body.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"IF (left)\nTHEN right");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (cond != null) AstGraphviz.getInstance().logEdge(serialNumber,cond.serialNumber);
		if (body != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}

	public Type semantMe()
	{
		/****************************/
		/* [0] Semant the Condition */
		/****************************/
		if (cond.semantMe() != TypeInt.getInstance())
		{
			System.out.format(">> ERROR [%d:%d] condition inside IF is not integral\n",line,line);
			throw new Error("ERROR(" + line + ")");
		}
		
		/*************************/
		/* [1] Begin If Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant Data Members */
		/***************************/
		body.semantMe();

		/*****************/
		/* [3] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

		/*************************/
		/* [4] Handle Else block */
		/*************************/
		if (elseBody != null) {
			SymbolTable.getInstance().beginScope();
			elseBody.semantMe();
			SymbolTable.getInstance().endScope();
		}

		/***************************************************/
		/* [5] Return value is irrelevant for if statement */
		/**************************************************/
		return null;		
	}
	
	@Override
	public boolean hasReturnStatement()
	{
		if (body != null && body.hasReturnStatement()) {
			return true;
		}

		// An if statement has a return only if both branches have returns
		if (elseBody != null && elseBody.hasReturnStatement()) {
			return true;
		}

		// If no else, it doesn't guarantee a return
		return false;
	}
	
	public Temp irMe()
	{
		/*******************************/
		/* [1] Allocate fresh labels */
		/*******************************/
		String labelElse = IrCommand.getFreshLabel("else");
		String labelEnd  = IrCommand.getFreshLabel("end");
		
		/********************/
		/* [2] cond.IRme(); */
		/********************/
		Temp condTemp = cond.irMe();
		
		/****************************************************/
		/* [3] Jump conditionally to else (or end if no else) */
		/****************************************************/
		if (elseBody != null) {
			Ir.
					getInstance().
					AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelElse));
		} else {
			Ir.
					getInstance().
					AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelEnd));
		}
		
		/*******************/
		/* [4] body.IRme() */
		/*******************/
		if (body != null) {
			body.irMe();
		}
		
		/****************************************/
		/* [5] Jump to end (skip else if present) */
		/****************************************/
		if (elseBody != null) {
			Ir.
					getInstance().
					AddIrCommand(new IrCommandJumpLabel(labelEnd));
			
			/**********************/
			/* [6] Else label */
			/**********************/
			Ir.
					getInstance().
					AddIrCommand(new IrCommandLabel(labelElse));
			
			/**********************/
			/* [7] elseBody.IRme() */
			/**********************/
			elseBody.irMe();
		}
		
		/**********************/
		/* [8] End label */
		/**********************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandLabel(labelEnd));
		
		/*******************/
		/* [9] return null */
		/*******************/
		return null;
	}
}
