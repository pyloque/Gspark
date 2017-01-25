package ackern.core.config;

public class MemcacheConfig {

	private String addrs;
	private int poolSize = 5;
	private int connectTimeout = 3000;
	private int soTimeout = 1000;
	private int healSessionInterval = 100000;

	public String getAddrs() {
		return addrs;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public int getHealSessionInterval() {
		return healSessionInterval;
	}

}
