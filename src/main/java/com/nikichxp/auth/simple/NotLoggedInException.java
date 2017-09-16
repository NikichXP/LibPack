package com.nikichxp.auth.simple;

public class NotLoggedInException extends RuntimeException {
	public NotLoggedInException (String s) {
		super(s);
	}
}
