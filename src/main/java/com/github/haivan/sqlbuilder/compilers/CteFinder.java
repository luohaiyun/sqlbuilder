package com.github.haivan.sqlbuilder.compilers;

import com.github.haivan.sqlbuilder.Query;
import com.github.haivan.sqlbuilder.clauses.from.AbstractFrom;
import com.github.haivan.sqlbuilder.clauses.from.QueryFromClause;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class CteFinder
{
	private final Query query;
	private final String dialect;
	private HashSet<String> namesOfPreviousCtes;
	private List<AbstractFrom> orderedCteList;

	public CteFinder(Query query, String dialect)
	{
		this.query = query;
		this.dialect = dialect;
	}

	public List<AbstractFrom> Find()
	{
		if(null != orderedCteList)
			return orderedCteList;

		namesOfPreviousCtes = new HashSet<>();

		orderedCteList = findInternal(query);

		namesOfPreviousCtes.clear();
		namesOfPreviousCtes = null;

		return orderedCteList;
	}

	private List<AbstractFrom> findInternal(Query queryToSearch)
	{
		List<AbstractFrom> cteList = queryToSearch.getComponents("cte", dialect);

		LinkedList<AbstractFrom> resultList = new LinkedList<>();

		for(AbstractFrom cte : cteList)
		{
			if(namesOfPreviousCtes.contains(cte.getAlias()))
				continue;

			namesOfPreviousCtes.add(cte.getAlias());
			resultList.add(cte);

			if(cte instanceof QueryFromClause)
			{
				QueryFromClause queryFromClause = (QueryFromClause)cte;

				resultList.addAll(0, findInternal(queryFromClause.getQuery()));
			}
		}

		return resultList;
	}
}
