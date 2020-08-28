package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.*;
import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.clauses.AggregateClause;
import com.github.haivan.sqlbuilder.clauses.column.Column;
import com.github.haivan.sqlbuilder.clauses.column.QueryColumn;
import com.github.haivan.sqlbuilder.clauses.column.RawColumn;
import com.github.haivan.sqlbuilder.clauses.combine.RawCombine;
import com.github.haivan.sqlbuilder.clauses.condition.*;
import com.github.haivan.sqlbuilder.clauses.from.AbstractFrom;
import com.github.haivan.sqlbuilder.clauses.from.FromClause;
import com.github.haivan.sqlbuilder.clauses.from.QueryFromClause;
import com.github.haivan.sqlbuilder.clauses.from.RawFromClause;
import com.github.haivan.sqlbuilder.clauses.insert.AbstractInsertClause;
import com.github.haivan.sqlbuilder.clauses.insert.InsertClause;
import com.github.haivan.sqlbuilder.clauses.insert.InsertQueryClause;
import com.github.haivan.sqlbuilder.clauses.order.OrderBy;
import com.github.haivan.sqlbuilder.clauses.order.RawOrderBy;
import com.github.haivan.sqlbuilder.utils.ArrayUtils;
import com.github.haivan.sqlbuilder.utils.StringUtils;
import com.github.haivan.sqlbuilder.clauses.column.AbstractColumn;
import com.github.haivan.sqlbuilder.clauses.combine.AbstractCombine;
import com.github.haivan.sqlbuilder.clauses.combine.Combine;
import com.github.haivan.sqlbuilder.clauses.join.BaseJoin;

import java.util.*;
import java.util.stream.Collectors;

public class Compiler
{
	protected String parameterPlaceholder = "?";
	protected String parameterPrefix = "@p";
	protected String openingIdentifier = "\"";
	protected String closingIdentifier = "\"";
	protected String columnAsKeyword = "AS ";
	protected String tableAsKeyword = "AS ";
	protected String lastId = "";
	protected String escapeCharacter = "\\";
	protected String dialect;


	protected Compiler()
	{

	}

	protected final HashSet<String> operators = new HashSet<>(Arrays.asList(
			"=", "<", ">", "<=", ">=", "<>", "!=", "<=>",
			"like", "not like",
			"ilike", "not ilike",
			"like binary", "not like binary",
			"rlike", "not rlike",
			"regexp", "not regexp",
			"similar to", "not similar to"));
	protected HashSet<String> userOperators = new HashSet<String>()
	{

	};

	protected Map<String, Object> generateNamedBindings(Object[] bindings)
	{
		Map<String, Object> result = new HashMap<>();

		List<Object> flattened = ArrayUtils.flatten(bindings);

		for(int i = 0; i < flattened.size(); i++)
		{
			result.put(parameterPrefix + i, flattened.get(i));
		}
		return result;
	}

	protected <T extends Query<T>> SqlResult<T> prepareResult(SqlResult<T> ctx)
	{
		ctx.setNamedBindings(generateNamedBindings(ctx.getBindings().toArray()));
		ctx.setSql(StringUtils.replaceAll(ctx.getRawSql(), parameterPlaceholder, i -> parameterPrefix + i));
		return ctx;
	}

	private <T extends Query<T>> T transformAggregateQuery(T query)
	{

		AggregateClause clause = query.getOneComponent("aggregate", dialect);

		if(clause.getColumns().size() == 1 && !query.isDistinct())
		{
			return query;
		}

		if(query.isDistinct())
		{
			query.clearComponent("aggregate", dialect);
			query.clearComponent("select", dialect);
			query.select(clause.getColumns().toArray(new String[0]));
		}
		else
		{
			for(String column : clause.getColumns())
			{
				query.whereNotNull(column);
			}
		}

		AggregateClause outerClause = new AggregateClause(new ArrayList<String>()
		{{
			add("*");
		}}, clause.getType());

		return query.newQuery()
				.addComponent("aggregate", outerClause)
				.from(query, clause.getType() + "Query");
	}

	public <T extends Query<T>> SqlResult<T> compile(T query)
	{
		SqlResult<T> ctx = compileRaw(query);

		ctx = prepareResult(ctx);

		return ctx;
	}


