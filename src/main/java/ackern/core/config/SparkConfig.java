package ackern.core.config;

public class SparkConfig {

	private String host = "0.0.0.0";
	private int port = 8088;
	private int threads = 16;
	private String staticDir = "static";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getStaticDir() {
		return staticDir;
	}

}
