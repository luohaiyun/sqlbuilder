package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class NullCondition extends AbstractCondition
{
	protected String column;

	public NullCondition()
	{
	}

	public NullCondition(String column, boolean isNot, boolean isOr)
	{
		this.column = column;
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

	@Override
	public AbstractClause clone()
	{
		NullCondition newNullCondition = new NullCondition();
		newNullCondition.column = column;
		newNullCondition.dialect = dialect;
		newNullCondition.isNot = isNot;
		newNullCondition.isOr = isOr;
		newNullCondition.component = component;
		return newNullCondition;
	}
}
