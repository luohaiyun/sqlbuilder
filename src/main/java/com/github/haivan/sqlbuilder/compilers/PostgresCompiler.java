package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.SqlResult;
import com.github.haivan.sqlbuilder.clauses.condition.BasicDateCondition;

public class PostgresCompiler extends Compiler
{

	public PostgresCompiler()
	{
		lastId = "SELECT lastval() AS id";
		dialect = Dialect.PostgreSql;
	}

	@Override
	public <T extends Query<T>> String compileBasicDateCondition(SqlResult<T> ctx, BasicDateCondition condition)
	{
		String column = wrap(condition.getColumn());

		String left;

		if(condition.getPart() == "time")
		{
			left = column + "::time";
		}
		else if(condition.getPart() == "date")
		{
			left = column + "::date";
		}
		else
		{
			left = "DATE_PART('" + condition.getPart().toUpperCase() + "', " + column + ")";
		}

		String sql = left + " " + condition.getOperator() + " " + parameter(ctx, condition.getValue());

		if(condition.isNot())
		{
			return "NOT (" + sql + ")";
		}

		return sql;
	}
}
