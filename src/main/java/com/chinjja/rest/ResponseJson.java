package com.chinjja.rest;

import com.google.gson.JsonElement;
import com.squareup.okhttp.Headers;

public class ResponseJson {
	public final int code;
	public final Headers headers;
	public final JsonElement entity;
	
	public ResponseJson(int code, Headers headers, JsonElement entity) {
		this.code = code;
		this.headers = headers;
		this.entity = entity;
	}
}
