package com.nikichxp.google.sheets;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Data
@Builder
public class GSheetsAPIConfigurer {
	
	private String accessToken;
	private RestTemplate restTemplate = null;
	private ResponseErrorHandler errorHandler = new ResponseErrorHandler() {
		@Override
		public boolean hasError (ClientHttpResponse clientHttpResponse) throws IOException {
			return false;
		}
		
		@Override
		public void handleError (ClientHttpResponse clientHttpResponse) throws IOException {
			if (clientHttpResponse.getRawStatusCode() == 403) {
				System.out.println("Warning! Illegal permissions!");
			}
		}
	};
	private char rowStartChar = 'A';
	
	public GSheetsAPI api() {
		if (restTemplate == null) {
			restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(errorHandler);
		}
		return new GSheetsAPI(this);
	}
	
}
