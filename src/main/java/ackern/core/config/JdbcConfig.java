package ackern.core.config;

public class JdbcConfig {

	private String driver;
	private String uri;
	private String username;
	private String password;
	private int maxConns;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxConns() {
		return maxConns;
	}

	public void setMaxConns(int maxConns) {
		this.maxConns = maxConns;
	}

}
