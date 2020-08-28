package com.github.haivan.sqlbuilder;

import com.github.haivan.sqlbuilder.compilers.OracleCompiler;
import com.github.haivan.sqlbuilder.compilers.PostgresCompiler;
import com.github.haivan.sqlbuilder.compilers.SqliteCompiler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SelectTest
{

	@Test
	public void testSelect(){
		List<String> param =  new ArrayList();
		param.add("xx");
		param.add("333");
		SqlBuilder.Builder builder = new SqlBuilder(new SqliteCompiler())
				.builder()
				.select(new String[]{"xxx"})
				.distinct()
				.from(new Query<>().from("aaa").select(new String[]{"xxx"}), "a")
				.orderBy("xxx")
				.groupBy("xxx")
				.offset(2)
				.limit(1)
				.where("x","x")
				.where("xx1", "<>", new String[]{"xx1","xx2"})
				.where("xx2", "<>", param)
				.where("xx3", "<>", new float[]{1,2})
				.whereIn("xxx", new float[]{1,2})
				.whereNotIn("xxx1", new Double[]{1.0,2.0})
				.innerJoin("joinTable", "aa", "bb", "=")
				.join(j -> {
					j.from("xxk").whereExists(new Query<>().from("aaa").select(new String[]{"xxx"}));
					return j;
				})
				.whereExists(SqlBuilder.newBuilder().from("xxx").select(new String[]{"xx1"}).where("x.x", "x1.xx"));

		String sql1 = builder.build().toString();
		String sql2 = builder.asCount().build().toString();

		System.err.println(sql1);
		System.err.println(sql2);
	}

}
