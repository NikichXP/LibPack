package com.nikichxp.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClient {
	
	public static String get (String path) {
		return query(path, (String[]) null);
	}
	
	/**
	 * Sends HTTP(S) request
	 *
	 * @param path - URL
	 * @param args - args for POST, map. null for get-method
	 * @return strung got via connection
	 */
	public static String query (String path, Map<String, String> args) {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpRequestBase arq;
		try {
			if (args != null) { //post
				HttpPost post = new HttpPost(path);
				List<NameValuePair> urlParameters = new ArrayList<>();
				args.forEach((k, v) -> urlParameters.add(new BasicNameValuePair(k, v)));
				post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
				arq = post;
			} else { //get
				arq = new HttpGet(path);
			}
//			arq.addHeader("charset", "utf-8");
			arq.setHeader("charset", "utf-8");
			CloseableHttpResponse response = client.execute(arq);
			
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
			
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Sends HTTP(S) request
	 *
	 * @param path - URL
	 * @param args - args for POST (key, value, key2, value2...), null for GET-method
	 * @return strung got via connection
	 */
	public static String query (String path, String[] args) {
		HashMap<String, String> map = null;
		if (args != null) {
			map = new HashMap<>();
			for (int i = 0; i < args.length / 2; i++) {
				map.put(args[i * 2], args[i * 2 + 1]);
			}
		}
		return query(path, map);
	}
	
	public static String post (String path, String... args) {
		return query(path, args);
	}
	
}
