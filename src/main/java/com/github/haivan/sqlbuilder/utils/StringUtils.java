package com.github.haivan.sqlbuilder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils
{
	public static final String EMPTY = "";


	public static String join(Collection arr, String separator)
	{
		return join(arr.toArray(), separator);
	}

	public static String join(Object[] arr, String separator)
	{

		if(arr == null)
		{
			return null;
		}

		if(arr.length < 1)
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < arr.length; i++)
		{
			sb.append(arr[i].toString());
			if(i + 1 < arr.length)
			{
				sb.append(separator);
			}
		}

		return sb.toString();
	}

	public static boolean isBlank(String str)
	{
		int strLen;
		if(str == null || (strLen = str.length()) == 0)
		{
			return true;
		}
		for(int i = 0; i < strLen; i++)
		{
			if((!Character.isWhitespace(str.charAt(i))))
			{
				return false;
			}
		}
		return true;
	}


	public static String replaceAll(String str, String match
			, Function<Integer, String> fn)
	{

		if(isBlank(str) || !str.contains(match))
		{
			return str;
		}
		String[] splitted = str.split(toUnicode(escape(match)), -1);

		StringBuilder sb = new StringBuilder(splitted[0]);

		for(int i = 1; i < splitted.length; i++)
		{
			sb.append(fn.apply(i - 1));
			sb.append(splitted[i]);
		}

		return sb.toString();
	}

	public static String[] repeat(String str, int count)
	{
		String[] arr = new String[count];
		for(int i = 0; i < count; i++)
		{
			arr[i] = str;
		}
		return arr;
	}

	public static String expandParameters(String sql, String placeholder, Object[] bindings)
	{
		return replaceAll(sql, placeholder, i ->
		{
			Object parameter = bindings[i];
			List<Object> _parameter = ArrayUtils.flatten(parameter);
			int count = _parameter.size();
			return String.join(",", repeat(placeholder, count));
		});
	}

	public static List<String> expandExpression(String expression)
	{
		Pattern pattern = Pattern.compile("^(?:\\w+\\.){1,2}\\{(.*)\\}");

		Matcher matcher = pattern.matcher(expression);

		if(!matcher.matches())
		{
			return new ArrayList<String>()
			{{
				add(expression);
			}};
		}

		String table = expression.substring(0, expression.indexOf(".{"));

		String captures = matcher.group(1);

		List<String> cols = Arrays.stream(captures.split("\\s*,\\s*"))
				.map(x -> table + "." + x.trim()).collect(Collectors.toList());

		return cols;
	}

	public static String escape(String s)
	{
		return s.replace("\\", "\\\\")
				.replace("\t", "\\t")
				.replace("\b", "\\b")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\f", "\\f")
				.replace("\'", "\\'")
				.replace("\"", "\\\"");
	}

	public static String fromUnicode(String unicode)
	{
		String str = unicode.replace("\\", "");
		String[] arr = str.split("u");
		StringBuffer text = new StringBuffer();
		for(int i = 1; i < arr.length; i++)
		{
			int hexVal = Integer.parseInt(arr[i], 16);
			text.append(Character.toChars(hexVal));
		}
		return text.toString();
	}

	public static String toUnicode(String text)
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < text.length(); i++)
		{
			int codePoint = text.codePointAt(i);
			if(codePoint > 0xffff)
			{
				i++;
			}
			String hex = Integer.toHexString(codePoint);
			sb.append("\\u");
			for(int j = 0; j < 4 - hex.length(); j++)
			{
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static String replaceIdentifierUnlessEscaped(String input, String escapeCharacter, String identifier, String newIdentifier)
	{
		String nonEscapedReplace = input.replaceAll(toUnicode("(?<!" + escape(escapeCharacter) + ")" + escape(identifier)), newIdentifier);
		return nonEscapedReplace.replaceAll(toUnicode(escape(escapeCharacter) + escape(identifier)), identifier);
	}
}
