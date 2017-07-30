package com.nikichxp.util;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class Async<T> {
	
	private CompletableFuture<T> result;
	
	private Async (Supplier<T> supplier) {
		this.result = CompletableFuture.supplyAsync(supplier);
	}
	
	public static <T> Async<T> of (Supplier<T> supplier) {
		return new Async<T>(supplier);
	}
	
	public static CompletableFuture async (Runnable task) {
		try {
			return CompletableFuture.runAsync(task);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T> CompletableFuture<T> async (Supplier<T> supplier) {
		try {
			return CompletableFuture.supplyAsync(supplier);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void hold (long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs a set of async tasks. If there are more tasks, than system can handle without problem - reduces
	 * number of parallel threads active.
	 *
	 * @param supplier varargs of runnable elements
	 */
	public static void gentleAsyncAll (Runnable... supplier) {
		LinkedList<CompletableFuture> list = new LinkedList<>();
		for (Runnable s : supplier) {
			list.add(CompletableFuture.runAsync(s));
		}
		CompletableFuture t;
		while ((t = list.poll()) != null) {
			t.join();
		}
	}
	
	/**
	 * Runs a set of async tasks, ignores the core count.
	 *
	 * @param supplier tasks to be done
	 */
	public static void asyncAll (Runnable... supplier) {
		LinkedList<Thread> list = new LinkedList<>();
		for (Runnable s : supplier) {
			Thread t = new Thread(s);
			t.start();
			list.add(t);
		}
		Thread t;
		while ((t = list.poll()) != null) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public T get () {
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
}
