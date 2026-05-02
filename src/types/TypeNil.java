package types;

public class TypeNil extends Type
{
	/******************************/
	/* SINGLETON INSTANCE PATTERN */
	/******************************/
	private static TypeNil instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	private TypeNil() { this.name = "nil"; }

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static TypeNil getInstance()
	{
		if (instance == null)
		{
			instance = new TypeNil();
		}
		return instance;
	}
}
