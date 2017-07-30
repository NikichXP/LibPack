package com.nikichxp.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Collz {
	
	public static <T> T getOne (Collection<T> collection, Predicate<T> lambda) {
		return collection.stream().filter(lambda).findAny().orElse(null);
	}
	
	public static <T> T getOne (T[] array, Predicate<T> lambda) {
		for (T o : array) {
			if (lambda.test(o)) {
				return o;
			}
		}
		return null;
	}
	
	public static <T> T getOne (Enumeration<T> keys, Predicate<T> lambda) {
		T next;
		while ((next = keys.nextElement()) != null) {
			if (lambda.test(next)) {
				return next;
			}
		}
		return null;
	}
	
	public static <T> List<T> getList (Collection<T> collection, Predicate<T> lambda) {
		return collection.stream().filter(lambda).collect(Collectors.toList());
	}
	
	
}
