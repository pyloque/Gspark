package gspark.core.hibernate;

import java.util.function.Consumer;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gspark.core.config.HibernateConfig;
import gspark.core.error.HibernateError;

/**
 * 
 * 使用Java8的Consumer特性封装hibernate的session操作，避免忘记关闭session导致资源泄露
 *
 */
public class HibernateStore {
	private final static Logger LOG = LoggerFactory.getLogger(HibernateStore.class);

	private SessionFactory factory;
	private AnnotationConfiguration cfg = new AnnotationConfiguration();

	public HibernateStore(HibernateConfig config) {
		cfg.setProperty("hibernate.dialect", config.getDialect());
		cfg.setProperty("hibernate.connection.driver_class", config.getDriver());
		cfg.setProperty("hibernate.connection.url", config.getUri());
		cfg.setProperty("hibernate.hbm2ddl.auto", config.getAutoddl());
		cfg.setProperty("hibernate.connection.username", config.getUsername());
		cfg.setProperty("hibernate.connection.password", config.getPassword());
		cfg.setProperty("hibernate.show_sql", String.valueOf(config.isVerbose()));
		cfg.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
		cfg.setProperty("hibernate.c3p0.max_size", "" + config.getMaxConns());
		cfg.setProperty("hibernate.c3p0.min_size", "0");
		cfg.setProperty("hibernate.c3p0.acquire_increment", "1");
	}

	private SessionFactory factory() {
		if (factory == null) {
			factory = cfg.buildSessionFactory();
		}
		return factory;
	}

	public void close() {
		if (factory != null) {
			factory.close();
		}
	}

	public HibernateStore config(Consumer<AnnotationConfiguration> configFunc) {
		configFunc.accept(cfg);
		factory = cfg.buildSessionFactory();
		return this;
	}

	public HibernateStore execute(Consumer<Session> func) {
		Session session = factory().openSession();
		try {
			func.accept(session);
		} catch (HibernateException e) {
			LOG.error("hibernate operation error", e);
			throw new HibernateError("hibernate operation error", e);
		} finally {
			session.close();
		}
		return this;
	}

}