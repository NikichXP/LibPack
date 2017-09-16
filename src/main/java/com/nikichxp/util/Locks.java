package com.nikichxp.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Contains multithreading locks.
 * Purpose: less lines of code.
 */
public class Locks {
	
	private static ConcurrentHashMap<Object, Semaphore> locks = new ConcurrentHashMap<>();
	
	public static Object get (Object key, int... defaultValue) {
		locks.putIfAbsent(key, new Semaphore((defaultValue.length == 0) ? 1 : defaultValue[0]));
		return locks.get(key);
	}
	
	public static void locked (String a, Runnable o) {
		synchronized (get(a)) {
			o.run();
		}
	}
	
	public void lock (Object key) {
		Semaphore s = locks.get(key);
		if (s == null) {
			s = locks.put(key, new Semaphore(1));
		}
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void lock (Object key, int defaultValue) {
		Semaphore s = locks.get(key);
		if (s == null) {
			s = locks.put(key, new Semaphore(defaultValue));
		}
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void unlock (Object key) {
		locks.get(key).release();
	}
	
}
