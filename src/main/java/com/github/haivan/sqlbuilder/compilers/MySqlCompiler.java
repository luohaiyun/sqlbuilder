package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.SqlResult;

public class MySqlCompiler extends Compiler
{

	public MySqlCompiler()
	{
		openingIdentifier = closingIdentifier = "`";
		lastId = "SELECT last_insert_id() as Id";
		dialect = Dialect.MySql;
	}

	@Override
	public <T extends Query<T>> String compileLimit(SqlResult<T> ctx)
	{
		int limit = ctx.getQuery().getLimit(dialect);
		int offset = ctx.getQuery().getOffset(dialect);

		if(offset == 0 && limit == 0)
		{
			return null;
		}

		if (offset == 0)
		{
			ctx.getBindings().add(limit);
			return "LIMIT ?";
		}

		if (limit == 0)
		{
			ctx.getBindings().add(offset);
			return "LIMIT 18446744073709551615 OFFSET ?";
		}

		// We have both values

		ctx.getBindings().add(limit);
		ctx.getBindings().add(offset);

		return "LIMIT ? OFFSET ?";
	}
}
