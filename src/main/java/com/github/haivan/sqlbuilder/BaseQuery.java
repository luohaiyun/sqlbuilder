package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.clauses.condition.*;
import com.github.haivan.sqlbuilder.clauses.from.FromClause;
import com.github.haivan.sqlbuilder.clauses.from.QueryFromClause;
import com.github.haivan.sqlbuilder.clauses.from.RawFromClause;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseQuery<Q extends BaseQuery<Q>> extends AbstractQuery implements Cloneable
{

	protected List<AbstractClause> clauses = new ArrayList<>();

	protected boolean orFlag = false;

	protected boolean notFlag = false;

	protected String dialect;

	public Q setDialect(String dialect)
	{
		this.dialect = dialect;

		return (Q)this;
	}


	public BaseQuery()
	{

	}

	@Override
	public Q clone()
	{
		Q q = newQuery();
		q.clauses = this.clauses.stream().map(AbstractClause::clone).collect(Collectors.toList());
		return q;
	}

	public Q setParent(AbstractQuery parent)
	{
		if(this == parent)
		{
			throw new IllegalArgumentException("Cannot set the same query as a parent of itself");
		}

		this.parent = parent;
		return (Q)this;
	}

	public abstract Q newQuery();

	public Q newChild()
	{
		Q newQuery = newQuery().setParent(this);
		newQuery.setDialect(this.dialect);
		return (Q)this;
	}

	public Q addComponent(String component, AbstractClause clause)
	{
		return this.addComponent(component, clause, null);
	}

	public Q addComponent(String component, AbstractClause clause, String dialect)
	{
		if(dialect == null)
		{
			dialect = this.dialect;
		}

		clause.setDialect(dialect);
		clause.setComponent(component);
		clauses.add(clause);
		return (Q)this;
	}

	public Q addOrReplaceComponent(String component, AbstractClause clause)
	{
		return this.addOrReplaceComponent(component, clause, null);
	}

	public Q addOrReplaceComponent(String component, AbstractClause clause, String dialect)
	{
		final String fDialect = dialect == null ? this.dialect : dialect;

		getComponents(component).stream().filter(c -> Objects.equals(fDialect, c.getDialect()))
				.findFirst().ifPresent(current -> clauses.remove(current));

		return addComponent(component, clause, dialect);
	}

	public <C extends AbstractClause> List<C> getComponents(String component)
	{
		return getComponents(component, null);
	}

	public <C extends AbstractClause> List<C> getComponents(String component, String dialect)
	{
		final String finalEngine;
		if(dialect == null)
		{
			finalEngine = this.dialect;
		}
		else
		{
			finalEngine = dialect;
		}

		return clauses.stream().filter(x -> Objects.equals(x.getComponent(), component))
				.filter(x -> finalEngine == null || x.getDialect() == null || finalEngine.equals(x.getDialect()))
				.map(x -> (C)x)
				.collect(Collectors.toList());
	}


	public <C extends AbstractClause> C getOneComponent(String component)
	{
		return getOneComponent(component, null);
	}

	public <C extends AbstractClause> C getOneComponent(String component, String dialect)
	{

		final String fDialect = dialect == null ? this.dialect : dialect;

		List<C> all = getComponents(component, fDialect);

		return all.stream().filter(c -> Objects.equals(c.getDialect(), fDialect))
				.findFirst().orElse(all.stream().filter(c -> c.getDialect() == null).findFirst().orElse(null));
	}

	public boolean hasComponent(String component)
	{
		return hasComponent(component, null);
	}

	public boolean hasComponent(String component, String dialect)
	{
		if(dialect == null)
		{
			dialect = this.dialect;
		}

		return !getComponents(component, dialect).isEmpty();
	}

	public Q clearComponent(String component)
	{
		return clearComponent(component, null);
	}

	public Q clearComponent(String component, String dialect)
	{
		final String fDialect;
		if(dialect == null)
		{
			fDialect = this.dialect;
		}
		else
		{
			fDialect = dialect;
		}

		clauses = clauses.stream().filter(x -> !(x.getComponent().equals(component)
				&& (fDialect == null || x.getDialect() == null || fDialect.equals(x.getDialect()))))
				.collect(Collectors.toList());

		return (Q)this;
	}


	protected Q and()
	{
		orFlag = false;
		return (Q)this;
	}

	public Q or()
	{
		orFlag = true;
		return (Q)this;
	}

	public Q not(boolean flag)
	{
		notFlag = flag;
		return (Q)this;
	}

	public Q not()
	{
		return not(true);
	}

	protected boolean getOr()
	{
		boolean ret = orFlag;
		orFlag = false;
		return ret;
	}

	protected boolean getNot()
	{
		boolean ret = notFlag;

		notFlag = false;
		return ret;
	}

	public Q from(String table)
	{
		return addOrReplaceComponent("from", new FromClause(table));
	}

	public Q from(Query query)
	{
		return from(query, null);
	}

	public Q from(Query query, String alias)
	{
		query = query.clone();
		query.setParent(this);

		if(alias != null)
		{
			query.as(alias);
		}

		return addOrReplaceComponent("from", new QueryFromClause(query));
	}

	public Q from(Function<Query, Query> callback)
	{
		return from(callback, null);
	}

	public Q from(Function<Query, Query> callback, String alias)
	{
		Query query = new Query();
		query.setParent((Q)this);

		return from(callback.apply(query), alias);
	}

	public Q fromRaw(String sql, Object... bindings)
	{
		return addOrReplaceComponent("from", new RawFromClause(sql, bindings));
	}

	public Q fromRaw(Function<Query, Query> callback)
	{
		return fromRaw(callback, null);
	}

	public Q fromRaw(Function<Query, Query> callback, String alias)
	{
		Query query = new Query();
		query.setParent(this);

		return from(callback.apply(query), alias);
	}


	public Q where(String column, String op, Object value)
	{
		if(value == null)
		{
			return not(op.equals("=")).whereNull(column);
		}

		return addComponent("where", new BasicCondition(column, op, value, getNot(), getOr()));
	}

	public Q whereNot(String column, String op, Object value)
	{
		return not().where(column, op, value);
	}


	public Q orWhere(String column, String op, Object value)
	{
		return or().where(column, op, value);
	}

	public Q orWhereNot(String column, String op, Object value)
	{
		return or().not().where(column, op, value);
	}

	public Q where(String column, Object value)
	{
		return where(column, "=", value);
	}

	public Q whereNot(String column, Object value)
	{
		return whereNot(column, "=", value);
	}

	public Q orWhere(String column, Object value)
	{
		return orWhere(column, "=", value);
	}

	public Q orWhereNot(String column, Object value)
	{
		return orWhereNot(column, "=", value);
	}

	public Q where(Map<String, Object> values)
	{
		Q query = (Q)this;
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
			query = not(notFlag).where(entry.getKey(), entry.getValue());
		}

		return query;
	}

	public Q whereRaw(String sql, Object... bindings)
	{
		return addComponent("where", new RawCondition(sql, bindings));
	}

	public Q orWhereRaw(String sql, Object... bindings)
	{
		return or().whereRaw(sql, bindings);
	}

	public Q where(Function<Q, Q> callback)
	{
		Q query = callback.apply(newChild());

		if(query.clauses.stream().anyMatch(x -> x.getComponent().equals("where")))
		{
			return (Q)this;
		}
		return addComponent("where", new NestedCondition<Q>(query, getNot(), getOr()));
	}

	public Q whereNot(Function<Q, Q> callback)
	{
		return not().where(callback);
	}

	public Q orWhere(Function<Q, Q> callback)
	{
		return or().where(callback);
	}

	public Q orWhereNot(Function<Q, Q> callback)
	{
		return or().not().where(callback);
	}

	public Q whereColumns(String first, String op, String second)
	{
		return addComponent("where", new TwoColumnsCondition(first, op, second, getNot(), getOr()));
	}

	public Q orWhereColumns(String first, String op, String second)
	{
		return or().whereColumns(first, op, second);
	}

	public Q whereNull(String column)
	{
		return addComponent("where", new NullCondition(column, getNot(), getOr()));
	}

	public Q whereNotNull(String column)
	{
		return not().whereNull(column);
	}

	public Q orWhereNull(String column)
	{
		return or().whereNull(column);
	}

	public Q orWhereNotNull(String column)
	{
		return or().not().whereNull(column);
	}

	public Q whereTrue(String column)
	{
		return addComponent("where", new BooleanCondition(column, true, getNot(), getOr()));
	}

	public Q orWhereTrue(String column)
	{
		return or().whereTrue(column);
	}

	public Q whereFalse(String column)
	{
		return addComponent("where", new BooleanCondition(column, false, getNot(), getOr()));
	}

	public Q orWhereFalse(String column)
	{
		return or().whereFalse(column);
	}

	public Q whereLike(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return addComponent("where", new BasicStringCondition(column, "like", value, caseSensitive, escapeCharacter, getNot(), getOr()));
	}

	public Q orWhereLike(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().whereLike(column, value, caseSensitive, escapeCharacter);
	}

	public Q whereNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return not().whereNotLike(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereNotLike(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().whereNotLike(column, value, caseSensitive, escapeCharacter);
	}

	public Q whereStarts(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return addComponent("where", new BasicStringCondition(column, "starts", value, caseSensitive, escapeCharacter, getNot(), getOr()));
	}

	public Q whereNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return not().whereStarts(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereStarts(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().whereStarts(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereNotStarts(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().not().whereStarts(column, value, caseSensitive, escapeCharacter);
	}

	public Q whereEnds(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return addComponent("where", new BasicStringCondition(column, "ends", value
				, caseSensitive, escapeCharacter, getNot(), getOr()));
	}

	public Q whereNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return not().whereEnds(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereEnds(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().whereEnds(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereNotEnds(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().not().whereEnds(column, value, caseSensitive, escapeCharacter);
	}

	public Q whereContains(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return addComponent("where", new BasicStringCondition(column, "contains", value
				, caseSensitive, escapeCharacter, getNot(), getOr()));
	}

	public Q whereNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return not().whereContains(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereContains(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().whereContains(column, value, caseSensitive, escapeCharacter);
	}

	public Q orWhereNotContains(String column, Object value, boolean caseSensitive, String escapeCharacter)
	{
		return or().not().whereContains(column, value, caseSensitive, escapeCharacter);
	}

	public <T> Q whereBetween(String column, T lower, T higher)
	{
		return addComponent("where",
				new BetweenCondition<>(column, getNot(), getOr(), lower, higher));
	}

	public <T> Q orWhereBetween(String column, T lower, T higher)
	{
		return or().whereBetween(column, lower, higher);
	}

	public <T> Q whereNotBetween(String column, T lower, T higher)
	{
		return not().whereBetween(column, lower, higher);
	}

	public <T> Q orWhereNotBetween(String column, T lower, T higher)
	{
		return or().not().whereBetween(column, lower, higher);
	}

	public Q whereIn(String column, Object values)
	{
		return addComponent("where", new InCondition(column, values, getNot(), getOr()));
	}

	public Q orWhereIn(String column, Object values)
	{
		return or().whereIn(column, values);
	}

	public Q whereNotIn(String column, Object values)
	{
		return not().whereIn(column, values);
	}

	public Q orWhereNotIn(String column, Object values)
	{
		return or().not().whereIn(column, values);
	}


	public Q WhereIn(String column, Query query)
	{
		return addComponent("where", new InQueryCondition(query, column, getNot(), getOr()));
	}

	public Q whereIn(String column, Function<Query, Query> callback)
	{
		Query query = callback.apply(new Query<>().setParent(this));

		return whereIn(column, query);
	}

	public Q orWhereIn(String column, Query query)
	{
		return or().WhereIn(column, query);
	}

	public Q orWhereIn(String column, Function<Query, Query> callback)
	{
		return or().whereIn(column, callback);
	}

	public Q whereNotIn(String column, Query query)
	{
		return not().whereIn(column, query);
	}

	public Q whereNotIn(String column, Function<Query, Query> callback)
	{
		return not().whereIn(column, callback);
	}

	public Q orWhereNotIn(String column, Query query)
	{
		return or().not().WhereIn(column, query);
	}

	public Q orWhereNotIn(String column, Function<Query, Query> callback)
	{
		return or().not().whereIn(column, callback);
	}

	public Q where(String column, String op, Function<Q, Q> callback)
	{
		Q query = callback.apply(newChild());

		return where(column, op, query);
	}

	public Q where(String column, String op, Query query)
	{
		return addComponent("where"
				, new QueryCondition<>(column, op, query, getNot(), getOr()));
	}

	public Q whereSub(Query query, Object value)
	{
		return whereSub(query, "=", value);
	}

	public Q whereSub(Query query, String op, Object value)
	{
		return addComponent("where", new SubQueryCondition
				(value, op, query, getNot(), getOr()));
	}

	public Q orWhereSub(Query query, Object value)
	{
		return or().whereSub(query, value);
	}

	public Q orWhereSub(Query query, String op, Object value)
	{
		return or().whereSub(query, op, value);
	}

	public Q orWhere(String column, String op, Query query)
	{
		return or().where(column, op, query);
	}

	public Q orWhere(String column, String op, Function<Query, Query> callback)
	{
		return or().where(column, op, callback);
	}

	public Q whereExists(Query query)
	{
		if(!query.hasComponent("from"))
		{
			throw new IllegalArgumentException("'FromClause' cannot be empty if used inside a 'whereExists' condition");
		}

		query = ((Query)(query.clone().clearComponent("select")))
				.selectRaw("1");

		return addComponent("where", new ExistsCondition(query, getNot(), getOr()));
	}

	public Q whereExists(Function<Query, Query> callback)
	{
		Query childQuery = new Query<>().setParent(this);
		return whereExists(callback.apply(childQuery));
	}

	public Q whereNotExists(Query query)
	{
		return not().whereExists(query);
	}

	public Q whereNotExists(Function<Query, Query> callback)
	{
		return not().whereExists(callback);
	}

	public Q orWhereExists(Query query)
	{
		return or().whereExists(query);
	}

	public Q orWhereExists(Function<Query, Query> callback)
	{
		return or().whereExists(callback);
	}

	public Q orWhereNotExists(Query query)
	{
		return or().not().whereExists(query);
	}

	public Q orWhereNotExists(Function<Query, Query> callback)
	{
		return or().not().whereExists(callback);
	}

	public Q whereDatePart(String part, String column, String op, Object value)
	{
		return addComponent("where", new BasicDateCondition(column, op, value, getNot(), getOr(), part));
	}

	public Q whereNotDatePart(String part, String column, String op, Object value)
	{
		return not().whereDatePart(part, column, op, value);
	}

	public Q orWhereDatePart(String part, String column, String op, Object value)
	{
		return or().whereDatePart(part, column, op, value);
	}

	public Q orWhereNotDatePart(String part, String column, String op, Object value)
	{
		return or().not().whereDatePart(part, column, op, value);
	}

	public Q whereDate(String column, String op, Object value)
	{
		return whereDatePart("date", column, op, value);
	}

	public Q whereNotDate(String column, String op, Object value)
	{
		return not().whereDate(column, op, value);
	}

	public Q orWhereDate(String column, String op, Object value)
	{
		return or().whereDate(column, op, value);
	}

	public Q orWhereNotDate(String column, String op, Object value)
	{
		return or().not().whereDate(column, op, value);
	}

	public Q whereTime(String column, String op, Object value)
	{
		return whereDatePart("time", column, op, value);
	}

	public Q whereNotTime(String column, String op, Object value)
	{
		return not().whereTime(column, op, value);
	}

	public Q orWhereTime(String column, String op, Object value)
	{
		return or().whereTime(column, op, value);
	}

	public Q orWhereNotTime(String column, String op, Object value)
	{
		return or().not().whereTime(column, op, value);
	}

	public Q whereDatePart(String part, String column, Object value)
	{
		return whereDatePart(part, column, "=", value);
	}

	public Q whereNotDatePart(String part, String column, Object value)
	{
		return whereNotDatePart(part, column, "=", value);
	}

	public Q orWhereDatePart(String part, String column, Object value)
	{
		return orWhereDatePart(part, column, "=", value);
	}

	public Q orWhereNotDatePart(String part, String column, Object value)
	{
		return orWhereNotDatePart(part, column, "=", value);
	}

	public Q whereDate(String column, Object value)
	{
		return whereDate(column, "=", value);
	}

	public Q whereNotDate(String column, Object value)
	{
		return whereNotDate(column, "=", value);
	}

	public Q orWhereDate(String column, Object value)
	{
		return orWhereDate(column, "=", value);
	}

	public Q orWhereNotDate(String column, Object value)
	{
		return orWhereNotDate(column, "=", value);
	}

	public Q whereTime(String column, Object value)
	{
		return whereTime(column, "=", value);
	}

	public Q whereNotTime(String column, Object value)
	{
		return whereNotTime(column, "=", value);
	}

	public Q orWhereTime(String column, Object value)
	{
		return orWhereTime(column, "=", value);
	}

	public Q orWhereNotTime(String column, Object value)
	{
		return orWhereNotTime(column, "=", value);
	}
}
