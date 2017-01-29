package gspark.core.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

import gspark.core.config.MongoConfig;
import gspark.core.error.MongoError;

public class MongoStore {
	private final static Logger LOG = LoggerFactory.getLogger(MongoStore.class);

	private MongoClient mongo;

	public MongoStore(MongoConfig config) {
		this.mongo = new MongoClient(config.getUri());
	}

	public void execute(String dbName, MongoOperation<MongoDatabase> op) {
		MongoDatabase db = mongo.getDatabase(dbName);
		try {
			op.accept(db);
		} catch (MongoException e) {
			LOG.error("mongo operation error", e);
			throw new MongoError("mongo operation error", e);
		}
	}

}
