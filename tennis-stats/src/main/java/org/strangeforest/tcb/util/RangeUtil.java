package org.strangeforest.tcb.util;

import com.google.common.collect.*;

public abstract class RangeUtil {

	public static <T extends Comparable<? super T>> Range<T> toRange(T from, T to) {
		if (from != null) {
			if (to != null)
				return from.compareTo(to) <= 0 ? Range.closed(from, to) : Range.closed(to, from);
			else
				return Range.atLeast(from);
		}
		else
			return to != null ? Range.atMost(to) : Range.all();
	}

	public static <T extends Comparable<? super T>> boolean isSingleton(Range<T> range) {
		return range.hasLowerBound() && range.equals(Range.singleton(range.lowerEndpoint()));
	}
}
