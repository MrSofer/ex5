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
import regalloc.InterferenceNode;
import temp.*;

public class MipsGenerator
{
	private static final int WORD_SIZE=4;
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;

	/****************************************************/
	/* Helper: safely get the register color for a Temp */
	/****************************************************/
	private int colorOf(Temp t)
	{
		if (t == null) return 0;
		regalloc.InterferenceNode node = InterferenceGraph.getInstance().allNodes.get(t);
		if (node == null) return 0;
		return node.assignedColor;
	}

	/***********************/
	/* The file writer ... */
	/***********************/
	public void finalizeFile()
	{
		fileWriter.print("main:\n");
		fileWriter.print("\tjal user_main\n");
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.close();
	}
	public void printInt(Temp t)
	{
		int idx = colorOf(t);
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
	public void callFunction(String funcName)
	{
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $ra,0($sp)\n");
		fileWriter.format("\tjal %s\n", funcName);
		fileWriter.format("\tlw $ra,0($sp)\n");
		fileWriter.format("\taddu $sp,$sp,4\n");
	}
	public void returnFromFunction()
	{
		fileWriter.format("\tjr $ra\n");
	}
	public void loadIndirect(Temp dst, Temp base)
	{
		int dstIdx  = colorOf(dst);
		int baseIdx = colorOf(base);
		fileWriter.format("\tlw $t%d,0($t%d)\n", dstIdx, baseIdx);
	}
	public void storeIndirect(Temp base, Temp src)
	{
		int baseIdx = colorOf(base);
		int srcIdx  = colorOf(src);
		fileWriter.format("\tsw $t%d,0($t%d)\n", srcIdx, baseIdx);
	}
	public void allocateHeap(Temp size, Temp result)
	{
		int sizeIdx   = colorOf(size);
		int resultIdx = colorOf(result);
		fileWriter.format("\tmove $a0,$t%d\n", sizeIdx);
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove $t%d,$v0\n", resultIdx);
	}
	public void allocate(String varName)
	{
		fileWriter.format(".data\n");
		fileWriter.format("\tglobal_%s: .word 721\n",varName);
		fileWriter.format(".text\n");
	}
	public void load(Temp dst, String varName)
	{
		int idxdst=colorOf(dst);
		fileWriter.format("\tlw $t%d,global_%s\n",idxdst,varName);
	}
	public void store(String varName, Temp src)
	{
		int idxsrc=colorOf(src);
		fileWriter.format("\tsw $t%d,global_%s\n",idxsrc,varName);
	}
	public void li(Temp t, int value)
	{
		int idx=colorOf(t);
		fileWriter.format("\tli $t%d,%d\n",idx,value);
	}
	public void add(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tadd $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void mul(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tmul $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void div(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tdiv $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void sub(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tsub $t%d,$t%d,$t%d\n",dstidx,i1,i2);
	}
	public void bgt(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);

		fileWriter.format("\tbgt $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void ble(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);

		fileWriter.format("\tble $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void label(String inlabel)
	{
		fileWriter.format(".text\n");
		fileWriter.format("%s:\n",inlabel);
	}	
	public void jump(String inlabel)
	{
		fileWriter.format("\tj %s\n",inlabel);
	}	
	public void blt(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		
		fileWriter.format("\tblt $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void bge(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		
		fileWriter.format("\tbge $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void bne(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		
		fileWriter.format("\tbne $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void beq(Temp oprnd1, Temp oprnd2, String label)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		
		fileWriter.format("\tbeq $t%d,$t%d,%s\n",i1,i2,label);
	}
	public void beqz(Temp oprnd1, String label)
	{
		int i1 =colorOf(oprnd1);
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
