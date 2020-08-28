package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.BaseQuery;

public class NestedCondition<T extends BaseQuery<T>> extends AbstractCondition
{
	protected T query;

	public NestedCondition()
	{

	}

	public NestedCondition(T query, boolean isNot, boolean isOr)
	{
		this.query = query;
		this.isNot = isNot;
		this.isOr = isOr;
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
		NestedCondition<T> newNestedCondition = new NestedCondition<>();
		newNestedCondition.dialect = dialect;
		newNestedCondition.isNot = isNot;
		newNestedCondition.isOr = isOr;
		newNestedCondition.component = component;
		newNestedCondition.query = query.clone();
		return newNestedCondition;
	}
}
