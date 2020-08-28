package com.github.haivan.sqlbuilder.clauses.insert;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;

import java.util.List;

public class InsertClause extends AbstractInsertClause
{

	private List<String> columns;

	private List<Object> values;

	private boolean returnId = false;

	public InsertClause(List<String> columns, List<Object> values)
	{
		this.columns = columns;
		this.values = values;
	}

	public InsertClause()
	{
	}

	public List<String> getColumns()
	{
		return columns;
	}

	public void setColumns(List<String> columns)
	{
		this.columns = columns;
	}

	public List<Object> getValues()
	{
		return values;
	}

	public void setValues(List<Object> values)
	{
		this.values = values;
	}

	public boolean isReturnId()
	{
		return returnId;
	}

	public void setReturnId(boolean returnId)
	{
		this.returnId = returnId;
	}

	@Override
	public AbstractClause clone()
	{
		InsertClause clone = new InsertClause();
		clone.columns = columns;
		clone.values = values;
		clone.returnId = returnId;
		clone.component = component;
		clone.dialect = dialect;

		return clone;
	}
}
