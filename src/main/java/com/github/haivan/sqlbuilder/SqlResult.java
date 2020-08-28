package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.utils.StringUtils;
import com.github.haivan.sqlbuilder.utils.ArrayUtils;
import com.github.haivan.sqlbuilder.utils.DateUtils;

import java.math.BigDecimal;
import java.util.*;

public class SqlResult<T extends BaseQuery<T>>
{
	private T query;

	private String rawSql;

	private List<Object> bindings = new ArrayList();

	private String sql;

	private Map<String, Object> namedBindings = new HashMap<>();

	public SqlResult()
	{
	}

	public SqlResult(T query)
	{
		this.query = query;
	}

	public T getQuery()
	{
		return query;
	}

	public void setQuery(T query)
	{
		this.query = query;
	}

	public String getRawSql()
	{
		return rawSql;
	}

	public void setRawSql(String rawSql)
	{
		this.rawSql = rawSql;
	}

	public List<Object> getBindings()
	{
		return bindings;
	}

	public void setBindings(List<Object> bindings)
	{
		this.bindings = bindings;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	public Map<String, Object> getNamedBindings()
	{
		return namedBindings;
	}

	public void setNamedBindings(Map<String, Object> namedBindings)
	{
		this.namedBindings = namedBindings;
	}

	@Override
	public String toString()
	{
		List<Object> deepParameters = ArrayUtils.flatten(bindings);

		return StringUtils.replaceAll(rawSql, "?", index -> {
			if(index >= deepParameters.size())
			{
				throw new RuntimeException(
						"Failed to retrieve a binding at the index " + index + ", the total bindings count is " + bindings.size());
			}
			Object value = deepParameters.get(index);
			return changeToSqlValue(value);
		});

	}

	private static final Class[] NUMBER_TYPES =
			{
					int.class,
					long.class,
					BigDecimal.class,
					double.class,
					float.class,
					short.class,
					Integer.class,
					Long.class,
					Double.class,
					Short.class,
					Float.class
			};


	private String changeToSqlValue(Object value)
	{
   		if(value == null)
		{
			return "NULL";
		}

		if(ArrayUtils.isIteration(value))
		{
			return ArrayUtils.joinArray(",", value);
		}

		if(Arrays.stream(NUMBER_TYPES).anyMatch(t -> t.equals(value.getClass())))
		{
			return value.toString();
		}

		if(value.getClass().isAssignableFrom(Date.class))
		{
			Date date = (Date)value;

			return "'" + DateUtils.format(date) + "'";
		}

		if(value.getClass().isAssignableFrom(Boolean.class))
		{
			boolean vBool = (boolean)value;
			return vBool ? "true" : "false";
		}

		// fallback to string
		return "'" + value.toString() + "'";

	}
}
