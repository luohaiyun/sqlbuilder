package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class AbstractCondition extends AbstractClause
{
	protected boolean isOr;

	protected boolean isNot;

	public boolean isOr()
	{
		return isOr;
	}

	public void setOr(boolean or)
	{
		isOr = or;
	}

	public boolean isNot()
	{
		return isNot;
	}

	public void setNot(boolean not)
	{
		isNot = not;
	}
}
