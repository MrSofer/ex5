import java.io.*;
import java.io.PrintWriter;

//import java_cup.Lexer;
import java_cup.runtime.Symbol;
import ast.*;
import ir.*;
import mips.*;
import regalloc.InterferenceGraph;
import regalloc.RegisterAllocator;
import temp.Temp;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstDecList ast;
		FileReader fileReader;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/******************************/
			/* [2] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [3] Initialize a new parser */
			/*******************************/
			p = new Parser(l);

			/***********************************/
			/* [4] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstDecList) p.parse().value;

			/**************************/
			/* [5] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [6] Ir the AST ... */
			/**********************/
			// Build vtables before preRegisterParams so override bases can be determined
			VtableRegistry.getInstance().buildAll(ast);

			for (ast.AstDecList it = ast; it != null; it = it.tail)
			{
				if (it.head instanceof AstDecFunc f)
					f.preRegisterParams();
				else if (it.head instanceof AstDecClass c)
					c.preRegisterParams();
			}

			// Initialize MipsGenerator before irMeTopLevel so vtable data can be emitted
			MipsGenerator.init(outputFileName);
			ast.irMeTopLevel();

			/*Register Allocation*/
			RegisterAllocator allocator = new RegisterAllocator(Ir.getInstance().getCommands());
			allocator.allocate();

			/***********************/
			/* [7] MIPS the Ir ... */
			/***********************/
			Ir.getInstance().mipsMe();


			/***************************/
			/* [9] Finalize MIPS file */
			/***************************/
			MipsGenerator.getInstance().finalizeFile();
		}

		catch (Exception e)
		{
			e.printStackTrace();
			try {
				PrintWriter errWriter = new PrintWriter(outputFileName);
				errWriter.print("ERROR");
				errWriter.close();
			} catch (Exception ex) {}
		}
		catch (Error e)
		{
			String emsg = e.getMessage();
			String finalError;
			if (emsg != null && emsg.startsWith("ERROR(")) {
				finalError = emsg;
			} else if (emsg != null && emsg.equals("Register Allocation Failed")) {
				finalError = "Register Allocation Failed";
			} else {
				finalError = "ERROR";
			}
			try {
				PrintWriter errWriter = new PrintWriter(outputFileName);
				errWriter.print(finalError);
				errWriter.close();
			} catch (Exception ex) {}
		}
	}
}