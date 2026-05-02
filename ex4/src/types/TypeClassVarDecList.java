package types;

public class TypeClassVarDecList
{
	public TypeClassVarDec head;
	public TypeClassVarDecList tail;
	
	public TypeClassVarDecList(TypeClassVarDec head, TypeClassVarDecList tail)
	{
		this.head = head;
		this.tail = tail;
	}

	public boolean containsFunction(String name) {
		if (head == null) {
			return false;
		}
		if (head.t instanceof TypeFunction) {
			if (tail == null) {
				return name.equals(head.name);
			}
			return name.equals(head.name) || tail.containsFunction(name);
		}
		if (tail != null)
			return tail.containsFunction(name);
		return false;

	}
}
