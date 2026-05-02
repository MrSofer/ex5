package cfg;

import ir.*;
import temp.*;
import java.util.*;

/**
 * Dataflow analysis on the Control Flow Graph to detect uninitialized variables.
 * Uses a worklist algorithm to compute initialization status at each program point.
 */
public class DataflowAnalysis
{
	private ControlFlowGraph cfg;
	
	// For each basic block, track what variables/temps are initialized at entry
	private Map<BasicBlock, Set<String>> initializedVarsAtEntry;
	private Map<BasicBlock, Set<Temp>> initializedTempsAtEntry;
	
	// Cache exit states for use in meet operations
	private Map<BasicBlock, Set<String>> initializedVarsAtExit;
	private Map<BasicBlock, Set<Temp>> initializedTempsAtExit;
	
	// Track uninitialized variable uses
	private Set<String> uninitializedVars;
	
	public DataflowAnalysis(ControlFlowGraph cfg)
	{
		this.cfg = cfg;
		this.initializedVarsAtEntry = new HashMap<>();
		this.initializedTempsAtEntry = new HashMap<>();
		this.initializedVarsAtExit = new HashMap<>();
		this.initializedTempsAtExit = new HashMap<>();
		this.uninitializedVars = new HashSet<>();
	}
	
	/**
	 * Perform forward dataflow analysis to find uninitialized variables
	 * Uses chaotic iteration algorithm
	 */
	public Set<String> findUninitializedVariables()
	{
		uninitializedVars = new HashSet<>();
		
		System.out.println("\n========== DATAFLOW ANALYSIS START (Chaotic Iteration) ==========");
		
		// Initialize data for all blocks to bottom (empty = nothing initialized)
		for (BasicBlock block : cfg.getBlocks()) {
			initializedVarsAtEntry.put(block, new HashSet<>());
			initializedTempsAtEntry.put(block, new HashSet<>());
			initializedVarsAtExit.put(block, new HashSet<>());
			initializedTempsAtExit.put(block, new HashSet<>());
		}
		
		System.out.println("Total blocks: " + cfg.getBlocks().size());
		
		// Chaotic iteration: keep iterating until fixed point
		boolean changed = true;
		int iteration = 0;
		
		while (changed) {
			changed = false;
			iteration++;
			System.out.println("\n========== ITERATION " + iteration + " ==========");
			
			// Process all blocks in each iteration
			for (BasicBlock block : cfg.getBlocks()) {
				System.out.println("\nProcessing " + block);
				
				// Compute initialization status at block entry from predecessors
				Set<String> varsAtEntry = new HashSet<>();
				Set<Temp> tempsAtEntry = new HashSet<>();
				
				if (block == cfg.getEntryBlock()) {
					// Entry block starts with nothing initialized
					varsAtEntry = new HashSet<>();
					tempsAtEntry = new HashSet<>();
					System.out.println("  Entry block - nothing initialized at start");
				} else {
					// Meet operation: A variable is initialized at block entry if it's
					// initialized at the exit of ALL predecessors (intersection)
					// Use the exit values from the PREVIOUS iteration (cached)
					// Special handling for back edges in first iteration: skip them if unprocessed
					System.out.println("  Predecessors: " + block.getPredecessors().size());
					boolean firstPred = true;
					int processedPredCount = 0;
					for (BasicBlock pred : block.getPredecessors()) {
						// Use cached exit values from previous iteration
						Set<String> predVars = new HashSet<>(initializedVarsAtExit.get(pred));
						Set<Temp> predTemps = new HashSet<>(initializedTempsAtExit.get(pred));
						
						// Detect back edges: predecessor with higher or equal ID (comes later in list)
						// In first iteration, skip back edges that haven't produced results yet
						// We detect "not yet processed" by checking if BOTH sets are empty
						// This is a heuristic: truly unprocessed blocks have both sets empty
						boolean isBackEdge = (pred.getId() > block.getId());
						boolean appearsUnprocessed = (predVars.isEmpty() && predTemps.isEmpty());
						boolean skipPred = (iteration == 1 && isBackEdge && appearsUnprocessed);
						
						if (skipPred) {
							System.out.println("    From " + pred + ": SKIPPING (back edge in first iteration, appears unprocessed)");
							continue;
						}
						
						System.out.println("    From " + pred + ": vars=" + predVars + ", temps=" + predTemps.size());
						
						if (firstPred) {
							varsAtEntry.addAll(predVars);
							tempsAtEntry.addAll(predTemps);
							firstPred = false;
							processedPredCount++;
						} else {
							// Intersection: only vars initialized in ALL paths
							varsAtEntry.retainAll(predVars);
							tempsAtEntry.retainAll(predTemps);
							processedPredCount++;
						}
					}
					
					// If all predecessors were skipped, varsAtEntry and tempsAtEntry remain empty
					// This is correct: with no information from predecessors, assume nothing initialized
					if (processedPredCount == 0) {
						System.out.println("  Note: All predecessors skipped, using empty entry state");
					}
					
					System.out.println("  Block entry after meet: vars=" + varsAtEntry);
				}
				
				// Check if entry state changed
				Set<String> oldVarsAtEntry = initializedVarsAtEntry.get(block);
				Set<Temp> oldTempsAtEntry = initializedTempsAtEntry.get(block);
				
				if (!varsAtEntry.equals(oldVarsAtEntry) || !tempsAtEntry.equals(oldTempsAtEntry)) {
					System.out.println("  Entry state CHANGED for " + block);
					initializedVarsAtEntry.put(block, varsAtEntry);
					initializedTempsAtEntry.put(block, tempsAtEntry);
					changed = true; // Need another iteration
				} else {
					System.out.println("  No change in entry state for " + block);
				}
				
				// Compute exit state for this block
				Set<String> varsAtExit = new HashSet<>(varsAtEntry);
				Set<Temp> tempsAtExit = new HashSet<>(tempsAtEntry);
				
				for (IrCommand cmd : block.getInstructions()) {
					processCommandForInitialization(cmd, varsAtExit, tempsAtExit);
				}
				
				// Update cached exit state for next iteration
				initializedVarsAtExit.put(block, varsAtExit);
				initializedTempsAtExit.put(block, tempsAtExit);
			}
			
			if (!changed) {
				System.out.println("\n========== FIXED POINT REACHED after " + iteration + " iterations ==========");
			}
		}
		
		System.out.println("\n========== ANALYZING FOR UNINITIALIZED USES ==========");
		
		// Now analyze each block to find uninitialized uses
		for (BasicBlock block : cfg.getBlocks()) {
			System.out.println("\nAnalyzing block " + block + " for uninitialized uses:");
			analyzeBlockForUninitializedUses(block);
		}
		
		System.out.println("\n========== DATAFLOW ANALYSIS COMPLETE ==========");
		System.out.println("Uninitialized variables: " + uninitializedVars);
		System.out.println("==============================================\n");
		
		return uninitializedVars;
	}
	
