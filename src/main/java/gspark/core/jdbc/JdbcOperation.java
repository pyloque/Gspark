package gspark.core.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface JdbcOperation<T> {
	public void accept(T t) throws SQLException;
}
