package com.github.haivan.sqlbuilder.clauses;

public class OffsetClause extends AbstractClause
{
	protected int offset;

	public OffsetClause()
	{

	}

	public OffsetClause(int offset)
	{
		this.offset = offset;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset > 0 ? offset : this.offset;
	}

	public boolean hasOffset()
	{
		return offset > 0;
	}

	public OffsetClause clear()
	{
		offset = 0;
		return this;
	}

	@Override
	public AbstractClause clone()
	{
		OffsetClause newOffsetClause = new OffsetClause();
		newOffsetClause.setOffset(offset);
		newOffsetClause.setComponent(component);
		newOffsetClause.setDialect(dialect);
		return newOffsetClause;
	}
}
