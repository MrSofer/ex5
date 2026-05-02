package types;

public class TypeClass extends Type
{
	/*********************************************************************/
	/* If this class does not extend a father class this should be null  */
	/*********************************************************************/
	public TypeClass father;

	/**************************************************/
	/* Gather up all data members in one place        */
	/* Note that data members coming from the AST are */
	/* packed together with the class methods         */
	/**************************************************/
	public TypeClassVarDecList dataMembers;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeClass(TypeClass father, String name, TypeClassVarDecList dataMembers)
	{
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
	}

	/*************/
	/* isClass() */
	/*************/
	public boolean isClass() { return true; }

	public Type findInDataMembers(String name) {
		for (TypeClass ct = this; ct != null; ct = ct.father) {
            for (TypeClassVarDecList cd = ct.dataMembers; cd != null; cd = cd.tail) {
				if (name.equals(cd.head.name)) {
					return cd.head.t;
                }
            }
        }
		return null;
	}

	public boolean isAncestor(TypeClass father) {
		for (TypeClass ct = this; ct != null; ct = ct.father) {
			if (father.equals(ct)) {
				return true;
			}
        }
		return false;
	}
}
