import java.io.*;
import java.io.PrintWriter;
import java.util.*;
import java_cup.runtime.Symbol;
import ast.*;
import ir.*;
import temp.*;
import cfg.*;

public class Main
{
	/**************************************************************/
	/* Dataflow analysis to find used but uninitialized variables */
	/* Uses Control Flow Graph analysis for accurate results      */
	/**************************************************************/
	static public List<String> findUninitializedVariables()
	{
		// Get IR commands from the Ir singleton
		Ir ir = Ir.getInstance();
		
		// Build Control Flow Graph
		ControlFlowGraph cfg = new ControlFlowGraph();
		cfg.buildFromIR(ir);
		
		// Perform dataflow analysis on CFG
		DataflowAnalysis analysis = new DataflowAnalysis(cfg);
		Set<String> uninitializedVars = analysis.findUninitializedVariables();
		
		// Convert Set to List for return
		return new ArrayList<>(uninitializedVars);
	}

	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstDecList ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/********************************/
			/* [2] Initialize a file writer */
			/********************************/
			fileWriter = new PrintWriter(outputFileName);

			/******************************/
			/* [3] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [4] Initialize a new parser */
			/*******************************/
			p = new Parser(l);

			/***********************************/
			/* [5] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstDecList) p.parse().value;

			/*************************/
			/* [6] Print the AST ... */
			/*************************/
//			ast.printMe();

			/**************************/
			/* [7] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [8] IR the AST ... */
			/**********************/
			ast.irMe();

			/*************************************************/
			/* [9] Dataflow analysis for uninitialized vars */
			/*************************************************/
			List<String> uninitializedVars = findUninitializedVariables();
			if (uninitializedVars.isEmpty()) {
				fileWriter.print("!OK");
			}
			else {
				fileWriter.print(String.join("\n", uninitializedVars.stream().sorted().toList()));
			}

			/**************************/
			/* [10] Close output file */
			/**************************/
			fileWriter.close();

			/*************************************/
			/* [11] Finalize AST GRAPHIZ DOT file */
			/*************************************/
//			AstGraphviz.getInstance().finalizeFile();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		catch (Error e)
		{
			String emsg = e.getMessage();
			System.out.println(emsg);
			String finalError;
			if (!emsg.startsWith("ERROR("))
				finalError = "ERROR";
			else
				finalError = emsg;

			try {
				fileWriter = new PrintWriter(outputFileName);
				fileWriter.print(finalError);
				fileWriter.close();
			} catch (IOException fileError) {

			}
		}
	}
}


