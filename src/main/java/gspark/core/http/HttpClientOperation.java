package gspark.core.http;

import java.io.IOException;

@FunctionalInterface
public interface HttpClientOperation<T> {
	public void accept(T t) throws IOException;
}
