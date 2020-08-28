package com.github.haivan.sqlbuilder.clauses.insert;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

import java.util.List;

public class InsertQueryClause<T extends Query<T>> extends AbstractInsertClause
{
	private List<String> columns;

	private T query;

	public List<String> getColumns()
	{
		return columns;
	}

	public void setColumns(List<String> columns)
	{
		this.columns = columns;
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
		InsertQueryClause<T> clone = new InsertQueryClause<>();
		clone.columns = columns;
		clone.query = query;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
