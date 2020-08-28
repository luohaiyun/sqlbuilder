package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class BooleanCondition extends AbstractCondition
{

	private String column;

	private boolean value;

	public BooleanCondition(){

	}

	public BooleanCondition(String column, boolean value, boolean isNot, boolean isOr)
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

	public boolean isValue()
	{
		return value;
	}

	public void setValue(boolean value)
	{
		this.value = value;
	}

	@Override
	public AbstractClause clone()
	{
		BooleanCondition newBooleanCondition = new BooleanCondition();
		newBooleanCondition.column = column;
		newBooleanCondition.value = value;
		newBooleanCondition.dialect = dialect;
		newBooleanCondition.column = column;
		newBooleanCondition.isNot = isNot;
		newBooleanCondition.isOr = isOr;
		newBooleanCondition.component = component;
		return newBooleanCondition;
	}
}
