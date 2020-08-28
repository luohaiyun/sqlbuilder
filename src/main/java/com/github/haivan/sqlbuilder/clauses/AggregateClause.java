package com.github.haivan.sqlbuilder.clauses;

import java.util.List;

public class AggregateClause extends AbstractClause
{
	protected List<String> columns;

	private String type;

	public AggregateClause()
	{
	}

	public AggregateClause(List<String> columns, String type)
	{
		this.columns = columns;
		this.type = type;
	}

	public List<String> getColumns()
	{
		return columns;
	}

	public void setColumns(List<String> columns)
	{
		this.columns = columns;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public AbstractClause clone()
	{
		AggregateClause newAggregateClause = new AggregateClause();
		newAggregateClause.columns = columns;
		newAggregateClause.type = type;
		newAggregateClause.component = component;
		newAggregateClause.dialect = dialect;
		return newAggregateClause;
	}
}
