package com.nikichxp.util;

public class TelegramBotMethods {
	
	private static final String BASE_URL;
	private static String token = "";
	
	private static TelegramBotMethods instance = new TelegramBotMethods();
	
	static {
		BASE_URL = "https://api.telegram.org/bot" + token + "/";
//        async(() -> HttpSender.get(BASE_URL + "setWebhook?url=" + AppLoader.getProperty("telegram.bot.host")));
	}
	
	private TelegramBotMethods () {
	}
	
	/**
	 * @param value telegram token to be set
	 */
	public static void setToken (String value) {
		token = value;
	}
	
	public static String sendMessage (String chatId, String text) {
		return HttpClient.post(BASE_URL + "sendMessage", "chat_id", chatId, "text", text);
	}
	
}
