package org.finlayfamily.littlefamily.util;

import java.util.*;

public class ValueComparator<T> implements Comparator<T>
{

	private Map<T,? extends Comparable> map;

	public void setMap(Map<T, ? extends Comparable> map)
	{
		this.map = map;
	}

	public Map<T, ? extends Comparable> getMap()
	{
		return map;
	}
	
	@Override
	public int compare(T p1, T p2)
	{
		Comparable c1 = map.get(p1);
		Comparable c2 = map.get(p2);
		if (c1!=null & c2!=null) return c1.compareTo(c2);
		return 0;
	}

	
}
