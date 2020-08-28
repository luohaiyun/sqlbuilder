package com.github.haivan.sqlbuilder.clauses.order;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class RawOrderBy extends AbstractOrderBy
{
	protected String expression;

	protected Object[] bindings;

	public RawOrderBy()
	{
	}

	public RawOrderBy(String expression, Object[] bindings)
	{
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
		RawOrderBy newRawOrderBy = new RawOrderBy();
		newRawOrderBy.bindings = bindings;
		newRawOrderBy.expression = expression;
		newRawOrderBy.component = component;
		newRawOrderBy.dialect = dialect;
		return newRawOrderBy;
	}
}
