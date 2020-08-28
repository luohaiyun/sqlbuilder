package com.github.haivan.sqlbuilder.clauses.from;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class RawFromClause extends AbstractFrom
{
	private String expression;

	public Object[] bindings;

	public RawFromClause()
	{

	}

	public RawFromClause(String expression, Object[] bindings)
	{
		this(null, expression, bindings);
	}

	public RawFromClause(String alias, String expression, Object[] bindings)
	{
		this.alias = alias;
		this.expression = expression;
		this.bindings = bindings;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public Object[] getBindings()
	{
		return bindings;
	}

	public void setBindings(Object[] bindings)
	{
		this.bindings = bindings;
	}

	@Override
	public AbstractClause clone()
	{
		RawFromClause newRawFromClause = new RawFromClause();
		newRawFromClause.bindings = bindings;
		newRawFromClause.expression = expression;
		newRawFromClause.alias = alias;
		newRawFromClause.component = component;
		newRawFromClause.dialect = dialect;
		return newRawFromClause;
	}
}
