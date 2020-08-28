package com.github.haivan.sqlbuilder.clauses.order;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class OrderBy extends AbstractOrderBy
{
	private String column;

	private boolean ascending = true;

	public OrderBy()
	{

	}

	public OrderBy(String column, boolean ascending)
	{
		this.column = column;
		this.ascending = ascending;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public boolean isAscending()
	{
		return ascending;
	}

	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	@Override
	public AbstractClause clone()
	{
		OrderBy orderBy = new OrderBy();
		orderBy.component = component;
		orderBy.ascending = ascending;
		orderBy.dialect = dialect;
		orderBy.column = column;
		return orderBy;
	}
}