	protected <T extends Query<T>> SqlResult<T> compileRaw(T query)
	{
		SqlResult<T> ctx;

		if(query.getMethod().equals("insert"))
		{
			ctx = compileInsertQuery(query);
		}
		else if(query.getMethod().equals("update"))
		{
			ctx = compileUpdateQuery(query);
		}
		else if(query.getMethod().equals("delete"))
		{
			ctx = compileDeleteQuery(query);
		}
		else
		{
			if(query.getMethod().equals("aggregate"))
			{
				query.clearComponent("limit")
						.clearComponent("order")
						.clearComponent("group");

				query = transformAggregateQuery(query);
			}

			ctx = compileSelectQuery(query);
		}

		// handle CTEs
		if(query.hasComponent("cte", dialect))
		{
			ctx = compileCteQuery(ctx, query);
		}

		ctx.setRawSql(StringUtils.expandParameters(ctx.getRawSql(), "?", ctx.getBindings().toArray()));

		return ctx;
	}

	protected <T extends Query<T>> SqlResult<T> compileCteQuery(SqlResult<T> ctx, T query)
	{
		CteFinder cteFinder = new CteFinder(query, dialect);
		List<AbstractFrom> cteSearchResult = cteFinder.Find();

		StringBuilder rawSql = new StringBuilder("WITH ");
		List<Object> cteBindings = new ArrayList<>();

		for(AbstractFrom cte : cteSearchResult)
		{
			SqlResult<T> cteCtx = compileCte(cte);

			cteBindings.addAll(cteCtx.getBindings());
			rawSql.append(cteCtx.getRawSql().trim());
			rawSql.append(",\n");
		}

		rawSql.substring(0, rawSql.length() - 2); // remove last comma
		rawSql.append('\n');
		rawSql.append(ctx.getRawSql());

		ctx.getBindings().addAll(0, cteBindings);
		ctx.setRawSql(rawSql.toString());

		return ctx;
	}

	public <T extends Query<T>> SqlResult<T> compileCte(AbstractFrom cte)
	{
		SqlResult<T> ctx = new SqlResult<>();

		if(null == cte)
		{
			return ctx;
		}

		if(cte instanceof RawFromClause)
		{
			RawFromClause raw = (RawFromClause)cte;

			ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
			ctx.setRawSql(wrapValue(raw.getAlias()) + " AS (" + wrapIdentifiers(raw.getExpression() + ")"));
		}
		else if(cte instanceof QueryFromClause)
		{
			QueryFromClause<T> queryFromClause = (QueryFromClause)cte;

			SqlResult<T> subCtx = compileSelectQuery(queryFromClause.getQuery());
			ctx.getBindings().addAll(subCtx.getBindings());

			ctx.setRawSql(wrapValue(queryFromClause.getAlias()) + " AS (" + subCtx.getRawSql() + ")");
		}

		return ctx;
	}


	protected <T extends Query<T>> SqlResult<T> compileInsertQuery(T query)
	{

		SqlResult<T> ctx = new SqlResult<>(query);

		if(!ctx.getQuery().hasComponent("from", dialect))
		{
			throw new IllegalArgumentException("No table set to insert");
		}

		AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", dialect);

		if(fromClause == null)
		{
			throw new IllegalArgumentException("Invalid table expression");
		}

		String table = null;

		if(fromClause instanceof FromClause)
		{
			table = wrap(((FromClause)fromClause).getTable());
		}

		if(fromClause instanceof RawFromClause)
		{
			RawFromClause rawFromClause = (RawFromClause)fromClause;
			table = wrapIdentifiers(rawFromClause.getExpression());
			ctx.getBindings().addAll(Arrays.asList(rawFromClause.bindings));
		}

		if(table == null)
		{
			throw new IllegalArgumentException("Invalid table expression");
		}

		List<AbstractInsertClause> inserts = ctx.getQuery().getComponents("insert", dialect);

		if(inserts.get(0) instanceof InsertClause)
		{

			InsertClause insertClause = (InsertClause)inserts.get(0);

			String columns = String.join(", ", wrapArray(insertClause.getColumns()));
			String values = String.join(", ", parameterize(ctx, insertClause.getValues()));

			ctx.setRawSql("INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")");
			if(insertClause.isReturnId() && !StringUtils.isBlank(lastId))
			{
				ctx.setRawSql(ctx.getRawSql() + ";" + lastId);
			}
		}
		else
		{
			InsertQueryClause<T> clause = (InsertQueryClause)inserts.get(0);

			String columns = "";

			if(!clause.getColumns().isEmpty())
			{
				columns = " (" + String.join("", wrapArray(clause.getColumns())) + ") ";
			}

			SqlResult<T> subCtx = compileSelectQuery(clause.getQuery());
			ctx.getBindings().addAll(subCtx.getBindings());

			ctx.setRawSql("INSERT INTO " + table + columns + subCtx.getRawSql());
		}

		return ctx;
	}

