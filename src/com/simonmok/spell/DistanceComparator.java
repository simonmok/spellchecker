package com.simonmok.spell;

import java.util.Comparator;
import java.util.Map;

public final class DistanceComparator implements Comparator<String> {
	
	private final Map<String, Integer> base;
	
	public DistanceComparator(final Map<String, Integer> base) {
		this.base = base;
	}

	public final int compare(final String string1, final String string2) {
		final int result = (base.get(string1)).compareTo(base.get(string2));
		return result == 0 ? string1.compareTo(string2) : result;
	}
}