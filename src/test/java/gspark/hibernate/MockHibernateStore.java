package gspark.hibernate;

import gspark.core.config.HibernateConfig;
import gspark.core.hibernate.HibernateStore;

public class MockHibernateStore extends HibernateStore {

	public MockHibernateStore(String name) {
		super(mockConfig(name));
	}

	public MockHibernateStore() {
		this("test");
	}

	private static HibernateConfig mockConfig(String name) {
		HibernateConfig config = new HibernateConfig();
		config.setDialect("org.hibernate.dialect.HSQLDialect");
		config.setDriver("org.hsqldb.jdbc.JDBCDriver");
		config.setAutoddl("auto");
		config.setMaxConns(5);
		config.setVerbose(true);
		config.setUri("jdbc:hsqldb:mem:" + name);
		return config;
	}

}