	protected <T extends Query<T>> SqlResult<T> compileSelectQuery(T query)
	{
		SqlResult<T> ctx = new SqlResult<>(query.clone());

		List<String> results = Arrays.stream(new String[]{
				this.compileColumns(ctx),
				this.compileFrom(ctx),
				this.compileJoins(ctx),
				this.compileWheres(ctx),
				this.compileGroups(ctx),
				this.compileHaving(ctx),
				this.compileOrders(ctx),
				this.compileLimit(ctx),
				this.compileUnion(ctx),
		}).filter(x -> !StringUtils.isBlank(x)).collect(Collectors.toList());

		String sql = String.join(" ", results);

		ctx.setRawSql(sql);

		return ctx;
	}

	protected <T extends Query<T>> SqlResult<T> compileUpdateQuery(Query<T> query)
	{

		SqlResult<T> ctx = new SqlResult<>(query.clone());

		if(!ctx.getQuery().hasComponent("from", dialect))
		{
			throw new IllegalArgumentException("No table set to update");
		}

		AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", dialect);

		String table = null;

		if(fromClause instanceof FromClause)
		{
			FromClause fromClauseCast = (FromClause)fromClause;

			table = wrap(fromClauseCast.getTable());
		}

		if(fromClause instanceof RawFromClause)
		{
			RawFromClause rawFromClause = (RawFromClause)fromClause;
			table = wrapIdentifiers(rawFromClause.getExpression());
			ctx.getBindings().addAll(Arrays.asList(rawFromClause.getBindings()));
		}

		if(table == null)
		{
			throw new IllegalArgumentException("Invalid table expression");
		}

		InsertClause toUpdate = ctx.getQuery().getOneComponent("update", dialect);

		List<String> parts = new ArrayList<>();

		for(int i = 0; i < toUpdate.getColumns().size(); i++)
		{
			parts.add(wrap(toUpdate.getColumns().get(i)) + " = " + parameter(ctx, toUpdate.getValues().get(i)));
		}

		String where = compileWheres(ctx);

		if(!StringUtils.isBlank(where))
		{
			where = " " + where;
		}

		String sets = String.join(", ", parts);

		ctx.setRawSql("UPDATE " + table + " SET " + sets + where);

		return ctx;
	}

	protected <T extends Query<T>> SqlResult<T> compileDeleteQuery(T query)
	{
		SqlResult<T> ctx = new SqlResult<>(query.clone());

		if(!ctx.getQuery().hasComponent("from", dialect))
		{
			throw new IllegalArgumentException("No table set to delete");
		}

		AbstractFrom fromClause = ctx.getQuery().getOneComponent("from", dialect);

		String table = null;

		if(fromClause instanceof FromClause)
		{
			FromClause fromClauseCast = (FromClause)fromClause;

			table = wrap(fromClauseCast.getTable());
		}

		if(fromClause instanceof RawFromClause)
		{
			RawFromClause rawFromClause = (RawFromClause)fromClause;

			table = wrapIdentifiers(rawFromClause.getExpression());
			ctx.getBindings().addAll(Arrays.asList(rawFromClause.getBindings()));
		}

		if(table == null)
		{
			throw new IllegalArgumentException("Invalid table expression");
		}

		String where = compileWheres(ctx);

		if(!StringUtils.isBlank(where))
		{
			where = " " + where;
		}

		ctx.setRawSql("DELETE FROM " + table + where);

		return ctx;
	}


