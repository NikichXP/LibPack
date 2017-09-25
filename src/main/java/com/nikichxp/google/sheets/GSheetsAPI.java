package com.nikichxp.google.sheets;

import com.nikichxp.util.Json;
import com.nikichxp.util.Locks;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;

public class GSheetsAPI {
	
	private static RestTemplate restTemplate = new RestTemplate();
	private String accessToken;
	private char rowStartChar;
	
	public GSheetsAPI (GSheetsAPIConfigurer config) {
		this.accessToken = config.getAccessToken();
		this.rowStartChar = config.getRowStartChar();
	}
	
	/**
	 * Creates Google Sheets Spreadsheet (table)
	 *
	 * @param title  title
	 * @param sheets names of sheets. if length == 0 -> 1 default page (Page 1)
	 * @return spreadSheetId
	 */
	public ResponseEntity<String> createTable (String title, String... sheets) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("charset", "UTF-8");
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<byte[]> entity = new HttpEntity<>(
			Json.of(
				"properties", Json
					.of("title", title))
				.and("sheets", sheets)
				.toString().getBytes()
			, headers);
		
		return restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets?access_token=" + accessToken, entity, String.class);
	}
	
	public ResponseEntity<String> createPage (String sheetId, String... titles) {
		Locks.lock(sheetId);
		try {
			String requestJson = Json.arr("requests", Arrays.stream(titles).map(
				title ->
					Json.of("addSheet",
						Json.of("properties",
							Json.of("title", title)
						)
					)
				).toArray()
			).json();
			
			HttpHeaders headers = new HttpHeaders();
			headers.set("charset", "UTF-8");
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			@SuppressWarnings ("ConfusingArgumentToVarargsMethod")
			HttpEntity<byte[]> entity = new HttpEntity<>(requestJson.getBytes(), headers);
			
			return restTemplate.postForEntity(
				"https://sheets.googleapis.com/v4/spreadsheets/" + sheetId + "/values:batchUpdate" +
					"?access_token=" + accessToken,
				entity,
				String.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			Locks.unlock(sheetId);
		}
	}
	
	private ResponseEntity<String> writeToTable (String sheetId, String page, int row, String... data) {
		return writeToTable(sheetId, page + "!" + rowStartChar + row + ":" + (char) (rowStartChar + data.length - 1) + row,
			new String[][]{ data });
	}
	
	private synchronized ResponseEntity<String> writeToTable (String id, String range, String[][] data) {
		Json.JsonArr[] arrays = new Json.JsonArr[data.length];
		for (int i = 0; i < arrays.length; i++) {
			arrays[i] = new Json.JsonArr(data[i]);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("charset", "UTF-8");
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		@SuppressWarnings ("ConfusingArgumentToVarargsMethod")
		HttpEntity<byte[]> entity = new HttpEntity<>(Json.of("valueInputOption", "RAW")
			.and("data", Json.of(
				"range", range)
				.and("values", Json.arr(arrays))
			).json().getBytes(), headers);
		
		
		return restTemplate.postForEntity(
			"https://sheets.googleapis.com/v4/spreadsheets/" + id + "/values:batchUpdate" +
				"?access_token=" + accessToken,
			entity,
			String.class);
		
	}
	
}
