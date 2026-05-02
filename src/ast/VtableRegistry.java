package ast;

import types.*;
import mips.MipsGenerator;
import java.util.*;

public class VtableRegistry {
    private final Map<String, List<String>> methodOrder = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> impls = new LinkedHashMap<>();

    private static VtableRegistry instance = null;
    public static VtableRegistry getInstance() {
        if (instance == null) instance = new VtableRegistry();
        return instance;
    }
    protected VtableRegistry() {}

    public void buildAll(AstDecList program) {
        for (AstDecList it = program; it != null; it = it.tail) {
            if (it.head instanceof AstDecClass c) {
                types.Type t = symboltable.SymbolTable.getInstance().find(c.name);
                if (t instanceof TypeClass tc) buildVtable(tc);
            }
        }
    }

    private void buildVtable(TypeClass tc) {
        if (methodOrder.containsKey(tc.name)) return;
        if (tc.father != null) buildVtable(tc.father);

        List<String> order = new ArrayList<>(
            tc.father != null ? methodOrder.get(tc.father.name) : Collections.emptyList());
        Map<String, String> impl = new LinkedHashMap<>(
            tc.father != null ? impls.get(tc.father.name) : Collections.emptyMap());

        // Collect this class's own methods in declaration order
        // dataMembers is in REVERSE declaration order (prepended during semantMe)
        List<String> ownMethods = new ArrayList<>();
        for (TypeClassVarDecList it = tc.dataMembers; it != null; it = it.tail) {
            if (it.head.t instanceof TypeFunction) {
                ownMethods.add(0, it.head.name); // prepend to get declaration order
            }
        }

        for (String m : ownMethods) {
            String label = tc.name + "_" + m;
            impl.put(m, label);
            if (!order.contains(m)) order.add(m);
        }

        methodOrder.put(tc.name, order);
        impls.put(tc.name, impl);
    }

    public int getMethodIndex(String className, String methodName) {
        List<String> order = methodOrder.get(className);
        if (order == null) return -1;
        return order.indexOf(methodName);
    }

    public String getRootMethodOwner(String className, String methodName) {
        Map<String, String> classImpl = impls.get(className);
        if (classImpl == null || !classImpl.containsKey(methodName)) return className;
        TypeClass tc = (TypeClass) symboltable.SymbolTable.getInstance().find(className);
        return findRoot(tc, methodName);
    }

    private String findRoot(TypeClass tc, String methodName) {
        if (tc == null) return null;
        if (tc.father != null) {
            String parentRoot = findRoot(tc.father, methodName);
            if (parentRoot != null) return parentRoot;
        }
        for (TypeClassVarDecList it = tc.dataMembers; it != null; it = it.tail) {
            if (it.head.name.equals(methodName) && it.head.t instanceof TypeFunction) {
                return tc.name;
            }
        }
        return null;
    }

    public boolean hasVtable(String className) {
        List<String> order = methodOrder.get(className);
        return order != null && !order.isEmpty();
    }

    public void emitVtables() {
        for (Map.Entry<String, List<String>> entry : methodOrder.entrySet()) {
            String className = entry.getKey();
            List<String> order = entry.getValue();
            if (order.isEmpty()) continue;
            Map<String, String> classImpl = impls.get(className);
            List<String> entries = new ArrayList<>();
            for (String m : order) {
                entries.add(classImpl.get(m));
            }
            MipsGenerator.getInstance().emitVtable(className, entries);
        }
    }
}