	public <T extends Query<T>> String compileUnion(SqlResult<T> ctx)
	{
		// Handle UNION, EXCEPT and INTERSECT
		if(ctx.getQuery().getComponents("combine", dialect).isEmpty())
		{
			return null;
		}

		List<String> combinedQueries = new ArrayList<>();

		List<AbstractCombine> clauses = ctx.getQuery().getComponents("combine", dialect);

		for(AbstractCombine clause : clauses)
		{
			if(clause instanceof Combine)
			{
				Combine<T> combineClause = (Combine<T>)clause;

				String combineOperator = combineClause.getOperation().toUpperCase() + " " + (combineClause.isAll() ? "ALL " : "");

				SqlResult<T> subCtx = compileSelectQuery(combineClause.getQuery());

				ctx.getBindings().addAll(subCtx.getBindings());

				combinedQueries.add(combineOperator + subCtx.getRawSql());
			}
			else
			{
				RawCombine combineRawClause = (RawCombine)clause;

				ctx.getBindings().addAll(Arrays.asList(combineRawClause.getBindings()));

				combinedQueries.add(wrapIdentifiers(combineRawClause.getExpression()));
			}
		}

		return String.join(" ", combinedQueries);
	}


	public <T extends Query<T>> String compileLimit(SqlResult<T> ctx)
	{
		int limit = ctx.getQuery().getLimit(dialect);
		int offset = ctx.getQuery().getOffset(dialect);

		if(limit == 0 && offset == 0)
		{
			return null;
		}

		if(offset == 0)
		{
			ctx.getBindings().add(limit);
			return "LIMIT ?";
		}

		if(limit == 0)
		{
			ctx.getBindings().add(offset);
			return "OFFSET ?";
		}

		ctx.getBindings().add(limit);
		ctx.getBindings().add(offset);

		return "LIMIT ? OFFSET ?";
	}

	public <T extends Query<T>> String compileOrders(SqlResult<T> ctx)
	{
		if(!ctx.getQuery().hasComponent("order", dialect))
		{
			return null;
		}

		List<String> columns = ctx.getQuery()
				.getComponents("order", dialect)
				.stream().map(x -> {
					if(x instanceof RawOrderBy)
					{
						RawOrderBy raw = (RawOrderBy)x;
						ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
						return wrapIdentifiers(raw.getExpression());
					}
					OrderBy orderBy = ((OrderBy)x);
					String direction = orderBy.isAscending() ? "" : " DESC";
					return wrap(orderBy.getColumn()) + direction;
				}).collect(Collectors.toList());

		return "ORDER BY " + String.join(", ", columns);

	}


	public <T extends Query<T>> String compileHaving(SqlResult<T> ctx)
	{
		if(!ctx.getQuery().hasComponent("having", dialect))
		{
			return null;
		}

		List<String> sql = new ArrayList<>();
		String boolOperator;

		List<AbstractCondition> having = ctx.getQuery().getComponents("having", dialect);

		for(int i = 0; i < having.size(); i++)
		{
			String compiled = compileCondition(ctx, having.get(i));

			if(!StringUtils.isBlank(compiled))
			{
				boolOperator = i > 0 ? having.get(i).isOr() ? "OR " : "AND " : "";

				sql.add(boolOperator + compiled);
			}
		}

		return "HAVING " + String.join(" ", sql);
	}


	public <T extends Query<T>> String compileGroups(SqlResult<T> ctx)
	{
		if(!ctx.getQuery().hasComponent("group", dialect))
		{
			return null;
		}

		List<String> columns = ctx.getQuery()
				.getComponents("group", dialect)
				.stream().map(x -> compileColumn(ctx, (AbstractColumn)x))
				.collect(Collectors.toList());

		return "GROUP BY " + String.join(", ", columns);
	}

