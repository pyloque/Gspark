package gspark.core.rabbitmq;

import java.io.IOException;

@FunctionalInterface
public interface RabbitOperation<T> {
	public void accept(T t) throws IOException;
}
