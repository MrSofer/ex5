package types;

public class TypeArray extends Type
{
	/****************************/
	/* Element type of the array */
	/****************************/
	public Type elementType;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeArray(String name, Type elementType)
	{
		this.name = name;
		this.elementType = elementType;
	}

	/*************/
	/* isArray() */
	/*************/
	public boolean isArray() { return true; }
}
