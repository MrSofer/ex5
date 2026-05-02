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
		fileWriter.print("\tsubu $sp,$sp,4\n");
		fileWriter.print("\tsw $ra,0($sp)\n");
		fileWriter.print("\tjal global_init\n");
		fileWriter.print("\tlw $ra,0($sp)\n");
		fileWriter.print("\taddu $sp,$sp,4\n");
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
	public void callFunction(String funcName, java.util.List<String> callerParams)
	{
		// Save caller-saved registers $t0-$t7
		for (int i = 0; i < 8; i++) {
			fileWriter.format("\tsubu $sp,$sp,4\n");
			fileWriter.format("\tsw $t%d,0($sp)\n", i);
		}
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $ra,0($sp)\n");
		fileWriter.format("\tjal %s\n", funcName);
		fileWriter.format("\tlw $ra,0($sp)\n");
		fileWriter.format("\taddu $sp,$sp,4\n");
		// Restore caller-saved registers $t0-$t7 in reverse
		for (int i = 7; i >= 0; i--) {
			fileWriter.format("\tlw $t%d,0($sp)\n", i);
			fileWriter.format("\taddu $sp,$sp,4\n");
		}
	}

	public void saveGlobals(java.util.List<String> paramLabels) {
		if (paramLabels == null) return;
		for (String paramLabel : paramLabels) {
			fileWriter.format("\tlw $t8,global_%s\n", paramLabel);
			fileWriter.format("\tsubu $sp,$sp,4\n");
			fileWriter.format("\tsw $t8,0($sp)\n");
		}
	}

	public void restoreGlobals(java.util.List<String> paramLabels) {
		if (paramLabels == null) return;
		for (int i = paramLabels.size() - 1; i >= 0; i--) {
			fileWriter.format("\tlw $t8,0($sp)\n");
			fileWriter.format("\taddu $sp,$sp,4\n");
			fileWriter.format("\tsw $t8,global_%s\n", paramLabels.get(i));
		}
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
	private final java.util.Set<String> allocatedVars = new java.util.HashSet<>();
	public void allocate(String varName)
	{
		if (allocatedVars.add(varName)) {
			fileWriter.format(".data\n");
			fileWriter.format("\tglobal_%s: .word 0\n",varName);
			fileWriter.format(".text\n");
		}
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
		saturate(dstidx);
	}
	public void mul(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tmul $t%d,$t%d,$t%d\n",dstidx,i1,i2);
		saturate(dstidx);
	}
	public void div(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		// Floor division: MIPS truncates towards zero, L language floors towards -inf
		fileWriter.format("\tdiv $t%d,$t%d\n", i1, i2);
		fileWriter.format("\tmflo $t%d\n", dstidx);
		fileWriter.format("\tmfhi $t8\n");
		fileWriter.format("\tbeqz $t8,_floor_end_%d\n", divCounter);
		fileWriter.format("\txor $t9,$t%d,$t%d\n", i1, i2);
		fileWriter.format("\tbgez $t9,_floor_end_%d\n", divCounter);
		fileWriter.format("\taddi $t%d,$t%d,-1\n", dstidx, dstidx);
		fileWriter.format("_floor_end_%d:\n", divCounter);
		divCounter++;
		saturate(dstidx);
	}
	public void sub(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int i1 =colorOf(oprnd1);
		int i2 =colorOf(oprnd2);
		int dstidx=colorOf(dst);

		fileWriter.format("\tsub $t%d,$t%d,$t%d\n",dstidx,i1,i2);
		saturate(dstidx);
	}

	private int satCounter = 0;
	private int divCounter = 0;

	public void checkDivByZero(Temp divisor) {
		int idx = colorOf(divisor);
		String ok = "_div_ok_" + checkCounter++;
		fileWriter.format("\tbne $t%d,$zero,%s\n", idx, ok);
		fileWriter.format("\tla $a0,string_illegal_div_by_0\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", ok);
	}

	public void checkNullPtr(Temp ptr) {
		int idx = colorOf(ptr);
		String ok = "_null_ok_" + checkCounter++;
		fileWriter.format("\tbne $t%d,$zero,%s\n", idx, ok);
		fileWriter.format("\tla $a0,string_invalid_ptr_dref\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", ok);
	}

	public void checkArrayBounds(Temp ptr, Temp idx) {
		int pReg = colorOf(ptr);
		int iReg = colorOf(idx);
		String nullErrLabel   = "_null_err_"   + checkCounter;
		String boundsErrLabel = "_bounds_err_" + checkCounter;
		String okLabel        = "_bounds_ok_"  + checkCounter++;
		// Null check (ptr == 0) → Invalid Pointer Dereference
		fileWriter.format("\tbeq $t%d,$zero,%s\n", pReg, nullErrLabel);
		// Negative index check → Access Violation
		fileWriter.format("\tbltz $t%d,%s\n", iReg, boundsErrLabel);
		// Upper bound check → Access Violation
		fileWriter.format("\tlw $t8,0($t%d)\n", pReg);
		fileWriter.format("\tbge $t%d,$t8,%s\n", iReg, boundsErrLabel);
		fileWriter.format("\tj %s\n", okLabel);
		// null error:
		fileWriter.format("%s:\n", nullErrLabel);
		fileWriter.format("\tla $a0,string_invalid_ptr_dref\n");
		fileWriter.format("\tli $v0,4\n\tsyscall\n\tli $v0,10\n\tsyscall\n");
		// bounds error:
		fileWriter.format("%s:\n", boundsErrLabel);
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n\tsyscall\n\tli $v0,10\n\tsyscall\n");
		fileWriter.format("%s:\n", okLabel);
	}

	private int checkCounter = 0;
	private int strOpCounter = 0;

	public void stringEq(Temp dst, Temp str1, Temp str2) {
		int d  = colorOf(dst);
		int s1 = colorOf(str1);
		int s2 = colorOf(str2);
		int c  = strOpCounter++;
		fileWriter.format("\tmove $t8,$t%d\n", s1);
		fileWriter.format("\tmove $t9,$t%d\n", s2);
		fileWriter.format("_seq_loop_%d:\n", c);
		fileWriter.format("\tlb $a0,0($t8)\n");
		fileWriter.format("\tlb $a1,0($t9)\n");
		fileWriter.format("\tbne $a0,$a1,_seq_ne_%d\n", c);
		fileWriter.format("\tbeqz $a0,_seq_eq_%d\n", c);
		fileWriter.format("\taddu $t8,$t8,1\n");
		fileWriter.format("\taddu $t9,$t9,1\n");
		fileWriter.format("\tj _seq_loop_%d\n", c);
		fileWriter.format("_seq_ne_%d:\n", c);
		fileWriter.format("\tli $t%d,0\n", d);
		fileWriter.format("\tj _seq_end_%d\n", c);
		fileWriter.format("_seq_eq_%d:\n", c);
		fileWriter.format("\tli $t%d,1\n", d);
		fileWriter.format("_seq_end_%d:\n", c);
	}

	public void stringConcat(Temp dst, Temp str1, Temp str2) {
		int d  = colorOf(dst);
		int s1 = colorOf(str1);
		int s2 = colorOf(str2);
		int c  = strOpCounter++;
		// strlen(str1) into $a3
		fileWriter.format("\tmove $t8,$t%d\n", s1);
		fileWriter.format("\tli $a3,0\n");
		fileWriter.format("_scat_len1_%d:\n", c);
		fileWriter.format("\tlb $a0,0($t8)\n");
		fileWriter.format("\tbeqz $a0,_scat_l1d_%d\n", c);
		fileWriter.format("\taddu $t8,$t8,1\n");
		fileWriter.format("\taddu $a3,$a3,1\n");
		fileWriter.format("\tj _scat_len1_%d\n", c);
		fileWriter.format("_scat_l1d_%d:\n", c);
		// strlen(str2) into $a0
		fileWriter.format("\tmove $t8,$t%d\n", s2);
		fileWriter.format("\tli $a0,0\n");
		fileWriter.format("_scat_len2_%d:\n", c);
		fileWriter.format("\tlb $a1,0($t8)\n");
		fileWriter.format("\tbeqz $a1,_scat_l2d_%d\n", c);
		fileWriter.format("\taddu $t8,$t8,1\n");
		fileWriter.format("\taddu $a0,$a0,1\n");
		fileWriter.format("\tj _scat_len2_%d\n", c);
		fileWriter.format("_scat_l2d_%d:\n", c);
		// Allocate len1+len2+1 bytes via sbrk
		fileWriter.format("\taddu $a0,$a0,$a3\n");
		fileWriter.format("\taddu $a0,$a0,1\n");
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		// Copy str1 into new buffer ($v0)
		fileWriter.format("\tmove $a2,$v0\n");
		fileWriter.format("\tmove $t8,$t%d\n", s1);
		fileWriter.format("_scat_cp1_%d:\n", c);
		fileWriter.format("\tlb $a1,0($t8)\n");
		fileWriter.format("\tsb $a1,0($a2)\n");
		fileWriter.format("\tbeqz $a1,_scat_cp1d_%d\n", c);
		fileWriter.format("\taddu $t8,$t8,1\n");
		fileWriter.format("\taddu $a2,$a2,1\n");
		fileWriter.format("\tj _scat_cp1_%d\n", c);
		fileWriter.format("_scat_cp1d_%d:\n", c);
		// Copy str2 starting at the NUL position (overwrite it)
		fileWriter.format("\tmove $t8,$t%d\n", s2);
		fileWriter.format("_scat_cp2_%d:\n", c);
		fileWriter.format("\tlb $a1,0($t8)\n");
		fileWriter.format("\tsb $a1,0($a2)\n");
		fileWriter.format("\tbeqz $a1,_scat_cp2d_%d\n", c);
		fileWriter.format("\taddu $t8,$t8,1\n");
		fileWriter.format("\taddu $a2,$a2,1\n");
		fileWriter.format("\tj _scat_cp2_%d\n", c);
		fileWriter.format("_scat_cp2d_%d:\n", c);
		// Result is $v0
		fileWriter.format("\tmove $t%d,$v0\n", d);
	}

	private void saturate(int dst) {
		fileWriter.format("\tli $t8,32767\n");
		fileWriter.format("\tbgt $t%d,$t8,_sat_max_%d\n", dst, satCounter);
		fileWriter.format("\tli $t9,-32768\n");
		fileWriter.format("\tblt $t%d,$t9,_sat_min_%d\n", dst, satCounter);
		fileWriter.format("\tj _sat_end_%d\n", satCounter);
		fileWriter.format("_sat_max_%d:\n", satCounter);
		fileWriter.format("\tli $t%d,32767\n", dst);
		fileWriter.format("\tj _sat_end_%d\n", satCounter);
		fileWriter.format("_sat_min_%d:\n", satCounter);
		fileWriter.format("\tli $t%d,-32768\n", dst);
		fileWriter.format("_sat_end_%d:\n", satCounter);
		satCounter++;
	}

	private int stringCounter = 0;

	public void printString(String value) {
		String lbl = "str_lit_" + (stringCounter++);
		String stripped = value;
		if (stripped.startsWith("\"") && stripped.endsWith("\"") && stripped.length() >= 2)
			stripped = stripped.substring(1, stripped.length() - 1);
		fileWriter.format(".data\n");
		fileWriter.format("\t%s: .asciiz \"%s\"\n", lbl, stripped);
		fileWriter.format(".text\n");
		fileWriter.format("\tla $a0,%s\n", lbl);
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	public void loadString(Temp t, String value) {
		int idx = colorOf(t);
		String lbl = "str_lit_" + (stringCounter++);
		String stripped = value;
		if (stripped.startsWith("\"") && stripped.endsWith("\"") && stripped.length() >= 2)
			stripped = stripped.substring(1, stripped.length() - 1);
		fileWriter.format(".data\n");
		fileWriter.format("\t%s: .asciiz \"%s\"\n", lbl, stripped);
		fileWriter.format(".text\n");
		fileWriter.format("\tla $t%d,%s\n", idx, lbl);
	}

	public void printStringFromReg(Temp t) {
		int idx = colorOf(t);
		fileWriter.format("\tmove $a0,$t%d\n", idx);
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	public void ptrAdd(Temp dst, Temp base, Temp offset)
	{
		int dstidx = colorOf(dst);
		int bidx   = colorOf(base);
		int oidx   = colorOf(offset);
		fileWriter.format("\tadd $t%d,$t%d,$t%d\n", dstidx, bidx, oidx);
	}

	public void ptrMul(Temp dst, Temp oprnd1, Temp oprnd2)
	{
		int dstidx = colorOf(dst);
		int i1     = colorOf(oprnd1);
		int i2     = colorOf(oprnd2);
		fileWriter.format("\tmul $t%d,$t%d,$t%d\n", dstidx, i1, i2);
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
	
	public void callVirtual(temp.Temp objPtr, int vtableIndex) {
		int reg = colorOf(objPtr);
		// Load vtable pointer from offset 0 of object
		fileWriter.format("\tlw $t8,0($t%d)\n", reg);
		// Load method pointer from vtable at vtableIndex*4
		fileWriter.format("\tlw $t8,%d($t8)\n", vtableIndex * 4);
		// Save t0-t7
		for (int i = 0; i < 8; i++) {
			fileWriter.format("\tsubu $sp,$sp,4\n\tsw $t%d,0($sp)\n", i);
		}
		// Save ra
		fileWriter.format("\tsubu $sp,$sp,4\n\tsw $ra,0($sp)\n");
		fileWriter.format("\tjalr $t8\n");
		// Restore ra
		fileWriter.format("\tlw $ra,0($sp)\n\taddu $sp,$sp,4\n");
		// Restore t7-t0
		for (int i = 7; i >= 0; i--) {
			fileWriter.format("\tlw $t%d,0($sp)\n\taddu $sp,$sp,4\n", i);
		}
	}

	public void emitVtable(String className, java.util.List<String> entries) {
		fileWriter.format(".data\n%s_vtable:\n", className);
		for (String e : entries) {
			fileWriter.format("\t.word %s\n", e);
		}
		fileWriter.format(".text\n");
	}

	public void loadAddress(Temp dst, String label) {
		fileWriter.format("\tla $t%d,%s\n", colorOf(dst), label);
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MipsGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MipsGenerator() {}

	public static void init(String outputPath) {
		instance = new MipsGenerator();
		try {
			instance.fileWriter = new PrintWriter(outputPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance.fileWriter.print(".data\n");
		instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
		instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
		instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
		instance.fileWriter.print("\tglobal__virtual_retval: .word 0\n");
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance()
	{
		if (instance == null)
		{
			init("./output/MIPS.txt");
		}
		return instance;
	}
}
