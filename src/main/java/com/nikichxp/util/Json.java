package com.nikichxp.util;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;

public class Json {
	
	private HashMap<String, Object> data = new HashMap<>();
	
	public static Json of (String key, Object... value) {
		return new Json().and(key, value);
	}
	
	public Json and (String key, Object... value) {
		data.put(key, value.length == 1 ? value[0] : value.length == 0 ? "" : new JsonArr(value));
		return this;
	}
	
	public JsonArr arr(Object... value) {
		return new JsonArr(value);
	}
	
	public String json () {
		StringBuilder sb = new StringBuilder("{");
		data.forEach((key, value) -> {
			value = (value instanceof String) ? ("\"" + value.toString() + "\"") : value;
			sb.append("\"").append(key).append("\":").append(value.toString()).append(",");
		});
		return sb.substring(0, sb.length() - 1) + "}";
	}
	
	public String toString () {
		return json();
	}
	
	@AllArgsConstructor
	public static class JsonArr {
		private Object[] value;
		
		public String toString () {
			return "[" + Arrays.stream(value)
				.map(val -> val instanceof String ? "\"" + val + "\"" : val.toString())
				.reduce((s1, s2) -> s1 + ", " + s2)
				.orElse("") + "]";
		}
	}
}