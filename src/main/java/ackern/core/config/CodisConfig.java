package ackern.core.config;

import java.util.List;

public class CodisConfig {
	private List<String> uris;
	private int connectTimeout = 3000;
	private int soTimeout = 1000;

	public List<String> getUris() {
		return uris;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

}
