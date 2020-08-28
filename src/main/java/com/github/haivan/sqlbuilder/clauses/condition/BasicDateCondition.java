package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class BasicDateCondition extends BasicCondition
{
	protected String part;

	public BasicDateCondition()
	{
	}

	public BasicDateCondition(String column, String operator, Object value, boolean isNot, boolean isOr, String part)
	{
		super(column, operator, value, isNot, isOr);
		this.part = part;
	}

	public String getPart()
	{
		return part;
	}

	public void setPart(String part)
	{
		this.part = part;
	}

	@Override
	public AbstractClause clone()
	{
		BasicDateCondition newBasicDateCondition = new BasicDateCondition();
		newBasicDateCondition.dialect = dialect;
		newBasicDateCondition.column = column;
		newBasicDateCondition.isNot = isNot;
		newBasicDateCondition.isOr = isOr;
		newBasicDateCondition.component = component;
		newBasicDateCondition.operator = operator;
		newBasicDateCondition.value = value;
		newBasicDateCondition.part = part;
		return newBasicDateCondition;
	}
}