	public <T extends Query<T>> String compileWheres(SqlResult<T> ctx)
	{

		if(!ctx.getQuery().hasComponent("from", dialect) || !ctx.getQuery().hasComponent("where", dialect))
		{
			return null;
		}

		List<AbstractCondition> conditions = ctx.getQuery().getComponents("where", dialect);
		String sql = compileConditions(ctx, conditions).trim();

		return StringUtils.isBlank(sql) ? null : "WHERE " + sql;

	}


	public <T extends Query<T>> String compileTableExpression(SqlResult<T> ctx, AbstractFrom from)
	{
		if(from instanceof RawFromClause)
		{
			RawFromClause raw = (RawFromClause)from;
			ctx.getBindings().addAll(Arrays.asList(raw.getBindings()));
			return wrapIdentifiers(raw.getExpression());
		}

		if(from instanceof QueryFromClause)
		{

			T fromQuery = ((QueryFromClause<T>)from).getQuery();

			String alias = StringUtils.isBlank(fromQuery.getQueryAlias()) ? "" : " " + tableAsKeyword + wrapValue(fromQuery.getQueryAlias());

			SqlResult subCtx = compileSelectQuery(fromQuery);

			ctx.getBindings().addAll(subCtx.getBindings());

			return "(" + subCtx.getRawSql() + ")" + alias;
		}

		if(from instanceof FromClause)
		{
			FromClause fromClause = (FromClause)from;

			return wrap(fromClause.getTable());
		}

		throw InvalidClauseException("TableExpression", from);
	}


	private ClassCastException InvalidClauseException(String section, AbstractClause clause)
	{
		return new ClassCastException("Invalid type \"" + clause.getClass().getName() + "\" provided for the \"" + section + "\" clause.");
	}


	protected <T extends Query<T>> String compileJoin(SqlResult<T> ctx, Join join)
	{
		AbstractFrom from = join.getOneComponent("from", dialect);

		List<AbstractCondition> conditions = join.getComponents("where", dialect);

		String joinTable = compileTableExpression(ctx, from);
		String constraints = compileConditions(ctx, conditions);

		String onClause = !conditions.isEmpty() ? " ON " + constraints : "";

		return join.getType() + " " + joinTable + onClause;
	}

	protected <T extends Query<T>> String compileJoins(SqlResult<T> ctx)
	{

		if(!ctx.getQuery().hasComponent("join", dialect))
		{
			return null;
		}

		return "\n" + ctx.getQuery()
				.getComponents("join", dialect)
				.stream().map(x -> compileJoin(ctx, ((BaseJoin)x).getJoin()))
				.collect(Collectors.joining("\n"));
	}


	protected <T extends Query<T>> String compileFrom(SqlResult<T> ctx)
	{
		if(!ctx.getQuery().hasComponent("from", dialect))
		{
			throw new IllegalArgumentException("No table is set");
		}

		AbstractFrom from = ctx.getQuery().getOneComponent("from", dialect);

		return "FROM " + compileTableExpression(ctx, from);
	}


	protected <T extends Query<T>> String compileColumn(SqlResult<T> ctx, AbstractColumn column)
	{
		if(column instanceof RawColumn)
		{
			RawColumn rawColumn = (RawColumn)column;
			ctx.getBindings().addAll(Arrays.asList(rawColumn.getBindings()));
			return wrapIdentifiers(rawColumn.getExpression());
		}

		if(column instanceof QueryColumn)
		{

			QueryColumn<T> queryColumn = (QueryColumn<T>)column;

			String alias = StringUtils.EMPTY;
			if(!StringUtils.isBlank(queryColumn.getQuery().getQueryAlias()))
			{
				alias = " " + columnAsKeyword + wrapValue(queryColumn.getQuery().getQueryAlias());
			}

			SqlResult<T> subCtx = compileSelectQuery(queryColumn.getQuery());

			ctx.getBindings().addAll(subCtx.getBindings());

			return "(" + subCtx.getRawSql() + ")" + alias;
		}

		return wrap(((Column)column).getName());
	}

