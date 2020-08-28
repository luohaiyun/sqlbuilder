package com.github.haivan.sqlbuilder;

public class UnsafeLiteral
{
	private String value;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public UnsafeLiteral(String value)
	{
		this(value, true);
	}

	public UnsafeLiteral(String value, boolean replaceQuotes)
	{
		if(value == null)
		{
			value = "";
		}
		if(replaceQuotes)
		{
			value = value.replace("'", "''");
		}

		this.value = value;
	}

}
