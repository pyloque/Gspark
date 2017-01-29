package gspark.core.jdbc;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import gspark.core.Holder;
import gspark.core.config.JdbcConfig;
import gspark.core.error.JdbcError;

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

	public void select(String sql, List<Object> params, Consumer<ResultSet> rsConsumer) {
		this.execute(conn -> {
			PreparedStatement stmt = null;
			ResultSet rst = null;
			try {
				stmt = conn.prepareStatement(sql);
				for (int i = 0; i < params.size(); i++) {
					Object o = params.get(i);
					stmt.setObject(i, o);
				}
				rst = stmt.executeQuery();
				rsConsumer.accept(rst);
			} finally {
				if (rst != null)
					rst.close();
				if (stmt != null)
					stmt.close();
			}
		});
	}

	public int execute(String sql, List<Object> params) {
		Holder<Integer> holder = new Holder<>();
		this.execute(conn -> {
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(sql);
				for (int i = 0; i < params.size(); i++) {
					Object o = params.get(i);
					stmt.setObject(i, o);
				}
				holder.set(stmt.executeUpdate());
			} finally {
				if (stmt != null)
					stmt.close();
			}
		});
		return holder.value();
	}

	public int[] batch(String sql, List<List<Object>> multiParams) {
		Holder<int[]> holder = new Holder<>();
		this.execute(conn -> {
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(sql);
				for (List<Object> params : multiParams) {
					for (int i = 0; i < params.size(); i++) {
						stmt.setObject(i, params.get(i));
					}
					stmt.addBatch();
				}
				holder.set(stmt.executeBatch());
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		});
		return holder.value();
	}

	public int[] batch(List<String> sqls) {
		Holder<int[]> holder = new Holder<>();
		this.execute(conn -> {
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				for (String sql : sqls) {
					stmt.addBatch(sql);
				}
				holder.set(stmt.executeBatch());
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		});
		return holder.value();
	}

}