	protected <T extends Query<T>> String compileColumns(SqlResult<T> ctx)
	{
		if(ctx.getQuery().hasComponent("aggregate", dialect))
		{
			AggregateClause aggregate = ctx.getQuery().getOneComponent("aggregate", dialect);
			List<String> aggregateColumns = aggregate.getColumns().stream().map(x -> compileColumn(ctx, new Column(x)))
					.collect(Collectors.toList());

			String sql = StringUtils.EMPTY;
			if(aggregateColumns.size() == 1)
			{
				sql = String.join(", ", aggregateColumns);

				if(ctx.getQuery().isDistinct())
				{
					sql = "DISTINCT " + sql;
				}

				return "SELECT " + aggregate.getType().toUpperCase() + "(" + sql + ") " + columnAsKeyword + wrapValue(aggregate.getType());
			}

			return "SELECT 1";
		}

		List<String> columns = ctx.getQuery()
				.getComponents("select", dialect)
				.stream()
				.map(x -> compileColumn(ctx, (AbstractColumn)x))
				.collect(Collectors.toList());

		String distinct = ctx.getQuery().isDistinct() ? "DISTINCT " : "";

		String select = !columns.isEmpty() ? String.join(", ", columns) : "*";

		return "SELECT " + distinct + select;
	}

	//region condition

	public <T extends Query<T>> String compileConditions(SqlResult<T> ctx, List<AbstractCondition> conditions)
	{
		List<String> result = new ArrayList<>();

		for(int i = 0; i < conditions.size(); i++)
		{
			String compiled = compileCondition(ctx, conditions.get(i));

			if(StringUtils.isBlank(compiled))
			{
				continue;
			}

			String boolOperator = i == 0 ? "" : (conditions.get(i).isOr() ? "OR " : "AND ");

			result.add(boolOperator + compiled);
		}

		return String.join(" ", result);
	}

	public <T extends Query<T>> String compileCondition(SqlResult<T> ctx, AbstractCondition clause)
	{
		if(clause instanceof TwoColumnsCondition)
		{
			return compileTwoColumnsCondition(ctx, (TwoColumnsCondition)clause);
		}
		else if(clause instanceof BasicCondition)
		{
			return compileBasicCondition(ctx, (BasicCondition)clause);
		}
		else if(clause instanceof BasicDateCondition)
		{
			return compileBasicDateCondition(ctx, (BasicDateCondition)clause);
		}
		else if(clause instanceof BasicStringCondition)
		{
			return compileBasicStringCondition(ctx, (BasicStringCondition)clause);
		}
		else if(clause instanceof BetweenCondition)
		{
			return compileBetweenCondition(ctx, (BetweenCondition)clause);
		}
		else if(clause instanceof BooleanCondition)
		{
			return compileBooleanCondition(ctx, (BooleanCondition)clause);
		}
		else if(clause instanceof NestedCondition)
		{
			return compileNestedCondition(ctx, (NestedCondition)clause);
		}
		else if(clause instanceof NullCondition)
		{
			return compileNullCondition(ctx, (NullCondition)clause);
		}
		else if(clause instanceof RawCondition)
		{
			return compileRawCondition(ctx, (RawCondition)clause);
		}
		else if(clause instanceof ExistsCondition)
		{
			return compileExistsCondition(ctx, (ExistsCondition)clause);
		}
		else if(clause instanceof InQueryCondition)
		{
			return compileInQueryCondition(ctx, (InQueryCondition)clause);
		}
		else if(clause instanceof InCondition)
		{
			return compileInCondition(ctx, (InCondition)clause);
		}
		else if(clause instanceof SubQueryCondition)
		{
			return compileSubQueryCondition(ctx, (SubQueryCondition)clause);
		}
		return "";
	}

	public <T extends Query<T>> String compileSubQueryCondition(SqlResult<T> ctx, SubQueryCondition<T> x)
	{
		SqlResult<T> subCtx = compileSelectQuery(x.getQuery());

		ctx.getBindings().addAll(subCtx.getBindings());

		return "(" + subCtx.getRawSql() + ") " + checkOperator(x.getOperator()) + " " + parameter(ctx, x.getValue());
	}

	public <T extends Query<T>> String compileInCondition(SqlResult<T> ctx, InCondition item)
	{
		String column = wrap(item.getColumn());

		if(null == item.getValue())
		{
			return item.isNot() ? "1 = 1 /* NOT IN [empty list] */" : "1 = 0 /* IN [empty list] */";
		}

		String inOperator = item.isNot() ? "NOT IN" : "IN";

		String values = parameterize(ctx, ArrayUtils.flatten(item.getValue()));

		return column + " " + inOperator + " (" + values + ")";
	}

