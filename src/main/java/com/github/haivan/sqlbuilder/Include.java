package com.github.haivan.sqlbuilder;

public class Include
{
	public String name;

	public Query query;

	public String foreignKey;

	public String localKey;

	public boolean isMany;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Query getQuery()
	{
		return query;
	}

	public void setQuery(Query query)
	{
		this.query = query;
	}

	public String getForeignKey()
	{
		return foreignKey;
	}

	public void setForeignKey(String foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	public String getLocalKey()
	{
		return localKey;
	}

	public void setLocalKey(String localKey)
	{
		this.localKey = localKey;
	}

	public boolean isMany()
	{
		return isMany;
	}

	public void setMany(boolean many)
	{
		isMany = many;
	}
}
