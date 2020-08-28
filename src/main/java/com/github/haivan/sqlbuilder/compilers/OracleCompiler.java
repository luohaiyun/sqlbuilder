package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.SqlResult;
import com.github.haivan.sqlbuilder.clauses.condition.BasicDateCondition;

import java.util.Date;

public class OracleCompiler extends Compiler
{
	public OracleCompiler()
	{
		columnAsKeyword = "";
		tableAsKeyword = "";
		parameterPrefix = ":p";
		dialect = Dialect.Oracle;
	}

	private boolean useLegacyPagination;

	public boolean isUseLegacyPagination()
	{
		return useLegacyPagination;
	}

	public void setUseLegacyPagination(boolean useLegacyPagination)
	{
		this.useLegacyPagination = useLegacyPagination;
	}


	@Override
	protected <T extends Query<T>> SqlResult<T> compileSelectQuery(T query)
	{
		SqlResult<T> result = super.compileSelectQuery(query);

		if(useLegacyPagination)
		{
			applyLegacyLimit(result);
		}

		return result;
	}

	@Override
	public <T extends Query<T>> String compileLimit(SqlResult<T> ctx)
	{
		if(useLegacyPagination)
		{
			// in pre-12c versions of Oracle, limit is handled by ROWNUM techniques
			return null;
		}

		int limit = ctx.getQuery().getLimit(dialect);
		int offset = ctx.getQuery().getOffset(dialect);

		if(limit == 0 && offset == 0)
		{
			return null;
		}

		String safeOrder = "";

		if(!ctx.getQuery().hasComponent("order"))
		{
			safeOrder = "ORDER BY (SELECT 0 FROM DUAL) ";
		}

		if(limit == 0)
		{
			ctx.getBindings().add(offset);
			return safeOrder + "OFFSET ? ROWS";
		}

		ctx.getBindings().add(offset);
		ctx.getBindings().add(limit);

		return safeOrder + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	}

	private <T extends Query<T>> void applyLegacyLimit(SqlResult<T> ctx)
	{
		int limit = ctx.getQuery().getLimit(dialect);
		int offset = ctx.getQuery().getOffset(dialect);

		if(limit == 0 && offset == 0)
		{
			return;
		}

		String newSql;
		if(limit == 0)
		{
			newSql = "SELECT * FROM (SELECT \"results_wrapper\".*, ROWNUM \"row_num\" FROM (" + ctx.getRawSql() + ") \"results_wrapper\") WHERE \"row_num\" > ?";
			ctx.getBindings().add(offset);
		}
		else if(offset == 0)
		{
			newSql = "SELECT * FROM (" + ctx.getRawSql() + ") WHERE ROWNUM <= ?";
			ctx.getBindings().add(limit);
		}
		else
		{
			newSql =
					"SELECT * FROM (SELECT \"results_wrapper\".*, ROWNUM \"row_num\" FROM (" + ctx.getRawSql() + ") \"results_wrapper\" WHERE ROWNUM <= ?) WHERE \"row_num\" > ?";
			ctx.getBindings().add(limit + offset);
			ctx.getBindings().add(offset);
		}

		ctx.setRawSql(newSql);
	}


	@Override
	public <T extends Query<T>> String compileBasicDateCondition(SqlResult<T> ctx, BasicDateCondition condition)
	{
		String column = wrap(condition.getColumn());
		String value = parameter(ctx, condition.getValue());

		String sql = "";
		String valueFormat = "";

		boolean isDateTime = (condition.getValue() instanceof Date);

		switch(condition.getPart())
		{
			case "date": // assume YY-MM-DD format
				if(isDateTime)
					valueFormat = value;
				else
					valueFormat = "TO_DATE(" + value + ", 'YY-MM-DD')";
				sql = "TO_CHAR(" + column + ", 'YY-MM-DD') " + condition.getOperator() + " TO_CHAR(" + valueFormat + ", 'YY-MM-DD')";
				break;
			case "time":
				if(isDateTime)
					valueFormat = value;
				else
				{
					// assume HH:MM format
					if(condition.getValue().toString().split(":").length == 2)
						valueFormat = "TO_DATE(" + value + ", 'HH24:MI')";
					else // assume HH:MM:SS format
						valueFormat = "TO_DATE(" + value + ", 'HH24:MI:SS')";
				}
				sql = "TO_CHAR(" + column + ", 'HH24:MI:SS') " + condition.getOperator() + " TO_CHAR(" + valueFormat + ", 'HH24:MI:SS')";
				break;
			case "year":
			case "month":
			case "day":
			case "hour":
			case "minute":
			case "second":
				sql = "EXTRACT(" + condition.getPart().toUpperCase() + " FROM " + column + ") " + condition.getOperator() + " " + value;
				break;
			default:
				sql = column + " " + condition.getOperator() + " " + value;
				break;
		}

		if(condition.isNot())
		{
			return "NOT (" + sql + ")";
		}

		return sql;
	}
}
