package com.github.haivan.sqlbuilder.clauses;

public class LimitClause extends AbstractClause
{

	protected int limit;

	public LimitClause()
	{

	}

	public LimitClause(int limit)
	{
		this.limit = limit;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit > 0 ? limit : this.limit;
	}

	public boolean hasLimit()
	{
		return limit > 0;
	}

	public LimitClause clear()
	{
		limit = 0;
		return this;
	}

	@Override
	public AbstractClause clone()
	{
		LimitClause newLimitClause = new LimitClause();
		newLimitClause.setLimit(limit);
		newLimitClause.setComponent(component);
		newLimitClause.setDialect(dialect);
		return newLimitClause;
	}
}
