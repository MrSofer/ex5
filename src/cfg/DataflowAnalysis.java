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
		
		
		// Initialize data for all blocks to bottom (empty = nothing initialized)
		for (BasicBlock block : cfg.getBlocks()) {
			initializedVarsAtEntry.put(block, new HashSet<>());
			initializedTempsAtEntry.put(block, new HashSet<>());
			initializedVarsAtExit.put(block, new HashSet<>());
			initializedTempsAtExit.put(block, new HashSet<>());
		}
		
		
		// Chaotic iteration: keep iterating until fixed point
		boolean changed = true;
		int iteration = 0;
		
		while (changed) {
			changed = false;
			iteration++;
			
			// Process all blocks in each iteration
			for (BasicBlock block : cfg.getBlocks()) {
				
				// Compute initialization status at block entry from predecessors
				Set<String> varsAtEntry = new HashSet<>();
				Set<Temp> tempsAtEntry = new HashSet<>();
				
				if (block == cfg.getEntryBlock()) {
					// Entry block starts with nothing initialized
					varsAtEntry = new HashSet<>();
					tempsAtEntry = new HashSet<>();
				} else {
					// Meet operation: A variable is initialized at block entry if it's
					// initialized at the exit of ALL predecessors (intersection)
					// Use the exit values from the PREVIOUS iteration (cached)
					// Special handling for back edges in first iteration: skip them if unprocessed
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
							continue;
						}
						
						
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
					}
					
				}
				
				// Check if entry state changed
				Set<String> oldVarsAtEntry = initializedVarsAtEntry.get(block);
				Set<Temp> oldTempsAtEntry = initializedTempsAtEntry.get(block);
				
				if (!varsAtEntry.equals(oldVarsAtEntry) || !tempsAtEntry.equals(oldTempsAtEntry)) {
					initializedVarsAtEntry.put(block, varsAtEntry);
					initializedTempsAtEntry.put(block, tempsAtEntry);
					changed = true; // Need another iteration
				} else {
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
			}
		}
		
		
		// Now analyze each block to find uninitialized uses
		for (BasicBlock block : cfg.getBlocks()) {
			analyzeBlockForUninitializedUses(block);
		}
		
		
		return uninitializedVars;
	}
	
	/**
	 * Analyze a block to find uninitialized variable uses
	 */
	private void analyzeBlockForUninitializedUses(BasicBlock block)
	{
		Set<String> initialized = new HashSet<>(initializedVarsAtEntry.get(block));
		Set<Temp> initializedTemps = new HashSet<>(initializedTempsAtEntry.get(block));
		
		
		for (IrCommand cmd : block.getInstructions()) {
			// Check for uninitialized use before processing
			if (cmd instanceof IrCommandLoad) {
				IrCommandLoad loadCmd = (IrCommandLoad) cmd;
				if (!initialized.contains(loadCmd.getVarName())) {
					uninitializedVars.add(loadCmd.getVarName());
				}
			}
			
			// Update initialization status
			processCommandForInitialization(cmd, initialized, initializedTemps);
		}
		
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
			} else {
			}
		}
		else if (cmd instanceof IrCommandStore) {
			IrCommandStore storeCmd = (IrCommandStore) cmd;
			// Variable becomes initialized only if source temp is initialized
			if (initializedTemps.contains(storeCmd.getSrc())) {
				initialized.add(storeCmd.getVarName());
			} else {
				// If storing uninitialized temp, variable is not initialized
				initialized.remove(storeCmd.getVarName());
			}
		}
		else if (cmd instanceof IrCommandAllocate) {
			IrCommandAllocate allocCmd = (IrCommandAllocate) cmd;
			// Allocate resets initialization status
			initialized.remove(allocCmd.getVarName());
		}
		else if (cmd instanceof IRcommandConstInt) {
			IRcommandConstInt constCmd = (IRcommandConstInt) cmd;
			// Constants are always initialized
			initializedTemps.add(constCmd.getTemp());
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
