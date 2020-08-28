package com.github.haivan.sqlbuilder.clauses.column;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class Column extends AbstractColumn
{

	protected String name;

	public Column()
	{
	}

	public Column(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public AbstractClause clone()
	{
		Column newColumn = new Column();
		newColumn.name = name;
		newColumn.component = component;
		newColumn.dialect = dialect;
		return newColumn;
	}
}
