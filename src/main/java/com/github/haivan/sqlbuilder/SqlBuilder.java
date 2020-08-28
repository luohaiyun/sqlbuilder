package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.compilers.Compiler;
import com.github.haivan.sqlbuilder.compilers.MySqlCompiler;

public class SqlBuilder
{
	private Compiler compiler;

	private static final Compiler DEFAULT_COMPILER = new MySqlCompiler();

	public SqlBuilder()
	{
		this(DEFAULT_COMPILER);
	}

	public SqlBuilder(Compiler compiler)
	{
		this.compiler = compiler;
	}

	public SqlBuilder(String dialect)
	{

	}

	public Builder builder()
	{
		return new Builder(this.compiler);
	}

	public static Builder newBuilder()
	{
		return new Builder(DEFAULT_COMPILER);
	}


	public static class Builder extends Query<Builder>
	{
		private Compiler compiler;

		private Builder(Compiler compiler)
		{
			this.compiler = compiler;
		}

		public SqlResult<Builder> build()
		{
			SqlResult<Builder> ctx = compiler.compile(this);
			return ctx;
		}

		public String build(String dialect)
		{
			return null;
		}

		@Override
		public Builder newQuery()
		{
			return new Builder(compiler);
		}

		@Override
		public Builder clone()
		{
			return super.clone();
		}
	}

}
