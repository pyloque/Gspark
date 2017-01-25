package ackern.core.config;

public class HibernateConfig {

	private String dialect = "org.hibernate.dialect.HSQLDialect";
	private String driver = "org.hsqldb.jdbc.JDBCDriver";
	private String uri = "jdbc:hsqldb:mem:test";
	private String username = "test";
	private String password = "test";
	private String autoddl = "create";
	private int maxConns = 5;
	private boolean verbose = false;

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

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

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

	public String getAutoddl() {
		return autoddl;
	}

	public void setAutoddl(String autoddl) {
		this.autoddl = autoddl;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public int getMaxConns() {
		return maxConns;
	}

	public void setMaxConns(int maxConns) {
		this.maxConns = maxConns;
	}

}
