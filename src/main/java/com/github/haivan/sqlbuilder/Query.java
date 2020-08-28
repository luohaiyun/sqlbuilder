package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.clauses.AggregateClause;
import com.github.haivan.sqlbuilder.clauses.LimitClause;
import com.github.haivan.sqlbuilder.clauses.OffsetClause;
import com.github.haivan.sqlbuilder.clauses.column.Column;
import com.github.haivan.sqlbuilder.clauses.column.QueryColumn;
import com.github.haivan.sqlbuilder.clauses.column.RawColumn;
import com.github.haivan.sqlbuilder.clauses.condition.BasicCondition;
import com.github.haivan.sqlbuilder.clauses.condition.NullCondition;
import com.github.haivan.sqlbuilder.clauses.from.QueryFromClause;
import com.github.haivan.sqlbuilder.clauses.from.RawFromClause;
import com.github.haivan.sqlbuilder.clauses.insert.InsertClause;
import com.github.haivan.sqlbuilder.clauses.join.BaseJoin;
import com.github.haivan.sqlbuilder.clauses.order.OrderBy;
import com.github.haivan.sqlbuilder.clauses.order.RawOrderBy;
import com.github.haivan.sqlbuilder.utils.ArrayUtils;
import com.github.haivan.sqlbuilder.utils.StringUtils;

import java.util.*;
import java.util.function.Function;

public class Query<T extends Query<T>> extends BaseQuery<T>
{
	protected String queryAlias;

	protected boolean isDistinct;

	protected String method = "select";

	protected String queryComment = "";

	protected List<Include> includes = new ArrayList<>();

	protected Map<String, Object> variables = new HashMap<>();

	public Query()
	{

	}

	public Query(String table)
	{
		this(table, null);
	}

	public Query(String table, String comment)
	{
		from(table);
		comment(comment);
	}

	public String getMethod()
	{
		return method;
	}

	public boolean isDistinct()
	{
		return isDistinct;
	}

	public String getQueryAlias()
	{
		return queryAlias;
	}

	public void setQueryAlias(String queryAlias)
	{
		this.queryAlias = queryAlias;
	}

	public boolean hasOffset()
	{
		return hasOffset(null);
	}

	public boolean hasOffset(String dialect)
	{
		return getOffset(dialect) > 0;
	}

	public boolean hasLimit()
	{
		return hasLimit(null);
	}

	public boolean hasLimit(String dialect)
	{
		return getLimit(dialect) > 0;
	}

	public int getLimit(String dialect)
	{
		if(dialect == null)
		{
			dialect = this.dialect;
		}

		LimitClause limitClause = this.getOneComponent("limit", dialect);

		return limitClause != null ? limitClause.getLimit() : 0;
	}

	public int getOffset(String dialect)
	{
		if(dialect == null)
		{
			dialect = this.dialect;
		}

		OffsetClause offsetClause = this.getOneComponent("offset", dialect);

		return offsetClause != null ? offsetClause.getOffset() : 0;
	}


	public Query comment(String comment)
	{
		queryComment = comment;
		return this;
	}

	@Override
	public T newQuery()
	{
		return (T)new Query();
	}

	public T as(String alias)
	{
		queryAlias = alias;
		return (T)this;
	}

	@Override
	public T clone()
	{
		T clone = super.clone();
		clone.parent = parent;
		clone.queryAlias = queryAlias;
		clone.isDistinct = isDistinct;
		clone.method = method;
		clone.includes = includes;
		clone.variables = variables;
		return clone;
	}

	public T forDialect(String dialect, Function<T, T> fn)
	{
		this.dialect = dialect;

		T result = fn.apply((T)this);

		this.dialect = null;

		return result;
	}

	public T with(Query query)
	{

		if(StringUtils.isBlank(query.queryAlias))
		{
			throw new RuntimeException("No Alias found for the CTE query");
		}
		query = query.clone();

		String alias = query.queryAlias.trim();

		query.queryAlias = null;

		return addComponent("cte", new QueryFromClause(query, alias));
	}

	public T with(Function<T, T> fn)
	{
		return with(fn.apply(newQuery()));
	}

	public T with(String alias, T query)
	{
		return with(query.as(alias));
	}

	public T with(String alias, Function<T, T> fn)
	{
		return with(alias, fn.apply(newQuery()));
	}

	public T withRaw(String alias, String sql, Object... bindings)
	{
		return addComponent("cte", new RawFromClause(alias, sql, bindings));
	}

	public T limit(int value)
	{
		return addOrReplaceComponent("limit", new LimitClause(value));
	}

	public T offset(int value)
	{
		return addOrReplaceComponent("offset", new OffsetClause(value));
	}

	public T take(int value)
	{
		return limit(value);
	}

	public T skip(int value)
	{
		return offset(value);
	}


	public T distinct()
	{
		this.isDistinct = true;
		return (T)this;
	}

	public T when(boolean condition, Function<T, T> whenTrue)
	{
		return when(condition, whenTrue, null);
	}

	public T when(boolean condition, Function<T, T> whenTrue, Function<T, T> whenFalse)
	{
		if(condition && whenTrue != null)
		{
			return whenTrue.apply((T)this);
		}

		if(!condition && whenFalse != null)
		{
			return whenFalse.apply((T)this);
		}

		return (T)this;
	}

	public T whenNot(boolean condition, Function<T, T> callback)
	{
		if(!condition)
		{
			return callback.apply((T)this);
		}

		return (T)this;
	}

	//region order

	public T orderBy(String... columns)
	{
		for(String column : columns)
		{
			addComponent("order", new OrderBy(column, true));
		}

		return (T)this;
	}

