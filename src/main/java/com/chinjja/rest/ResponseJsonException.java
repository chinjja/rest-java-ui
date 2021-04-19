package com.chinjja.rest;

public class ResponseJsonException extends RuntimeException {
	public final ResponseJson response;
	
	public ResponseJsonException(ResponseJson response) {
		this.response = response;
	}
}
