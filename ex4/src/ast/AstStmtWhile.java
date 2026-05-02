package ast;

import ir.*;
import temp.Temp;
import types.*;
import symboltable.*;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;
	public int line;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, AstStmtList body)
	{
		this.cond = cond;
		this.body = body;
		this.line = 0;
	}
	
	public AstStmtWhile(AstExp cond, AstStmtList body, int line)
	{
		this.cond = cond;
		this.body = body;
		this.line = line;
	}

	public void printMe()
	{
		System.out.print("AST NODE STMT WHILE\n");
		if (cond != null) cond.printMe();
		if (body != null) body.printMe();
	}

	public Type semantMe()
	{
		/****************************/
		/* [0] Semant the Condition */
		/****************************/
		if (cond.semantMe() != TypeInt.getInstance())
		{
			System.out.format(">> ERROR [%d:%d] condition inside WHILE is not integral\n",line,line);
			throw new Error("ERROR(" + line + ")");
		}
		
		/*************************/
		/* [1] Begin While Scope */
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

		/*****************************************************/
		/* [4] Return value is irrelevant for while statement */
		/****************************************************/
		return null;
	}

	public boolean hasReturnStatement() {
		return body.hasReturnStatement();
	}

	public Temp irMe()
	{
		/*******************************/
		/* [1] Allocate 2 fresh labels */
		/*******************************/
		String labelEnd   = IrCommand.getFreshLabel("end");
		String labelStart = IrCommand.getFreshLabel("start");

		/*********************************/
		/* [2] entry label for the while */
		/*********************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandLabel(labelStart));

		/********************/
		/* [3] cond.IRme(); */
		/********************/
		Temp condTemp = cond.irMe();

		/******************************************/
		/* [4] Jump conditionally to the loop end */
		/******************************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandJumpIfEqToZero(condTemp,labelEnd));

		/*******************/
		/* [5] body.IRme() */
		/*******************/
		body.irMe();

		/******************************/
		/* [6] Jump to the loop entry */
		/******************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandJumpLabel(labelStart));

		/**********************/
		/* [7] Loop end label */
		/**********************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandLabel(labelEnd));

		/*******************/
		/* [8] return null */
		/*******************/
		return null;
	}
}