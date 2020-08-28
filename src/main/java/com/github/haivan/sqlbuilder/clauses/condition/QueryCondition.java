package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.BaseQuery;

public class QueryCondition<T extends BaseQuery<T>> extends AbstractCondition
{
	public String column;
	public String operator;
	public Query query;

	public QueryCondition()
	{
	}

	public QueryCondition(String column, String operator, Query query, boolean isNot, boolean isOr)
	{
		this.column = column;
		this.operator = operator;
		this.query = query;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public String getOperator()
	{
		return operator;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public Query getQuery()
	{
		return query;
	}

	public void setQuery(Query query)
	{
		this.query = query;
	}

	@Override
	public AbstractClause clone()
	{
		QueryCondition<T> clone = new QueryCondition<>();

		clone.column = column;
		clone.query = query;
		clone.operator = operator;
		clone.isNot = isNot;
		clone.isOr = isOr;
		clone.component = component;
		clone.dialect = dialect;
		return clone;
	}
}

