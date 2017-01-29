package gspark.core.mongo;

import com.mongodb.MongoException;

@FunctionalInterface
public interface MongoOperation<T> {
	public void accept(T t) throws MongoException;
}
