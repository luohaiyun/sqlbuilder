package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class RawCondition extends AbstractCondition
{
	protected String expression;

	protected Object[] bindings;

	public RawCondition(){

	}

	public RawCondition(String expression, Object[] bindings){
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
		RawCondition newRawCondition = new RawCondition();
		newRawCondition.dialect = dialect;
		newRawCondition.isNot = isNot;
		newRawCondition.isOr = isOr;
		newRawCondition.component = component;
		newRawCondition.expression = expression;
		newRawCondition.bindings = bindings;
		return newRawCondition;
	}
}