	public <T extends Query<T>> String compileInQueryCondition(SqlResult<T> ctx, InQueryCondition<T> item)
	{
		SqlResult<T> subCtx = compileSelectQuery(item.getQuery());

		ctx.getBindings().addAll(subCtx.getBindings());

		String inOperator = item.isNot() ? "NOT IN" : "IN";

		return wrap(item.getColumn()) + " " + inOperator + " (" + subCtx.getRawSql() + ")";
	}

	public <T extends Query<T>> String compileExistsCondition(SqlResult<T> ctx, ExistsCondition<T> item)
	{
		String op = item.isNot() ? "NOT EXISTS" : "EXISTS";

		SqlResult<T> subCtx = compileSelectQuery(item.getQuery());

		ctx.getBindings().addAll(subCtx.getBindings());

		return op + " (" + subCtx.getRawSql() + ")";
	}

	public <T extends Query<T>> String compileRawCondition(SqlResult<T> ctx, RawCondition item)
	{
		ctx.getBindings().addAll(Arrays.asList(item.getBindings()));
		return wrapIdentifiers(item.getExpression());
	}

	public <T extends Query<T>> String compileNullCondition(SqlResult<T> ctx, NullCondition item)
	{
		String op = item.isNot() ? "IS NOT NULL" : "IS NULL";
		return wrap(item.getColumn()) + " " + op;
	}

	public <T extends Query<T>, V extends BaseQuery<V>> String compileNestedCondition(SqlResult<T> ctx, NestedCondition<V> clause)
	{
		if(!clause.getQuery().hasComponent("where", dialect))
		{
			return null;
		}

		List<AbstractCondition> clauses = clause.getQuery().getComponents("where", dialect);

		String sql = compileConditions(ctx, clauses);

		return clause.isNot() ? "NOT (" + sql + ")" : "(" + sql + ")";
	}

	public <T extends Query<T>> String compileBooleanCondition(SqlResult<T> ctx, BooleanCondition item)
	{
		String column = wrap(item.getColumn());
		String value = item.isValue() ? compileTrue() : compileFalse();

		String op = item.isNot() ? "!=" : "=";

		return column + " " + op + " " + value;
	}

	public String compileTrue()
	{
		return "true";
	}

	public String compileFalse()
	{
		return "false";
	}

	public <T extends Query<T>, V> String compileBetweenCondition(SqlResult<T> ctx, BetweenCondition<V> item)
	{
		String between = item.isNot() ? "NOT BETWEEN" : "BETWEEN";
		String lower = parameter(ctx, item.getLower());
		String higher = parameter(ctx, item.getHigher());

		return wrap(item.getColumn()) + " " + between + " " + lower + " AND " + higher;
	}

	public <T extends Query<T>> String compileBasicStringCondition(SqlResult<T> ctx, BasicStringCondition clause)
	{
		String column = wrap(clause.getColumn());

		Object valueObj = resolve(ctx, clause.getValue());

		if(valueObj == null)
		{
			throw new IllegalArgumentException("Expecting a non nullable string");
		}

		String value = valueObj.toString();

		String method = clause.getOperator();

		if(Arrays.asList(new String[]{"starts", "ends", "contains", "like"}).contains(clause.getOperator()))
		{
			method = "LIKE";

			switch(clause.getOperator())
			{
				case "starts":
					value = value + "%";
					break;
				case "ends":
					value = "%" + value;
					break;
				case "contains":
					value = "%" + value + "%";
					break;
			}
		}

		String sql;


		if(!clause.isCaseSensitive())
		{
			column = compileLower(column);
			value = value.toLowerCase();
		}

		if(clause.getValue() instanceof UnsafeLiteral)
		{
			sql = column + " " + checkOperator(method) + " " + value;
		}
		else
		{
			sql = column + " " + checkOperator(method) + " " + parameter(ctx, value);
		}

		if(!StringUtils.isBlank(clause.getEscapeCharacter()))
		{
			sql = sql + " ESCAPE '" + clause.getEscapeCharacter() + "'";
		}

		return clause.isNot() ? "NOT (" + sql + ")" : sql;


	}

