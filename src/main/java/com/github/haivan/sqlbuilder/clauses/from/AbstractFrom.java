package com.github.haivan.sqlbuilder.clauses.from;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public abstract class AbstractFrom extends AbstractClause
{
	protected String alias;

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}
}
