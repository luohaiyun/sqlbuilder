package com.github.haivan.sqlbuilder.clauses.combine;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class RawCombine extends AbstractCombine
{
	private String expression;

	public Object[] bindings;

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
		RawCombine clone = new RawCombine();
		clone.expression = expression;
		clone.bindings = bindings;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