	public String compileLower(String value)
	{
		return "LOWER(" + value + ")";
	}

	public <T extends Query<T>> String compileBasicDateCondition(SqlResult<T> ctx, BasicDateCondition clause)
	{
		String column = wrap(clause.getColumn());
		String op = checkOperator(clause.getOperator());

		String sql = clause.getPart().toUpperCase() + "(" + column + ")" + " " + op + " " + parameter(ctx, clause.getValue());

		return clause.isNot() ? "NOT (" + sql + ")" : sql;
	}

	public <T extends Query<T>> String compileBasicCondition(SqlResult<T> ctx, BasicCondition clause)
	{
		String sql = wrap(clause.getColumn()) + " " + checkOperator(clause.getOperator()) + " " + parameter(ctx, clause.getValue());

		if(clause.isNot())
		{
			return "NOT (" + sql + ")";
		}

		return sql;
	}


	public <T extends Query<T>> String compileTwoColumnsCondition(SqlResult<T> ctx, TwoColumnsCondition clause)
	{
		String op = clause.isNot() ? "NOT " : "";
		return op + wrap(clause.getFirst()) + " " + checkOperator(clause.getOperator()) + " " + wrap(clause.getSecond());
	}

	public String checkOperator(String op)
	{
		op = op.toLowerCase();

		boolean valid = operators.contains(op) || userOperators.contains(op);

		if(!valid)
		{
			throw new IllegalArgumentException("The operator '" + op + "' cannot be used. Please consider white listing it before using it.");
		}

		return op;
	}


	//endregion

	public <T extends Query<T>> Object resolve(SqlResult<T> ctx, Object parameter)
	{
		if(parameter instanceof UnsafeLiteral)
		{
			return ((UnsafeLiteral)parameter).getValue();
		}

		if(parameter instanceof Variable)
		{
			return ctx.getQuery().findVariable(((Variable)parameter).getName());
		}

		return parameter;
	}


	public <T, V extends Query<V>> String parameterize(SqlResult<V> ctx, List<T> values)
	{
		return values.stream().map(x -> parameter(ctx, x)).collect(Collectors.joining(", "));
	}

	public <T extends Query<T>> String parameter(SqlResult<T> ctx, Object parameter)
	{

		if(parameter instanceof String)
		{
			return parameter.toString();
		}

		if(parameter instanceof Variable)
		{
			Object value = ctx.getQuery().findVariable(((Variable)parameter).getName());
			ctx.getBindings().add(value);
			return "?";
		}

		ctx.getBindings().add(parameter);
		return "?";
	}

	public List<String> wrapArray(List<String> values)
	{
		return values.stream().map(this::wrap).collect(Collectors.toList());
	}

	public String wrap(String value)
	{
		if(value.toLowerCase().contains(" as "))
		{
			int index = value.toLowerCase().indexOf(" as ");
			String before = value.substring(0, index);
			String after = value.substring(index + 4);

			return wrap(before) + " " + columnAsKeyword + wrapValue(after);
		}

		if(value.contains("."))
		{
			return Arrays.stream(value.split("\\.")).map(this::wrapValue).collect(Collectors.joining("."));
		}

		return wrapValue(value);
	}

	public String wrapValue(String value)
	{
		if(value.equals("*"))
		{
			return value;
		}
		return openingIdentifier + value.replaceAll(closingIdentifier, closingIdentifier + closingIdentifier) + closingIdentifier;
	}


	public String wrapIdentifiers(String input)
	{
		input = StringUtils.replaceIdentifierUnlessEscaped(input, escapeCharacter, "{", openingIdentifier);
		input = StringUtils.replaceIdentifierUnlessEscaped(input, escapeCharacter, "}", closingIdentifier);
		input = StringUtils.replaceIdentifierUnlessEscaped(input, escapeCharacter, "[", openingIdentifier);
		input = StringUtils.replaceIdentifierUnlessEscaped(input, escapeCharacter, "]", closingIdentifier);
		return input;
	}

}
