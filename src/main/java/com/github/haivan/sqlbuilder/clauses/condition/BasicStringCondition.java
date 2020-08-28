package com.github.haivan.sqlbuilder.clauses.condition;

import com.github.haivan.sqlbuilder.clauses.AbstractClause;
import com.github.haivan.sqlbuilder.utils.StringUtils;

public class BasicStringCondition extends BasicCondition
{

	protected boolean caseSensitive;

	protected String escapeCharacter;

	public BasicStringCondition()
	{

	}

	public BasicStringCondition(String column, String operator, Object value, boolean caseSensitive, String escapeCharacter, boolean isNot, boolean isOr)
	{
		this.column = column;
		this.operator = operator;
		this.value = value;
		this.caseSensitive = caseSensitive;
		this.escapeCharacter = escapeCharacter;
		this.isNot = isNot;
		this.isOr = isOr;
	}

	public boolean isCaseSensitive()
	{
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
	}

	public String getEscapeCharacter()
	{
		return escapeCharacter;
	}

	public void setEscapeCharacter(String escapeCharacter)
	{
		if(StringUtils.isBlank(escapeCharacter))
			escapeCharacter = null;
		else if(escapeCharacter.length() > 1)
			throw new StringIndexOutOfBoundsException("The EscapeCharacter can only contain a single character!");
		this.escapeCharacter = escapeCharacter;
	}

	@Override
	public AbstractClause clone()
	{
		BasicStringCondition newBasicStringCondition = new BasicStringCondition();
		newBasicStringCondition.caseSensitive = caseSensitive;
		newBasicStringCondition.escapeCharacter = escapeCharacter;
		newBasicStringCondition.dialect = dialect;
		newBasicStringCondition.column = column;
		newBasicStringCondition.isNot = isNot;
		newBasicStringCondition.isOr = isOr;
		newBasicStringCondition.component = component;
		newBasicStringCondition.operator = operator;
		newBasicStringCondition.value = value;
		return newBasicStringCondition;
	}
}
