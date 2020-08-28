package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.SqlResult;
import com.github.haivan.sqlbuilder.clauses.condition.BasicDateCondition;

import java.util.HashMap;
import java.util.Map;

public class SqliteCompiler extends Compiler
{

	public SqliteCompiler()
	{
		lastId = "select last_insert_rowid() as id";
		dialect = Dialect.Sqlite;
	}

	@Override
	public String compileTrue()
	{
		return "1";
	}

	@Override
	public String compileFalse()
	{
		return "0";
	}

	@Override
	public <T extends Query<T>> String compileLimit(SqlResult<T> ctx)
	{
		int limit = ctx.getQuery().getLimit(dialect);
		int offset = ctx.getQuery().getOffset(dialect);

		if(limit == 0 && offset > 0)
		{
			ctx.getBindings().add(offset);
			return "LIMIT -1 OFFSET ?";
		}

		return super.compileLimit(ctx);
	}


	@Override
	public <T extends Query<T>> String compileBasicDateCondition(SqlResult<T> ctx, BasicDateCondition condition)
	{
		String column = wrap(condition.getColumn());
		String value = parameter(ctx, condition.getValue());

		Map<String, String> formatMap = new HashMap<String, String>()
		{{
			this.put("date", "%Y-%m-%d");
			this.put("time", "%H:%M:%S");
			this.put("year", "%Y");
			this.put("month", "%m");
			this.put("day", "%d");
			this.put("hour", "%H");
			this.put("minute", "%M");
		}};

		if(!formatMap.containsKey(condition.getPart()))
		{
			return column + " " + condition.getOperator() + " " + value;
		}

		String sql = "strftime('" + formatMap.get(condition.getPart()) + "', " + column + ") " + condition.getOperator() + " cast(" + value + " as text)";

		if(condition.isNot())
		{
			return "NOT (" + sql + ")";
		}

		return sql;
	}
}
