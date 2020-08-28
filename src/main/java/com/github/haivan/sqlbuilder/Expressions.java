package com.github.haivan.sqlbuilder;

public class Expressions
{

	public static Variable variable(String name)
	{
		return new Variable(name);
	}

	public static UnsafeLiteral unsafeLiteral(String value)
	{
		return unsafeLiteral(value, true);
	}

	public static UnsafeLiteral unsafeLiteral(String value, boolean replaceQuotes)
	{
		return new UnsafeLiteral(value, replaceQuotes);
	}
}
