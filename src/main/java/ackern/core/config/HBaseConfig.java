package ackern.core.config;

public class HBaseConfig {

	private String zkAddrs = "localhost";
	private int zkPort = 2181;
	private int maxTableSize = 5;

	public String getZkAddrs() {
		return zkAddrs;
	}

	public void setZkAddrs(String zkAddrs) {
		this.zkAddrs = zkAddrs;
	}

	public int getZkPort() {
		return zkPort;
	}

	public void setZkPort(int zkPort) {
		this.zkPort = zkPort;
	}

	public int getMaxTableSize() {
		return maxTableSize;
	}

	public void setMaxTableSize(int maxTableSize) {
		this.maxTableSize = maxTableSize;
	}

}
