package ackern.core.jdbc;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import ackern.core.config.JdbcConfig;
import ackern.core.error.JdbcError;

public class JdbcStore {
	private final static Logger LOG = LoggerFactory.getLogger(JdbcStore.class);

	private ComboPooledDataSource db;

	public JdbcStore(JdbcConfig config) {
		db = new ComboPooledDataSource();
		db.setJdbcUrl(config.getUri());
		db.setUser(config.getUsername());
		db.setPassword(config.getPassword());
		db.setInitialPoolSize(0);
		db.setMinPoolSize(0);
		db.setAcquireIncrement(1);
		db.setMaxPoolSize(config.getMaxConns());
		try {
			db.setDriverClass(config.getDriver());
		} catch (PropertyVetoException e) {
			throw new JdbcError("driver class load error", e);
		}
	}

	public void execute(JdbcOperation<Connection> consumer) {
		Connection conn = null;
		try {
			conn = db.getConnection();
		} catch (SQLException e) {
			LOG.error("get jdbc connection error", e);
			throw new JdbcError("get jdbc connection error", e);
		}
		try {
			consumer.accept(conn);
		} catch (SQLException e) {
			LOG.error("jdbc operation error", e);
			throw new JdbcError("jdbc operation errror", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error("jdbc connection close error", e);
				}
			}
		}
	}

}