	/**
	 * Analyze a block to find uninitialized variable uses
	 */
	private void analyzeBlockForUninitializedUses(BasicBlock block)
	{
		Set<String> initialized = new HashSet<>(initializedVarsAtEntry.get(block));
		Set<Temp> initializedTemps = new HashSet<>(initializedTempsAtEntry.get(block));
		
		System.out.println("  Entry: initialized vars=" + initialized);
		
		for (IrCommand cmd : block.getInstructions()) {
			// Check for uninitialized use before processing
			if (cmd instanceof IrCommandLoad) {
				IrCommandLoad loadCmd = (IrCommandLoad) cmd;
				System.out.println("    Load(" + loadCmd.getVarName() + ") - initialized=" + initialized.contains(loadCmd.getVarName()));
				if (!initialized.contains(loadCmd.getVarName())) {
					System.out.println("      -> UNINITIALIZED USE DETECTED: " + loadCmd.getVarName());
					uninitializedVars.add(loadCmd.getVarName());
				}
			}
			
			// Update initialization status
			processCommandForInitialization(cmd, initialized, initializedTemps);
		}
		
		System.out.println("  Exit: initialized vars=" + initialized);
	}
	
	/**
	 * Process a command to update initialization status
	 */
	private void processCommandForInitialization(IrCommand cmd, Set<String> initialized, Set<Temp> initializedTemps)
	{
		if (cmd instanceof IrCommandLoad) {
			IrCommandLoad loadCmd = (IrCommandLoad) cmd;
			// If loading from initialized var, temp becomes initialized
			if (initialized.contains(loadCmd.getVarName())) {
				initializedTemps.add(loadCmd.getDst());
				System.out.println("      Temp " + loadCmd.getDst().getSerialNumber() + " <- initialized from " + loadCmd.getVarName());
			} else {
				System.out.println("      Temp " + loadCmd.getDst().getSerialNumber() + " <- UNINITIALIZED from " + loadCmd.getVarName());
			}
		}
		else if (cmd instanceof IrCommandStore) {
			IrCommandStore storeCmd = (IrCommandStore) cmd;
			// Variable becomes initialized only if source temp is initialized
			if (initializedTemps.contains(storeCmd.getSrc())) {
				initialized.add(storeCmd.getVarName());
				System.out.println("      " + storeCmd.getVarName() + " <- initialized from Temp " + storeCmd.getSrc().getSerialNumber());
			} else {
				// If storing uninitialized temp, variable is not initialized
				initialized.remove(storeCmd.getVarName());
				System.out.println("      " + storeCmd.getVarName() + " <- UNINITIALIZED from Temp " + storeCmd.getSrc().getSerialNumber());
			}
		}
		else if (cmd instanceof IrCommandAllocate) {
			IrCommandAllocate allocCmd = (IrCommandAllocate) cmd;
			// Allocate resets initialization status
			initialized.remove(allocCmd.getVarName());
			System.out.println("      Allocate(" + allocCmd.getVarName() + ") - reset initialization");
		}
		else if (cmd instanceof IRcommandConstInt) {
			IRcommandConstInt constCmd = (IRcommandConstInt) cmd;
			// Constants are always initialized
			initializedTemps.add(constCmd.getTemp());
			System.out.println("      Temp " + constCmd.getTemp().getSerialNumber() + " <- const(" + constCmd.getValue() + ") - initialized");
		}
		// Binary operations
		else if (cmd instanceof IrCommandBinopAddIntegers) {
			IrCommandBinopAddIntegers binop = (IrCommandBinopAddIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopDivIntegers) {
			IrCommandBinopDivIntegers binop = (IrCommandBinopDivIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopEqIntegers) {
			IrCommandBinopEqIntegers binop = (IrCommandBinopEqIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopGtIntegers) {
			IrCommandBinopGtIntegers binop = (IrCommandBinopGtIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopLtIntegers) {
			IrCommandBinopLtIntegers binop = (IrCommandBinopLtIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopMinIntegers) {
			IrCommandBinopMinIntegers binop = (IrCommandBinopMinIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
		else if (cmd instanceof IrCommandBinopMulIntegers) {
			IrCommandBinopMulIntegers binop = (IrCommandBinopMulIntegers) cmd;
			if (initializedTemps.contains(binop.t1) && initializedTemps.contains(binop.t2)) {
				initializedTemps.add(binop.dst);
			}
		}
	}
}
