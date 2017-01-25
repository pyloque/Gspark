package ackern.core.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import ackern.core.error.HttpError;

public class HttpRequest {
	private final static Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

	private String url;
	private Map<String, List<String>> params = new LinkedHashMap<>();
	private Map<String, List<String>> bodies = new LinkedHashMap<>();
	private Map<String, String> headers = new HashMap<>(0);

	private HttpRequest(String url) {
		this.url = url;
	}

	public static HttpRequest url(String url) {
		return new HttpRequest(url);
	}

	public <T> HttpRequest param(String name, T value) {
		List<String> values = params.get(name);
		if (values == null) {
			values = new ArrayList<>();
			params.put(name, values);
		}
		values.add(String.valueOf(value));
		return this;
	}

	public <T> HttpRequest body(String name, T value) {
		List<String> values = bodies.get(name);
		if (values == null) {
			values = new ArrayList<>();
			bodies.put(name, values);
		}
		values.add(String.valueOf(value));
		return this;
	}

	public <T> HttpRequest header(String name, T value) {
		this.headers.put(name, String.valueOf(value));
		return this;
	}

	public HttpResponse get(HttpClient client) {
		String url = this.uriWithParams();
		HttpGet get = new HttpGet(url);
		for (Entry<String, String> entry : headers.entrySet()) {
			get.setHeader(entry.getKey(), entry.getValue());
		}
		CloseableHttpResponse res = null;
		try {
			res = client.execute(get);
		} catch (IOException e) {
			LOG.error("url get error url={}", url, e);
			throw new HttpError("url get error", e);
		}
		HttpResponse result = new HttpResponse(res.getStatusLine().getStatusCode());
		for (Header header : res.getAllHeaders()) {
			result.header(header.getName(), header.getValue());
		}
		result.body(parseResponse(res));
		return result;
	}

	public HttpResponse head(HttpClient client) {
		String url = this.uriWithParams();
		HttpHead head = new HttpHead(url);
		for (Entry<String, String> entry : headers.entrySet()) {
			head.setHeader(entry.getKey(), entry.getValue());
		}
		CloseableHttpResponse res = null;
		try {
			res = client.execute(head);
		} catch (IOException e) {
			LOG.error("url head error url={}", url, e);
			throw new HttpError("url head error", e);
		}
		HttpResponse result = new HttpResponse(res.getStatusLine().getStatusCode());
		for (Header header : res.getAllHeaders()) {
			result.header(header.getName(), header.getValue());
		}
		return result;
	}

	public HttpResponse post(HttpClient client) {
		String url = this.uriWithParams();
		HttpPost post = new HttpPost(url);
		for (Entry<String, String> entry : headers.entrySet()) {
			post.setHeader(entry.getKey(), entry.getValue());
		}
		CloseableHttpResponse res = null;
		try {
			res = client.execute(post);
		} catch (IOException e) {
			LOG.error("url post error url={}", url, e);
			throw new HttpError("url post error", e);
		}
		HttpResponse result = new HttpResponse(res.getStatusLine().getStatusCode());
		for (Header header : res.getAllHeaders()) {
			result.header(header.getName(), header.getValue());
		}
		result.body(parseResponse(res));
		return result;
	}

	public HttpResponse put(HttpClient client) {
		String url = this.uriWithParams();
		HttpPut put = new HttpPut(url);
		for (Entry<String, String> entry : headers.entrySet()) {
			put.setHeader(entry.getKey(), entry.getValue());
		}
		CloseableHttpResponse res = null;
		try {
			res = client.execute(put);
		} catch (IOException e) {
			LOG.error("url put error url={}", url, e);
			throw new HttpError("url put error", e);
		}
		HttpResponse result = new HttpResponse(res.getStatusLine().getStatusCode());
		for (Header header : res.getAllHeaders()) {
			result.header(header.getName(), header.getValue());
		}
		result.body(parseResponse(res));
		return result;
	}

	public HttpResponse delete(HttpClient client) {
		String url = this.uriWithParams();
		HttpDelete delete = new HttpDelete(url);
		for (Entry<String, String> entry : headers.entrySet()) {
			delete.setHeader(entry.getKey(), entry.getValue());
		}
		CloseableHttpResponse res = null;
		try {
			res = client.execute(delete);
		} catch (IOException e) {
			LOG.error("url delete error url={}", url, e);
			throw new HttpError("url delete error", e);
		}
		HttpResponse result = new HttpResponse(res.getStatusLine().getStatusCode());
		for (Header header : res.getAllHeaders()) {
			result.header(header.getName(), header.getValue());
		}
		result.body(parseResponse(res));
		return result;
	}

	private String parseResponse(CloseableHttpResponse res) {
		byte[] content = null;
		try {
			content = IOUtils.toByteArray(res.getEntity().getContent());
		} catch (UnsupportedOperationException | IOException e) {
			throw new HttpError("url read response error", e);
		}
		return new String(content, Charsets.UTF_8);
	}

	public String body() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Entry<String, List<String>> entry : bodies.entrySet()) {
			for (String value : entry.getValue()) {
				if (!first) {
					builder.append('&');
				}
				first = false;
				builder.append(entry.getKey());
				builder.append('=');
				builder.append(value);
			}
		}
		return builder.toString();
	}

	private String uriWithParams() {
		if (params.isEmpty()) {
			return url;
		}
		StringBuilder builder = new StringBuilder();
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new HttpError("url format illegal", e);
		}
		String q = uri.getQuery();
		builder.append(url);
		boolean first = true;
		if (q != null) {
			builder.append(q);
			first = false;
		} else {
			builder.append('?');
		}
		for (Entry<String, List<String>> entry : params.entrySet()) {
			for (String value : entry.getValue()) {
				if (!first) {
					builder.append('&');
				}
				first = false;
				builder.append(entry.getKey());
				builder.append('=');
				builder.append(value);
			}
		}
		return builder.toString();
	}

}