	public T orderByDesc(String... columns)
	{
		for(String column : columns)
		{
			addComponent("order", new OrderBy(column, false));
		}

		return (T)this;
	}

	public T orderByRaw(String expression, Object... bindings)
	{
		List<Object> arrays = ArrayUtils.flatten(bindings);
		return addComponent("order", new RawOrderBy(expression, arrays.toArray()));
	}

	//endregion

	//region group

	public T groupBy(String... columns)
	{

		for(String column : columns)
		{
			addComponent("group", new Column(column));
		}
		return (T)this;
	}

	public T groupByRaw(String expression, Object... bindings)
	{
		return addComponent("group", new RawColumn(expression, bindings));
	}

	//endregion

	//region delete

	public T asDelete()
	{
		method = "delete";
		return (T)this;
	}

	//endregion

	//region update

	public T asUpdate(List<String> columns, List<Object> values)
	{

		if(columns == null || columns.size() == 0 || values == null || values.size() == 0)
		{
			throw new IllegalArgumentException("Columns and Values cannot be null or empty");
		}

		if(columns.size() != values.size())
		{
			throw new IllegalArgumentException("Columns count should be equal to Values count");
		}

		method = "update";

		clearComponent("update").addComponent("update", new InsertClause(columns, values));

		return (T)this;
	}

	public T asUpdate(Map<String, Object> data)
	{

		if(data == null || data.size() == 0)
		{
			throw new IllegalArgumentException("Values dictionary cannot be null or empty");
		}

		method = "update";

		List<String> columns = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		for(Map.Entry<String, Object> entry : data.entrySet())
		{
			columns.add(entry.getKey());
			values.add(entry.getValue());
		}

		return asUpdate(columns, values);
	}


	//endregion

	//region aggregate

	public T asAggregate(String type, String... columns)
	{

		method = "aggregate";

		this.clearComponent("aggregate")
				.addComponent("aggregate"
						, new AggregateClause(columns == null
								? new ArrayList<>() : Arrays.asList(columns), type));

		return (T)this;
	}


	public T asCount(String... columns)
	{
		List<String> cols;

		if(columns.length < 1)
		{
			cols = new ArrayList<>();
			cols.add("*");
		}
		else
		{
			cols = Arrays.asList(columns);
		}

		return asAggregate("count", cols.toArray(new String[0]));
	}

	public T asAvg(String column)
	{
		return asAggregate("avg", column);
	}

	public T asAverage(String column)
	{
		return asAvg(column);
	}

	public T asSum(String column)
	{
		return asAggregate("sum", column);
	}

	public T asMax(String column)
	{
		return asAggregate("max", column);
	}

	public T asMin(String column)
	{
		return asAggregate("min", column);
	}

	//endregion


	//region having

	public T having(String column, String op, Object value)
	{
		if(value == null)
		{
			return not(!op.equals("=")).havingNull(column);
		}

		return addComponent("having", new BasicCondition(column, op, value, getNot(), getOr()));
	}

	public T havingNull(String column)
	{
		return addComponent("having", new NullCondition(column, getNot(), getOr()));
	}

	public T having(String column, Object value)
	{
		return having(column, "=", value);
	}

	public T having(Map<String, Object> values)
	{
		T query = (T)this;
		boolean orFlag = getOr();
		boolean notFlag = getNot();

		for(Map.Entry<String, Object> entry : values.entrySet())
		{
			if(orFlag)
			{
				query.or();
			}
			else
			{
				query.and();
			}
			query = not(notFlag).having(entry.getKey(), entry.getValue());
		}

		return query;
	}


	//endregion

	//region select

	public T select(String[] columns)
	{
		method = "select";

		columns = Arrays.stream(columns).map(StringUtils::expandExpression)
				.flatMap(List::stream).toArray(String[]::new);

		for(String column : columns)
		{
			addComponent("select", new Column(column));
		}

		return (T)this;
	}

	public T selectRaw(String sql, Object... bindings)
	{

		method = "select";

		addComponent("select", new RawColumn(sql, bindings));

		return (T)this;
	}

	public T select(Query query, String alias)
	{
		method = "select";

		query = query.clone();

		addComponent("select", new QueryColumn(query.as(alias)));

		return (T)this;
	}

	public T select(Function<Query, Query> callback, String alias)
	{
		return select(callback.apply(newChild()), alias);
	}

	//endregion

	//region join

	public T join(Function<Join, Join> callback)
	{
		Join join = callback.apply(new Join().asInner());

		return addComponent("join", new BaseJoin(join));
	}

	public T join(String table, String first, String second, String op, String type)
	{
		return join(j -> j.joinWith(table).whereColumns(first, op, second).asType(type));
	}

	public T join(String table, Function<Join, Join> callback, String type)
	{
		return join(j -> j.joinWith(table).where(callback).asType(type));
	}

	public T join(Query query, Function<Join, Join> onCallback, String type)
	{
		return join(j -> j.joinWith(query).where(onCallback).asType(type));
	}

	public T leftJoin(String table, String first, String second, String op)
	{
		return join(table, first, second, op, "left join");
	}

	public T rightJoin(String table, String first, String second, String op)
	{
		return join(table, first, second, op, "right join");
	}

	public T innerJoin(String table, String first, String second, String op)
	{
		return join(table, first, second, op, "inner join");
	}


	//endregion

	//region other
	public T define(String variable, Object value)
	{
		variables.put(variable, value);
		return (T)this;
	}

	public Object findVariable(String variable)
	{
		boolean found = variables.containsKey(variable);

		if(found)
		{
			return variables.get(variable);
		}
		if(parent != null)
		{
			return ((Query)parent).findVariable(variable);
		}
		throw new IllegalArgumentException("variable " + variable + " not found");
	}
	//endregion
}
