package com.nikichxp.google;

import com.nikichxp.util.JsonUtil;
import com.nikichxp.util.Ret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

//import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static com.nikichxp.util.Async.async;

/**
 * Warning! Work in progress!
 */
@RestController
@RequestMapping ("/api/google/auth")
public class GAuthAPI {
	
	private static final String SCOPE =
		"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive.file+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive.readonly+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fspreadsheets+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fspreadsheets.readonly";
	
	private static final String ACCESS_TYPE = "offline";
	private static final String PROMPT = "consent";
	private static final String RESPONSE_TYPE = "code";
	private static final String GRANT_TYPE_1 = "authorization_code";
	private static final String GRANT_TYPE_2 = "refresh_token";
	private static RestTemplate restTemplate = new RestTemplate();
	
	static {
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			@Override
			public boolean hasError (ClientHttpResponse clientHttpResponse) throws IOException {
				return false;
			}
			
			@Override
			public void handleError (ClientHttpResponse clientHttpResponse) throws IOException {
			}
		});
	}
	
	private String redirect_uri = "https://avant-html.herokuapp.com/api/google/auth/proceed";
	private String client_id;
	private String client_secret;
	private String refresh_token;
	private String access_token = null; //gets after stage #3. if null - cannot write to Gdisk
//	private ConfigRepository configRepo;
	
	@Autowired
	public GAuthAPI (ApplicationContext context/*, ConfigRepository configRepo*/) {
//		this.configRepo = configRepo;
		async(() -> {
			client_id = context.getEnvironment().getProperty("google.client.id");
			client_secret = System.getenv("google_client_secret");
			try {
//				refresh_token = configRepo.findOne("refresh_token").getValue();
				updateAccessToken();
			} catch (Exception e) {
				System.out.println("Cannot update token. Need re-auth.");
			}
			System.out.println("GSheets service started, " + (refresh_token != null) + " & " + (access_token != null));
		});
	}
	
	public String getAccessToken () {
		if (access_token == null) {
			try {
				updateAccessToken();
			} catch (Exception ignored) {
			}
		}
		return access_token;
	}
	
	@GetMapping ("/status")
	public ResponseEntity status () {
		return access_token == null ? Ret.code(401, "Failure") :
			refresh_token == null ? Ret.code(401, "refresh failure") : Ret.ok("OK");
	}
	
//	@GetMapping
//	public void auth (HttpServletResponse resp) throws IOException {
//		resp.sendRedirect("https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=" +
//			redirect_uri +
//			"&response_type=" +
//			RESPONSE_TYPE +
//			"&client_id=" +
//			client_id +
//			"&scope=" +
//			SCOPE +
//			"&access_type=" +
//			ACCESS_TYPE +
//			"&prompt=" +
//			PROMPT);
//	}
	
	@GetMapping ("/proceed")
	public ResponseEntity proceed (@RequestParam ("code") String code) throws IOException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("code", code);
		map.add("client_id", client_id);
		map.add("client_secret", client_secret);
		map.add("grant_type", GRANT_TYPE_1);
		map.add("redirect_uri", redirect_uri);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity("https://www.googleapis.com/oauth2/v4/token", request, String.class);
		System.out.println(response.getBody());
		
		if (response.getStatusCodeValue() == 200) {
			refresh_token = JsonUtil.of(response.getBody()).getX("refresh_token");
		}
		
		if (refresh_token != null) {
//			configRepo.save(new ElseAPI.ConfigPair("refresh_token", refresh_token));
			async(this::updateAccessToken);
		}
		return ResponseEntity.ok(response.getBody());
	}
	
	@Scheduled (cron = "0 0 * * * *")
	private void updateAccessToken () {
		if (refresh_token == null) {
			return;
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("refresh_token", refresh_token);
		map.add("client_id", client_id);
		map.add("client_secret", client_secret);
		map.add("grant_type", GRANT_TYPE_2);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		
		ResponseEntity<String> response = restTemplate.postForEntity("https://www.googleapis.com/oauth2/v4/token", request, String.class);
		access_token = JsonUtil.of(response.getBody()).getX("access_token");
		
		if (access_token != null) {
			System.out.println("Access token granted successfully");
		}
	}
}
