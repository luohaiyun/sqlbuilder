package com.github.haivan.sqlbuilder.clauses.column;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class QueryColumn<T extends Query<T>> extends AbstractColumn
{
	private T query;

	public QueryColumn()
	{
	}

	public QueryColumn(T query)
	{
		this.query = query;
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
		QueryColumn<T> clone = new QueryColumn<>();
		clone.query = query;
		clone.component = component;
		clone.dialect = dialect;

		return clone;
	}
}
