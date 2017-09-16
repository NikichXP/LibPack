package com.nikichxp.auth.simple;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
public class AuthToken<T extends AbstractUser> {
	
	@Id
	private String sessionId;
	private T user;
	private String userId;
	private long timeout;
	
	public AuthToken (T user) {
		this.sessionId = UUID.randomUUID().toString();
		this.user = user;
		this.userId = user.getId();
		this.timeout = System.currentTimeMillis() + 1_000_000_000L; //TODO TEST TIME
	}
	
	@Deprecated
	public String getId () {
		return sessionId;
	}
}
