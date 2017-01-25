package ackern.core.config;

import java.util.List;

public class RedisClusterConfig {
	private List<String> uris;
	private int maxRedirections = 5;
	private int connectTimeout = 3000;
	private int soTimeout = 1000;

	public List<String> getUris() {
		return uris;
	}

	public int getMaxRedirections() {
		return maxRedirections;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

}
