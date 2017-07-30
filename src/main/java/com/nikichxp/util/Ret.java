package com.nikichxp.util;

import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Ret {
	
	public static ResponseEntity stMap (int code, Object... args) {
		return ResponseEntity.status(code).body(map(args));
	}
	
	public static ResponseEntity okMap (Object... args) {
		return ResponseEntity.ok(map(args));
	}
	
	public static Map<Object, Object> map (Object... args) {
		HashMap<Object, Object> ret = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			ret.put(args[i], args[i + 1]);
		}
		return ret;
	}
	
	public static Map<String, Object> st (Object... args) {
		HashMap<String, Object> ret = new HashMap<>();
		if (args.length == 1) {
			ret.put("status", args[0]);
		} else {
			ret.put("status", new Gson().toJson(args));
		}
		return ret;
	}
	
	public static ResponseEntity ok (Object... args) {
		return Ret.code(200, args);
	}
	
	/**
	 * If 1 arg - {'status':'args[0]'}, else - map {arg[0]:arg[1],...}
	 */
	public static ResponseEntity code (int code, Object... args) {
		if (args.length == 1) {
			return ResponseEntity.status(code).body(Ret.st(args));
		} else {
			return ResponseEntity.status(code).body(map(args));
		}
	}
	
	@SuppressWarnings ("unchecked")
	public static ResponseEntity notNull (Collection... args) {
		if (args == null || args.length == 0) {
			return code(402, "status", new Object[]{});
		}
		if (Arrays.stream(args).map(Collection::size).max(Integer::compareTo).orElse(0) == 0) {
			return code(402, "status", new Object[]{});
		}
		for (int i = 1; i < args.length; i++) {
			args[0].addAll(args[i]);
		}
		return ResponseEntity.ok(args[0]);
	}

//	public static ResponseEntity notNull (Object... args) {
//	}
}
