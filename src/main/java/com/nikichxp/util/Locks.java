package com.nikichxp.util;

import java.util.concurrent.ConcurrentHashMap;

public class Locks {
	
	private static ConcurrentHashMap<Object, Object> locks = new ConcurrentHashMap<>();
	
	public static Object get (Object key) {
		locks.putIfAbsent(key, new Object());
		return locks.get(key);
	}
	
	public static void locked (String a, Runnable o) {
		synchronized (get(a)) {
			o.run();
		}
	}
	
}
