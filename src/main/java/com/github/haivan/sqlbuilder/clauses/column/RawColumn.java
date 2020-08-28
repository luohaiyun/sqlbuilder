package com.github.haivan.sqlbuilder.clauses.column;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class RawColumn extends AbstractColumn
{
	private String expression;

	private Object[] bindings;

	public RawColumn()
	{
	}

	public RawColumn(String expression, Object[] bindings)
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
		RawColumn newRawColumn = new RawColumn();
		newRawColumn.expression = expression;
		newRawColumn.bindings = bindings;
		newRawColumn.component = component;
		newRawColumn.dialect = dialect;
		return newRawColumn;
	}
}
