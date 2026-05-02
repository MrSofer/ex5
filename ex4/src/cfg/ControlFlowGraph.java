package cfg;

import ir.*;
import temp.*;
import java.util.*;

/**
 * Control Flow Graph representation for IR commands.
 * Builds a CFG from the flat IR command list by identifying
 * basic blocks and their control flow relationships.
 */
public class ControlFlowGraph
{
	private List<BasicBlock> blocks;
	private BasicBlock entryBlock;
	private BasicBlock exitBlock;
	private Map<String, BasicBlock> labelToBlock;
	
	public ControlFlowGraph()
	{
		this.blocks = new ArrayList<>();
		this.labelToBlock = new HashMap<>();
	}
	
	public List<BasicBlock> getBlocks() { return blocks; }
	
	public BasicBlock getEntryBlock() { return entryBlock; }
	
	public BasicBlock getExitBlock() { return exitBlock; }
	
	/**
	 * Build CFG from IR commands
	 */
	public void buildFromIR(Ir ir)
	{
		System.out.println("\n========== BUILDING CFG ==========");
		
		// Collect all IR commands into a list
		List<IrCommand> allCommands = new ArrayList<>();
		IrCommand head = ir.getHead();
		IrCommandList tail = ir.getTail();
		
		if (head != null) {
			allCommands.add(head);
		}
		
		IrCommandList current = tail;
		while (current != null) {
			if (current.head != null) {
				allCommands.add(current.head);
			}
			current = current.tail;
		}
		
		System.out.println("Total IR commands: " + allCommands.size());
		
		if (allCommands.isEmpty()) {
			return;
		}
		
		// Step 1: Identify block boundaries (labels and after jumps)
		Set<Integer> blockStarts = new HashSet<>();
		blockStarts.add(0); // First instruction starts a block
		
		for (int i = 0; i < allCommands.size(); i++) {
			IrCommand cmd = allCommands.get(i);
			
			// Labels start new blocks
			if (cmd instanceof IrCommandLabel) {
				blockStarts.add(i);
				System.out.println("  Block boundary at " + i + ": Label");
			}
			
			// Instruction after a jump starts a new block
			if (cmd instanceof IrCommandJumpLabel || cmd instanceof IrCommandJumpIfEqToZero) {
				if (i + 1 < allCommands.size()) {
					blockStarts.add(i + 1);
					System.out.println("  Block boundary at " + (i + 1) + ": After jump");
				}
			}
		}
		
		// Step 2: Create basic blocks
		List<Integer> sortedStarts = new ArrayList<>(blockStarts);
		Collections.sort(sortedStarts);
		
		System.out.println("\nCreating " + sortedStarts.size() + " basic blocks:");
		
		Map<Integer, BasicBlock> startIndexToBlock = new HashMap<>();
		
		for (int i = 0; i < sortedStarts.size(); i++) {
			BasicBlock block = new BasicBlock(i);
			blocks.add(block);
			startIndexToBlock.put(sortedStarts.get(i), block);
			
			int startIdx = sortedStarts.get(i);
			int endIdx = (i + 1 < sortedStarts.size()) ? sortedStarts.get(i + 1) : allCommands.size();
			
			System.out.println("  Block" + i + ": commands " + startIdx + " to " + (endIdx - 1));
			
			// Add instructions to block
			for (int j = startIdx; j < endIdx; j++) {
				IrCommand cmd = allCommands.get(j);
				block.addInstruction(cmd);
				
				System.out.println("    [" + j + "] " + cmd.getClass().getSimpleName());
				
				// Track labels
				if (cmd instanceof IrCommandLabel) {
					IrCommandLabel labelCmd = (IrCommandLabel) cmd;
					block.setLabel(labelCmd.getLabelName());
					labelToBlock.put(labelCmd.getLabelName(), block);
					System.out.println("      Label: " + labelCmd.getLabelName());
				}
			}
		}
		
		System.out.println("\n========== CFG CONSTRUCTION COMPLETE ==========\n");
		
		// Step 3: Connect blocks (add edges) - keeping original logic
		for (int i = 0; i < blocks.size(); i++) {
			BasicBlock block = blocks.get(i);
			List<IrCommand> instructions = block.getInstructions();
			
			if (instructions.isEmpty()) {
				continue;
			}
			
			IrCommand lastCmd = instructions.get(instructions.size() - 1);
			
			// Unconditional jump
			if (lastCmd instanceof IrCommandJumpLabel) {
				IrCommandJumpLabel jump = (IrCommandJumpLabel) lastCmd;
				BasicBlock target = labelToBlock.get(jump.getLabelName());
				if (target != null) {
					block.addSuccessor(target);
					target.addPredecessor(block);
				}
			}
			// Conditional jump
			else if (lastCmd instanceof IrCommandJumpIfEqToZero) {
				IrCommandJumpIfEqToZero jump = (IrCommandJumpIfEqToZero) lastCmd;
				BasicBlock target = labelToBlock.get(jump.getLabelName());
				if (target != null) {
					block.addSuccessor(target);
					target.addPredecessor(block);
				}
				
				// Fall through to next block
				if (i + 1 < blocks.size()) {
					BasicBlock nextBlock = blocks.get(i + 1);
					block.addSuccessor(nextBlock);
					nextBlock.addPredecessor(block);
				}
			}
			// Fall through to next block (no jump at end)
			else {
				if (i + 1 < blocks.size()) {
					BasicBlock nextBlock = blocks.get(i + 1);
					block.addSuccessor(nextBlock);
					nextBlock.addPredecessor(block);
				}
			}
		}
		
		// Set entry and exit blocks
		if (!blocks.isEmpty()) {
			entryBlock = blocks.get(0);
			
			// Exit block is one with no successors
			for (BasicBlock block : blocks) {
				if (block.getSuccessors().isEmpty()) {
					exitBlock = block;
					break;
				}
			}
		}
	}
}
