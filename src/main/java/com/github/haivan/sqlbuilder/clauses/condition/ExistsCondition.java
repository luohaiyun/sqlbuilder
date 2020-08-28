package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class ExistsCondition<T extends Query<T>> extends AbstractCondition
{
	private T query;

	public ExistsCondition(T query, boolean isNot, boolean isOr)
	{
		this.query = query;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public ExistsCondition()
	{
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
		ExistsCondition<T> clone = new ExistsCondition<>();

		clone.query = query;
		clone.isNot = isNot;
		clone.isOr = isOr;
		clone.component = component;
		clone.dialect = dialect;

		return clone;
	}
}
