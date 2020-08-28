package com.github.haivan.sqlbuilder.clauses;

public abstract class AbstractClause implements Cloneable
{
	protected String dialect;

	protected String component;

	public String getDialect()
	{
		return dialect;
	}

	public void setDialect(String dialect)
	{
		this.dialect = dialect;
	}

	public String getComponent()
	{
		return component;
	}

	public void setComponent(String component)
	{
		this.component = component;
	}

	@Override
	public AbstractClause clone()
	{
		try
		{
			return (AbstractClause)super.clone();
		}
		catch(CloneNotSupportedException e)
		{
		}
		return null;
	}
}
