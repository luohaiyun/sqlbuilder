package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class InCondition extends AbstractCondition
{

	private String column;

	private Object value;

	public InCondition()
	{
	}

	public InCondition(String column, Object value, boolean isNot, boolean isOr)
	{
		this.column = column;
		this.value = value;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public AbstractClause clone()
	{

		InCondition clone = new InCondition();

		clone.column = column;
		clone.value = value;
		clone.isNot = isNot;
		clone.isOr = isOr;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
