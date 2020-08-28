package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class InQueryCondition<T extends Query<T>> extends AbstractCondition
{

	private T query;

	private String column;

	public InQueryCondition()
	{
	}

	public InQueryCondition(T query, String column, boolean isNot, boolean isOr)
	{
		this.query = query;
		this.column = column;
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

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	@Override
	public AbstractClause clone()
	{
		InQueryCondition<T> clone = new InQueryCondition<>();
		clone.column = column;
		clone.query = query;
		clone.isNot = isNot;
		clone.isOr = isOr;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}
