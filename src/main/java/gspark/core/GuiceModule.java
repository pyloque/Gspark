package gspark.core;

import java.util.Map.Entry;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import gspark.core.codis.CodisGroup;
import gspark.core.codis.CodisStore;
import gspark.core.config.CodisConfig;
import gspark.core.config.HBaseConfig;
import gspark.core.config.HibernateConfig;
import gspark.core.config.HttpClientConfig;
import gspark.core.config.JdbcConfig;
import gspark.core.config.MemcacheConfig;
import gspark.core.config.MongoConfig;
import gspark.core.config.RabbitConfig;
import gspark.core.config.RedisClusterConfig;
import gspark.core.config.RedisConfig;
import gspark.core.hbase.HBaseStore;
import gspark.core.hbase.HBaseStoreGroup;
import gspark.core.hibernate.HibernateGroup;
import gspark.core.hibernate.HibernateStore;
import gspark.core.http.HttpClient;
import gspark.core.http.HttpClientGroup;
import gspark.core.jdbc.JdbcGroup;
import gspark.core.jdbc.JdbcStore;
import gspark.core.memcache.MemcacheGroup;
import gspark.core.memcache.MemcacheStore;
import gspark.core.mongo.MongoGroup;
import gspark.core.mongo.MongoStore;
import gspark.core.rabbitmq.RabbitGroup;
import gspark.core.rabbitmq.RabbitStore;
import gspark.core.redis.RedisGroup;
import gspark.core.redis.RedisStore;
import gspark.core.rediscluster.RedisClusterGroup;
import gspark.core.rediscluster.RedisClusterStore;
import gspark.core.spark.SparkServer;

public abstract class GuiceModule<T extends AppConfig<T>> extends AbstractModule {

	protected T config;
	protected Class<T> configClass;
	protected SparkServer sparkServer;
	protected HBaseStoreGroup hbaseGroup;
	protected HibernateGroup hibernateGroup;
	protected JdbcGroup jdbcGroup;
	protected HttpClientGroup httpClientGroup;
	protected MemcacheGroup memcacheGroup;
	protected RabbitGroup rabbitGroup;
	protected RedisGroup redisGroup;
	protected CodisGroup codisGroup;
	protected RedisClusterGroup redisClusterGroup;
	protected MongoGroup mongoGroup;

	public GuiceModule(T config, Class<T> configClass) {
		this.config = config;
		this.configClass = configClass;
	}

	@Override
	protected void configure() {
		bind(configClass).toInstance(config);
		this.bindSpark();
		this.bindRedis();
		this.bindCodis();
		this.bindRedisCluster();
		this.bindMemcache();
		this.bindHttpClient();
		this.bindRabbitmq();
		this.bindHibernate();
		this.bindJdbc();
		this.bindHBase();
		this.bindMongo();
		this.bindMore();
	}

	private void bindSpark() {
		sparkServer = new SparkServer(config.getSpark());
		bind(SparkServer.class).toInstance(sparkServer);
	}

	private void bindHBase() {
		hbaseGroup = new HBaseStoreGroup();
		hbaseGroup.config(self -> {
			for (Entry<String, HBaseConfig> entry : config.getHbases().entrySet()) {
				self.register(entry.getKey(), new HBaseStore(entry.getValue()));
			}
		});
		for (String name : hbaseGroup.names()) {
			HBaseStore store = hbaseGroup.get(name);
			bind(HBaseStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindHibernate() {
		hibernateGroup = new HibernateGroup();
		hibernateGroup.config(self -> {
			for (Entry<String, HibernateConfig> entry : config.getHibernates().entrySet()) {
				self.register(entry.getKey(), new HibernateStore(entry.getValue()));
			}
		});
		for (String name : hibernateGroup.names()) {
			HibernateStore store = hibernateGroup.get(name);
			bind(HibernateStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindJdbc() {
		jdbcGroup = new JdbcGroup();
		jdbcGroup.config(self -> {
			for (Entry<String, JdbcConfig> entry : config.getJdbcs().entrySet()) {
				self.register(entry.getKey(), new JdbcStore(entry.getValue()));
			}
		});
		for (String name : jdbcGroup.names()) {
			JdbcStore store = jdbcGroup.get(name);
			bind(JdbcStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindHttpClient() {
		httpClientGroup = new HttpClientGroup();
		httpClientGroup.config(self -> {
			for (Entry<String, HttpClientConfig> entry : config.getHttpClients().entrySet()) {
				self.register(entry.getKey(), new HttpClient(entry.getValue()));
			}
		});
		for (String name : httpClientGroup.names()) {
			HttpClient client = httpClientGroup.get(name);
			bind(HttpClient.class).annotatedWith(Names.named(name)).toInstance(client);
		}
	}

	private void bindMemcache() {
		memcacheGroup = new MemcacheGroup();
		memcacheGroup.config(self -> {
			for (Entry<String, MemcacheConfig> entry : config.getMemcaches().entrySet()) {
				self.register(entry.getKey(), new MemcacheStore(entry.getValue()));
			}
		});
		for (String name : memcacheGroup.names()) {
			MemcacheStore store = memcacheGroup.get(name);
			bind(MemcacheStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindMongo() {
		mongoGroup = new MongoGroup();
		mongoGroup.config(self -> {
			for (Entry<String, MongoConfig> entry : config.getMongoes().entrySet()) {
				self.register(entry.getKey(), new MongoStore(entry.getValue()));
			}
		});
		for (String name : mongoGroup.names()) {
			MongoStore store = mongoGroup.get(name);
			bind(MongoStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindRabbitmq() {
		rabbitGroup = new RabbitGroup();
		rabbitGroup.config(self -> {
			for (Entry<String, RabbitConfig> entry : config.getRabbitmqs().entrySet()) {
				self.register(entry.getKey(), new RabbitStore(entry.getValue()));
			}
		});
		for (String name : rabbitGroup.names()) {
			RabbitStore store = rabbitGroup.get(name);
			bind(RabbitStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindRedis() {
		redisGroup = new RedisGroup();
		redisGroup.config(self -> {
			for (Entry<String, RedisConfig> entry : config.getRedises().entrySet()) {
				self.register(entry.getKey(), new RedisStore(entry.getValue()));
			}
		});
		for (String name : redisGroup.names()) {
			RedisStore store = redisGroup.redis(name);
			bind(RedisStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindCodis() {
		codisGroup = new CodisGroup();
		codisGroup.config(self -> {
			for (Entry<String, CodisConfig> entry : config.getCodises().entrySet()) {
				self.register(entry.getKey(), new CodisStore(entry.getValue()));
			}
		});
		for (String name : codisGroup.names()) {
			CodisStore store = codisGroup.get(name);
			bind(CodisStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindRedisCluster() {
		redisClusterGroup = new RedisClusterGroup();
		redisClusterGroup.config(self -> {
			for (Entry<String, RedisClusterConfig> entry : config.getRedisClusters().entrySet()) {
				self.register(entry.getKey(), new RedisClusterStore(entry.getValue()));
			}
		});
		for (String name : redisClusterGroup.names()) {
			RedisClusterStore store = redisClusterGroup.get(name);
			bind(RedisClusterStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	public abstract void bindMore();

}
