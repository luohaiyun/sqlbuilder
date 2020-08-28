package com.github.haivan.sqlbuilder.clauses.from;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FromClause extends AbstractFrom
{
	private String table = StringUtils.EMPTY;


	public FromClause()
	{
	}

	public FromClause(String table)
	{
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	@Override
	public String getAlias()
	{
		if(table.toLowerCase().contains(" as "))
		{
			List<String> segments = Arrays.stream(table.split(" ")).filter(str -> !StringUtils.isBlank(str)).collect(Collectors.toList());

			return segments.get(2);
		}

		return table;
	}

	@Override
	public AbstractClause clone()
	{
		FromClause newClause = new FromClause();
		newClause.alias = alias;
		newClause.component = component;
		newClause.dialect = dialect;
		newClause.table = table;
		return newClause;
	}
}
