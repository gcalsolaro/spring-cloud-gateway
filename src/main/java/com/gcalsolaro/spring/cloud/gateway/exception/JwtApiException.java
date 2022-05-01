package com.gcalsolaro.spring.cloud.gateway.exception;

import lombok.Getter;

@Getter
public class JwtApiException extends Exception {

	private static final long serialVersionUID = 1L;
	private int httpStatus = 200;
	private String errorCode;

	public JwtApiException() {
		super();
	}

	public JwtApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JwtApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public JwtApiException(String message, Throwable cause, int status) {
		super(message, cause);
		this.httpStatus = status;
	}

	public JwtApiException(String message) {
		super(message);
	}

	public JwtApiException(String message, String errorCode, int status) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = status;
	}

	public JwtApiException(String message, int status) {
		super(message);
		this.httpStatus = status;
	}

	public JwtApiException(Throwable cause) {
		super(cause);
	}

}
