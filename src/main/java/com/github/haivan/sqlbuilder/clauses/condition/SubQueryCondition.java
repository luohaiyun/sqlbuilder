package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class SubQueryCondition<T extends Query<T>> extends AbstractCondition
{
	private Object value;

	private String operator;

	private T query;

	public SubQueryCondition()
	{
	}

	public SubQueryCondition(Object value, String operator, T query, boolean isNot, boolean isOr)
	{
		this.value = value;
		this.operator = operator;
		this.query = query;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public T getQuery()
	{
		return query;
	}

	public void setQuery(T query)
	{
		this.query = query;
	}

	@Override
	public AbstractClause clone()
	{
		SubQueryCondition<T> clone = new SubQueryCondition<>();
		clone.operator = operator;
		clone.query = query;
		clone.value = value;
		clone.isNot = isNot;
		clone.isOr = isOr;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
