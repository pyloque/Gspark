package ackern.core.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private int code;
	private Map<String, String> headers = new HashMap<>();
	private String body;

	public HttpResponse(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public Map<String, String> headers() {
		return headers;
	}

	public HttpResponse header(String name, String value) {
		this.headers.put(name, value);
		return this;
	}

	public String body() {
		return body;
	}

	public void body(String body) {
		this.body = body;
	}

}
