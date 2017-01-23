package ackern.core.hbase;

import java.io.IOException;

@FunctionalInterface
public interface TableOperation<T> {
	void accept(T t) throws IOException;
}
