/***********/
/* PACKAGE */
/***********/
package mips;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import regalloc.InterferenceGraph;
import temp.*;

public class MipsGenerator
{
	private static final int WORD_SIZE=4;
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;

	/***********************/
	/* The file writer ... */
	/***********************/
	public void finalizeFile()
	{
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.close();
	}
	public void printInt(Temp t)
	{
		int idx = InterferenceGraph.getInstance().allNodes.get(t).assignedColor;
		// fileWriter.format("\taddi $a0,Temp_%d,0\n",idx);
		fileWriter.format("\tmove $a0,$t%d\n",idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}
//	public Temp addressLocalVar(int serialLocalVarNum)
//	{
//		Temp t  = TempFactory.getInstance().getFreshTemp();
//		int idx = t.getSerialNumber();
//
//		fileWriter.format("\taddi Temp_%d,$fp,%d\n",idx,-serialLocalVarNum*WORD_SIZE);
//
//		return t;
//	}
	public void allocate(String varName)
	{
		fileWriter.format(".data\n");
		fileWriter.format("\tglobal_%s: .word 721\n",varName);
	}
	public void load(Temp dst, String varName)
	{
		int idxdst=InterferenceGraph.getInstance().allNodes.get(dst).assignedColor;
		fileWriter.format("\tlw $t%d,global_%s\n",idxdst,varName);
	}
	public void store(String varName, Temp src)
	{
		int idxsrc=InterferenceGraph.getInstance().allNodes.get(src).assignedColor;
		fileWriter.format("\tsw $t%d,global_%s\n",idxsrc,varName);
	}
	public void li(Temp t, int value)
	{
		int idx=InterferenceGraph.getInstance().allNodes.get(t).assignedColor;
		fileWriter.format("\tli $t%d,%d\n",idx,value);
	}
	public void add(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		int dstidx=InterferenceGraph.getInstance().allNodes.get(dst).assignedColor;

		fileWriter.format("\tadd $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void mul(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		int dstidx=InterferenceGraph.getInstance().allNodes.get(dst).assignedColor;

		fileWriter.format("\tmul $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void label(String inlabel)
	{
		if (inlabel.equals("main"))
		{
			fileWriter.format(".text\n");
			fileWriter.format("%s:\n",inlabel);
		}
		else
		{
			fileWriter.format("%s:\n",inlabel);
		}
	}	
	public void jump(String inlabel)
	{
		fileWriter.format("\tj %s\n",inlabel);
	}	
	public void blt(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		
		fileWriter.format("\tblt $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void bge(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		
		fileWriter.format("\tbge $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void bne(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		
		fileWriter.format("\tbne $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void beq(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
		int i2 =InterferenceGraph.getInstance().allNodes.get(oprnd2).assignedColor;
		
		fileWriter.format("\tbeq $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void beqz(Temp oprnd1, String label)
	{
		int i1 =InterferenceGraph.getInstance().allNodes.get(oprnd1).assignedColor;
				
		fileWriter.format("\tbeq $t%d,$zero,%s\n",i1,label);
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MipsGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MipsGenerator() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MipsGenerator();

			try
			{
				/*********************************************************************************/
				/* [1] Open the MIPS text file and write data section with error message strings */
				/*********************************************************************************/
				String dirname="./output/";
				String filename=String.format("MIPS.txt");

				/***************************************/
				/* [2] Open MIPS text file for writing */
				/***************************************/
				instance.fileWriter = new PrintWriter(dirname+filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			/*****************************************************/
			/* [3] Print data section with error message strings */
			/*****************************************************/
			instance.fileWriter.print(".data\n");
			instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
			instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
			instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
		}
		return instance;
	}
}
