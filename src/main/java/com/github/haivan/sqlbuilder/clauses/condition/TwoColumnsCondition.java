package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class TwoColumnsCondition extends AbstractCondition
{

	protected String first;

	protected String operator;

	protected String second;

	public TwoColumnsCondition()
	{
	}

	public TwoColumnsCondition(String first, String operator, String second, boolean isNot, boolean isOr)
	{
		this.first = first;
		this.operator = operator;
		this.second = second;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public String getFirst()
	{
		return first;
	}

	public void setFirst(String first)
	{
		this.first = first;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public String getSecond()
	{
		return second;
	}

	public void setSecond(String second)
	{
		this.second = second;
	}

	@Override
	public AbstractClause clone()
	{
		TwoColumnsCondition newTwoColumnsCondition = new TwoColumnsCondition();
		newTwoColumnsCondition.dialect = dialect;
		newTwoColumnsCondition.first = first;
		newTwoColumnsCondition.isNot = isNot;
		newTwoColumnsCondition.isOr = isOr;
		newTwoColumnsCondition.component = component;
		newTwoColumnsCondition.operator = operator;
		newTwoColumnsCondition.second = second;
		return newTwoColumnsCondition;
	}
}
