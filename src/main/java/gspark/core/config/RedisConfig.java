package gspark.core.config;

public class RedisConfig {

	private String uri = "redis://localhost:6379/0";
	private int connectTimeout = 3000;
	private int soTimeout = 1000;

	public String getUri() {
		return uri;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

}
