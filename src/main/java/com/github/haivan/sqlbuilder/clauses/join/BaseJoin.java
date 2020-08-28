package com.github.haivan.sqlbuilder.clauses.join;

import com.github.haivan.sqlbuilder.Join;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class BaseJoin extends AbstractJoin
{

	private Join join;

	public BaseJoin()
	{
	}

	public BaseJoin(Join join)
	{
		this.join = join;
	}

	public Join getJoin()
	{
		return join;
	}

	public void setJoin(Join join)
	{
		this.join = join;
	}

	@Override
	public AbstractClause clone()
	{
		BaseJoin clone = new BaseJoin();
		clone.join = join;
		clone.component = component;
		clone.dialect = dialect;

		return clone;
	}
}
