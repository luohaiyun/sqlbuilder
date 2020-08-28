package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class BasicCondition extends AbstractCondition
{
	protected String column;

	protected String operator;

	protected Object value;

	public BasicCondition(){

	}

	public BasicCondition(String column, String operator, Object value, boolean isNot, boolean isOr){
		this.column = column;
		this.operator = operator;
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

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
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
		BasicCondition newBaseCondition = new BasicCondition();
		newBaseCondition.isNot = isNot;
		newBaseCondition.isOr = isOr;
		newBaseCondition.column = column;
		newBaseCondition.value = value;
		newBaseCondition.operator = operator;
		newBaseCondition.component = component;
		newBaseCondition.dialect = dialect;
		return newBaseCondition;
	}
}
