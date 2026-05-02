package cfg;

import ir.*;
import java.util.*;

/**
 * Represents a basic block in the control flow graph.
 * A basic block is a sequence of instructions with:
 * - A single entry point (at the beginning)
 * - A single exit point (at the end)
 * - No branches except at the end
 */
public class BasicBlock
{
	private int id;
	private List<IrCommand> instructions;
	private Set<BasicBlock> predecessors;
	private Set<BasicBlock> successors;
	private String label; // Optional label for this block
	
	public BasicBlock(int id)
	{
		this.id = id;
		this.instructions = new ArrayList<>();
		this.predecessors = new HashSet<>();
		this.successors = new HashSet<>();
		this.label = null;
	}
	
	public int getId() { return id; }
	
	public List<IrCommand> getInstructions() { return instructions; }
	
	public void addInstruction(IrCommand cmd) { instructions.add(cmd); }
	
	public Set<BasicBlock> getPredecessors() { return predecessors; }
	
	public Set<BasicBlock> getSuccessors() { return successors; }
	
	public void addPredecessor(BasicBlock block) { predecessors.add(block); }
	
	public void addSuccessor(BasicBlock block) { successors.add(block); }
	
	public String getLabel() { return label; }
	
	public void setLabel(String label) { this.label = label; }
	
	@Override
	public String toString()
	{
		return "Block" + id + (label != null ? "(" + label + ")" : "");
	}
}
