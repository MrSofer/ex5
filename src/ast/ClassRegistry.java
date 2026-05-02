package ast;
import java.util.*;

public class ClassRegistry {
    private static ClassRegistry instance = null;
    private Map<String, AstDecList> classDataMembers = new HashMap<>();
    public static ClassRegistry getInstance() {
        if (instance == null) instance = new ClassRegistry();
        return instance;
    }
    public void register(String className, AstDecList dataMembers) {
        classDataMembers.put(className, dataMembers);
    }
    public AstDecList getDataMembers(String className) {
        return classDataMembers.get(className);
    }
}
