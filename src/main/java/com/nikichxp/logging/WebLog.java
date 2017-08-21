package com.nikichxp.logging;

import com.nikichxp.util.HttpClient;
import org.springframework.stereotype.Component;

import static com.nikichxp.util.Async.async;

/**
 * Warning! Work in progress!
 */
@Component
public class WebLog {
	
	private static String reportUrl;
	private static String appName;
	
	private static int minLevelLog = 0;
	
	static {
		
	}
	
	public static void registerRemoteApp (String host) {
		if (reportUrl == null || appName == null) {
			throw new IllegalArgumentException("You must specify app name first");
		}
	}
	
	public static void setUrl (String url) {
		reportUrl = url;
	}
	
	public static void setAppName (String name) {
		appName = name;
	}
	
	public static void log (String message, int level) {
		async(() -> HttpClient.post(reportUrl, "app", appName, "message", message, "level", String.valueOf(level)));
	}
	
}
