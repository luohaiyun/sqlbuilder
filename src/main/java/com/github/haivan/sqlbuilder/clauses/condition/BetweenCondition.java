package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class BetweenCondition<T> extends AbstractCondition
{

	protected String column;

	protected T higher;

	protected T lower;

	public BetweenCondition()
	{

	}

	public BetweenCondition(String column, boolean isNot, boolean isOr, T lower, T higher)
	{
		this.column = column;
		this.isNot = isNot;
		this.isOr = isOr;
		this.lower = lower;
		this.higher = higher;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public T getHigher()
	{
		return higher;
	}

	public void setHigher(T higher)
	{
		this.higher = higher;
	}

	public T getLower()
	{
		return lower;
	}

	public void setLower(T lower)
	{
		this.lower = lower;
	}

	@Override
	public AbstractClause clone()
	{
		BetweenCondition<T> newBetweenCondition = new BetweenCondition<>();
		newBetweenCondition.column = column;
		newBetweenCondition.higher = higher;
		newBetweenCondition.lower = lower;
		newBetweenCondition.isNot = isNot;
		newBetweenCondition.isOr = isOr;
		newBetweenCondition.dialect = dialect;
		newBetweenCondition.component = component;
		return newBetweenCondition;
	}
}
