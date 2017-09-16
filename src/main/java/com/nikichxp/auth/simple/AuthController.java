package com.nikichxp.auth.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.nikichxp.util.Async.async;

@Component
public class AuthController<T extends AbstractUser> {
	
	private final MongoRepository<T, String> userRepo;
	private final AuthRepo<AuthToken<T>> authRepo;
	private ConcurrentHashMap<String, AuthToken<T>> activeSessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, T> activeUsers = new ConcurrentHashMap<>();
	
	private boolean exceptionSafe = false;
	private long tokenTimeOut = 3600 * 1000 * 24;
	
	@Autowired
	public AuthController (MongoRepository<T, String> userRepo, AuthRepo<AuthToken<T>> authRepo) {
		this.userRepo = userRepo;
		this.authRepo = authRepo;
		Logger.getGlobal().log(Level.INFO, "Auth loaded");
	}
	
	public AuthToken<T> auth (AuthReason reason) {
		T user = userRepo.findOne(reason.getUserid());
		if (user == null) {
			return fail("Reason " + reason.getHash() + " leads to no user.");
		}
		AuthToken<T> token = new AuthToken<>(user);
		put(token);
		return token;
	}
	
	public void logout (String token) {
		async(() -> delete(token));
	}
	
	public T getUser (String token) {
		T user = activeUsers.get(token);
		
		if (user == null) {
			if (get(token) == null) {
				return failUser("This session token (" + token + ") is invalid.");
			}
		}
		
		user = activeUsers.get(token);
		
		if (user != null) {
			AuthToken<T> tokenEntity = activeSessions.get(token);
			
			if (tokenEntity == null) {
				return failUser("This session token (" + token + ") is invalid.");
			}
			
			if (tokenEntity.getTimeout() < System.currentTimeMillis()) {
				async(this::cleanup);
				return null;
			}
		}
		
		return user;
	}
	
	public String renew (String token) {
		AuthToken authToken = getToken(token);
		if (authToken == null) {
			throw new NotLoggedInException("Wrong token");
		}
		authToken.setTimeout(System.currentTimeMillis() + tokenTimeOut);
		authRepo.save(authToken);
		return "Done";
	}
	
	public void logout (AbstractUser user) {
		async(() -> authRepo.findByUser(user).forEach(token -> authRepo.delete(token.getId())));
		async(
			() -> activeSessions.forEach(
				(string, token) -> {
					if (token.getUser().getId().equals(user.getId())) {
						activeSessions.remove(string);
					}
				}
			)
		);
	}
	
	public Collection<AuthToken<T>> getOnline () {
		return activeSessions.values();
	}
	
	public Collection<T> getOnlineUsers () {
		return activeUsers.values();
	}
	
	public AuthToken getToken (String token) {
		return get(token);
	}
	
	public boolean getExceptionSafe () {
		return exceptionSafe;
	}
	
	public void setExceptionSafe (boolean exceptionSafe) {
		this.exceptionSafe = exceptionSafe;
	}
	
	// Achtung: private action-methods below
		
	private AuthToken<T> fail (String reason) {
		if (!exceptionSafe) {
			throw new NotLoggedInException(reason); //"This session token (" + token + ") is invalid."
		}
		return null;
	}
	
	private T failUser (String reason) {
		if (!exceptionSafe) {
			throw new NotLoggedInException(reason); //"This session token (" + token + ") is invalid."
		}
		return null;
	}
	
	private void put (AuthToken<T> token) {
		async(() -> authRepo.save(token));
		activeSessions.put(token.getSessionId(), token);
		activeUsers.put(token.getSessionId(), token.getUser());
	}
	
	private AuthToken<T> get (String sessionId) {
		AuthToken<T> token = activeSessions.get(sessionId);
		if (token == null) {
			token = authRepo.findOne(sessionId);
			if (token != null) {
				activeSessions.put(sessionId, token);
				activeUsers.put(sessionId, token.getUser());
			}
		}
		if (token == null) {
			return null;
		}
		if (token.getTimeout() < System.currentTimeMillis()) {
			Logger.getGlobal().log(Level.WARNING, "Found token with timeout");
			async(this::cleanup);
			return null;
		}
		return token;
	}
	
	private void cleanup () {
		activeSessions.forEach((key, value) -> {
			if (value.getTimeout() < System.currentTimeMillis()) {
				activeSessions.remove(key);
			}
		});
		authRepo.findByTimeoutLessThan(System.currentTimeMillis())
			.filter(token -> token.getTimeout() < System.currentTimeMillis())
			.forEach(authRepo::delete);
	}
	
	private void delete (String token) {
		activeSessions.remove(token);
		authRepo.delete(token);
	}
	
	public boolean checkLogin (String token) {
		return get(token) != null;
	}
	
}
