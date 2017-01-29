package gspark.core.hbase;

import java.io.IOException;

@FunctionalInterface
public interface HBaseOperation<T> {
	void accept(T t) throws IOException;
}
