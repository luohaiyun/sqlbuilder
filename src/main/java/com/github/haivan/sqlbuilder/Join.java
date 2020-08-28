package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.clauses.condition.TwoColumnsCondition;

import java.util.function.Function;

public class Join extends BaseQuery<Join>
{
	protected String type = "inner join";

	public String getType()
	{
		return type;
	}

	public Join asType(String type)
	{
		this.type = type.toUpperCase();
		return this;
	}

	public Join joinWith(String table)
	{
		return from(table);
	}

	public Join joinWith(Query query)
	{
		return from(query);
	}

	public Join joinWith(Function<Query, Query> callback)
	{
		return from(callback);
	}

	public Join asInner()
	{
		return asType("inner join");
	}

	public Join asOuter()
	{
		return asType("outer join");
	}

	public Join asLeft()
	{
		return asType("left join");
	}

	public Join asRight()
	{
		return asType("right join");
	}

	public Join asCross()
	{
		return asType("cross join");
	}


	public Join on(String first, String second)
	{
		return on(first, second, "=");
	}

	public Join on(String first, String second, String op)
	{
		return addComponent("where", new TwoColumnsCondition(first, op, second, getNot(), getOr()));
	}

	public Join orOn(String first, String second)
	{
		return orOn(first, second, "=");
	}

	public Join orOn(String first, String second, String op)
	{
		return or().on(first, second, op);
	}

	@Override
	public Join newQuery()
	{
		return new Join();
	}
}
