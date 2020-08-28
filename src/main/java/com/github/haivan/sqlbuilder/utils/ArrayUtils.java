package com.github.haivan.sqlbuilder.utils;

import java.util.*;

public class ArrayUtils
{

	public static boolean isIteration(Object value)
	{
		if(isArray(value))
		{
			return true;
		}
		if(value instanceof Collection)
		{
			return true;
		}

		if(value instanceof Iterator)
		{
			return true;
		}
		if(value instanceof Iterable)
		{
			return true;
		}
		return false;
	}

	public static boolean isArray(Object value)
	{
		return value.getClass().isArray();
	}

	public static List<Object> convertObjectToList(Object o)
	{

		Class<?> objectClass = o.getClass();

		if(!objectClass.isArray())
		{
			throw new IllegalArgumentException("obj not array");
		}

		List<Object> result = new ArrayList<>();

		if(objectClass.getComponentType() == Float.TYPE)
		{
			float[] arr = (float[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Double.TYPE)
		{
			double[] arr = (double[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Short.TYPE)
		{
			short[] arr = (short[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Long.TYPE)
		{
			long[] arr = (long[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Byte.TYPE)
		{
			byte[] arr = (byte[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == String.class)
		{
			String[] arr = (String[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Double.class)
		{
			Double[] arr = (Double[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Float.class)
		{
			Float[] arr = (Float[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Integer.class)
		{
			Integer[] arr = (Integer[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Long.class)
		{
			Long[] arr = (Long[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Byte.class)
		{
			Byte[] arr = (Byte[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else if(objectClass.getComponentType() == Short.class)
		{
			Short[] arr = (Short[])o;
			for(int i = 0; i < arr.length; i++)
			{
				result.add(arr[i]);
			}
		}
		else
		{
			throw new IllegalArgumentException("only support primary type");
		}

		return result;
	}


	public static String joinArray(String str, Object it)
	{
		List<Object> result = flatten(it);

		return StringUtils.join(result, str);
	}

	private static void flatten(List<Object> result, Object obj)
	{
		if(obj instanceof Collection)
		{
			Iterator it = ((Collection)obj).iterator();

			while(it.hasNext())
			{
				flatten(result, it.next());
			}
		}
		else if(isArray(obj))
		{
			List<Object> arr = convertObjectToList(obj);

			for(int i = 0; i < arr.size(); i++)
			{
				flatten(result, arr.get(i));
			}
		}
		else if(obj instanceof Iterable)
		{
			Iterator it = ((Iterable)obj).iterator();

			while(it.hasNext())
			{
				flatten(result, it.next());
			}
		}
		else if(obj instanceof Iterator)
		{
			Iterator it = (Iterator)obj;

			while(it.hasNext())
			{
				flatten(result, it.next());
			}
		}
		else
		{
			result.add(obj);
		}
	}

	public static int iteratorCount(Iterator it)
	{
		int count = 0;
		while(it.hasNext())
		{
			it.next();
			count++;
		}
		return count;
	}

	public static List<Object> flatten(List<Object> bindings)
	{

		List<Object> result = new ArrayList<>();

		for(Object obj : bindings)
		{
			flatten(result, obj);
		}

		return result;
	}

	public static List<Object> flatten(Object[] bindings)
	{
		return flatten(Arrays.asList(bindings));
	}

	public static List<Object> flatten(Object value)
	{
		List<Object> result = new ArrayList<>();
		flatten(result, value);
		return result;
	}


}
