package com.github.haivan.sqlbuilder.clauses.from;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.utils.StringUtils;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;

public class QueryFromClause<T extends Query<T>> extends AbstractFrom
{
	private T query;

	public QueryFromClause()
	{
	}

	public QueryFromClause(T query)
	{
		this(query, null);
	}

	public QueryFromClause(T query, String alias)
	{
		this.query = query;
		this.alias = alias;
	}

	@Override
	public String getAlias()
	{
		return StringUtils.isBlank(super.getAlias()) ? query.getQueryAlias() : super.getAlias();
	}

	public T getQuery()
	{
		return query;
	}

	public void setQuery(T query)
	{
		this.query = query;
	}

	@Override
	public AbstractClause clone()
	{
		QueryFromClause<T> newQueryFromClause = new QueryFromClause<>();
		newQueryFromClause.query = query;
		newQueryFromClause.alias = alias;
		newQueryFromClause.component = component;
		newQueryFromClause.dialect = dialect;
		return newQueryFromClause;
	}
}
