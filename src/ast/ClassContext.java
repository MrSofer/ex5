package ast;

import types.*;
import ir.*;
import temp.*;

import java.util.*;

/**
 * Tracks the current class method compilation context so that
 * field accesses and `this` can be resolved during IR generation.
 */
public class ClassContext {

    private static ClassContext instance = null;

    private TypeClass currentClassType = null;
    private String thisLabel = null;

    public static ClassContext getInstance() {
        if (instance == null) instance = new ClassContext();
        return instance;
    }

    public void enterClass(TypeClass tc, String thisLbl) {
        this.currentClassType = tc;
        this.thisLabel = thisLbl;
    }

    public void leaveClass() {
        this.currentClassType = null;
        this.thisLabel = null;
    }

    public boolean isInClassMethod() { return currentClassType != null; }
    public TypeClass getCurrentClassType() { return currentClassType; }
    public String getThisLabel() { return thisLabel; }

    // ---------------------------------------------------------------
    // Static helpers for field layout (used by multiple AST nodes)
    // ---------------------------------------------------------------

    public static int countNonMethodFields(TypeClass tc) {
        if (tc == null) return 0;
        int count = countNonMethodFields(tc.father);
        for (TypeClassVarDecList it = tc.dataMembers; it != null; it = it.tail) {
            if (!(it.head.t instanceof TypeFunction)) count++;
        }
        return count;
    }

    /**
     * Returns the byte offset of fieldName within an object of class tc,
     * or -1 if not found.  Fields are laid out parent-first, in declaration
     * order (reversed relative to the linked list, which is in reverse-decl order).
     */
    public static int getFieldOffset(TypeClass tc, String fieldName) {
        if (tc == null) return -1;

        // Check parent hierarchy first
        int parentOffset = getFieldOffset(tc.father, fieldName);
        if (parentOffset >= 0) return parentOffset;

        int baseOffset = countNonMethodFields(tc.father) * 4;

        // Collect own non-method fields in declaration order (list is reversed)
        List<String> ownFields = new ArrayList<>();
        for (TypeClassVarDecList it = tc.dataMembers; it != null; it = it.tail) {
            if (!(it.head.t instanceof TypeFunction)) {
                ownFields.add(0, it.head.name); // prepend → declaration order
            }
        }
        for (int i = 0; i < ownFields.size(); i++) {
            if (ownFields.get(i).equals(fieldName)) return 4 + baseOffset + i * 4;
        }
        return -1;
    }

    /**
     * Emit inline field-initialization IR for a newly allocated object.
     * thisPtr is the Temp holding the object pointer.
     */
    public static void emitFieldInit(TypeClass tc, Temp thisPtr) {
        if (tc == null) return;
        // Recurse for parent fields first
        emitFieldInit(tc.father, thisPtr);

        int baseOffset = 4 + countNonMethodFields(tc.father) * 4;
        AstDecList dataMembers = ClassRegistry.getInstance().getDataMembers(tc.name);
        List<AstDecVar> ownVarFields = new ArrayList<>();
        if (dataMembers != null) {
            for (AstDecList it = dataMembers; it != null; it = it.tail) {
                if (it.head instanceof AstDecVar varDec) {
                    ownVarFields.add(varDec); // append → declaration order
                }
            }
        }

        for (int i = 0; i < ownVarFields.size(); i++) {
            int offset = baseOffset + i * 4;
            AstDecVar varDec = ownVarFields.get(i);

            Temp value;
            if (varDec.initialValue != null) {
                value = varDec.initialValue.irMe();
            } else {
                value = TempFactory.getInstance().getFreshTemp();
                Ir.getInstance().AddIrCommand(new IRcommandConstInt(value, 0));
            }

            Temp offTemp = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IRcommandConstInt(offTemp, offset));
            Temp addr = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IrCommandPtrAdd(addr, thisPtr, offTemp));
            Ir.getInstance().AddIrCommand(new IrCommandStoreIndirect(addr, value));
        }
    }
}
