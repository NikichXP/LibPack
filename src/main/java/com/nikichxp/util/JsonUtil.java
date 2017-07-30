package com.nikichxp.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.util.Map;
import java.util.function.Consumer;

@Data
public class JsonUtil {
	
	public JsonObject json;
	
	public JsonUtil (String src) {
		json = new JsonParser().parse(src).getAsJsonObject();
	}
	
	public JsonUtil (JsonObject json) {
		this.json = json;
	}
	
	public static JsonUtil of (String json) {
		return new JsonUtil(json);
	}
	
	/**
	 * Get array index
	 *
	 * @param name member, like 'routes[0]'
	 * @return JsonObject of this
	 */
	public JsonUtil arr (String name) {
		JsonElement jse = json.get(
				name.substring(0, name.indexOf('[')))
				.getAsJsonArray()
				.get(Integer.parseInt(name.substring(name.indexOf('[') + 1, name.indexOf(']'))));
		
		return new JsonUtil(jse.getAsJsonObject());
	}
	
	public void forEach (Consumer<? super Map.Entry<String, JsonElement>> function) {
		json.entrySet().forEach(function);
	}
	
	public JsonUtil get (String name) {
		return new JsonUtil(json.getAsJsonObject(name));
	}
	
	public String getX (String name) {
		String[] part = name.split("\\.");
		JsonUtil ret = this;
		for (int i = 0; i < part.length - 1; i++) {
			if (part[i].contains("[")) {
				ret = ret.arr(part[i]);
			} else {
				ret = ret.get(part[i]);
			}
		}
		String r2 = ret.json.get(part[part.length - 1]).toString();
		if (r2.startsWith("\"") && r2.endsWith("\"")) {
			r2 = r2.substring(1, r2.length() - 1);
		}
		if (r2.startsWith("[") && r2.endsWith("]")) {
			r2 = r2.substring(1, r2.length() - 1);
		}
		return r2;
	}
	
	public double getD (String name) {
		return Double.parseDouble(getX(name));
	}
	
	@Override
	public String toString () {
		return json.toString();
	}
	
	
}
