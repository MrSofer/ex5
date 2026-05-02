package ir;

import java.util.*;

public class FuncParamTable
{
	private final Map<String, List<String>> table = new HashMap<>();

	public void register(String funcName, List<String> paramLabels)
	{
		table.put(funcName, new ArrayList<>(paramLabels));
	}

	public List<String> getParams(String funcName)
	{
		return table.getOrDefault(funcName, Collections.emptyList());
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static FuncParamTable instance = null;

	protected FuncParamTable() {}

	public static FuncParamTable getInstance()
	{
		if (instance == null)
		{
			instance = new FuncParamTable();
		}
		return instance;
	}
}
