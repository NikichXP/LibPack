package com.nikichxp.auth.simple;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

public interface AuthRepo <T extends AuthToken> extends MongoRepository<T, String> {
	
	Stream<T> findByUser (AbstractUser user);
	Stream<T> findByUserId (String userId);
	Stream<T> findByTimeoutLessThan (long time);
	
}
