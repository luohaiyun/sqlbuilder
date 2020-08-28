package com.github.haivan.sqlbuilder.clauses.combine;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class Combine<T extends Query<T>> extends AbstractCombine
{
	private T query;

	private String operation;

	private boolean all;

	public T getQuery()
	{
		return query;
	}

	public void setQuery(T query)
	{
		this.query = query;
	}

	public String getOperation()
	{
		return operation;
	}

	public void setOperation(String operation)
	{
		this.operation = operation;
	}

	public boolean isAll()
	{
		return all;
	}

	public void setAll(boolean all)
	{
		this.all = all;
	}

	@Override
	public AbstractClause clone()
	{
		Combine<T> clone = new Combine<>();
		clone.query = query;
		clone.operation = operation;
		clone.all = all;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
