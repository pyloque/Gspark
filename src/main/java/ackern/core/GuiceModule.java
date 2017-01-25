package ackern.core;

import java.util.Map.Entry;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import ackern.core.config.CodisConfig;
import ackern.core.config.HBaseConfig;
import ackern.core.config.HibernateConfig;
import ackern.core.config.HttpClientConfig;
import ackern.core.config.JdbcConfig;
import ackern.core.config.MemcacheConfig;
import ackern.core.config.RabbitConfig;
import ackern.core.config.RedisClusterConfig;
import ackern.core.config.RedisConfig;
import ackern.core.hbase.HBaseStore;
import ackern.core.hbase.HBaseStoreGroup;
import ackern.core.hibernate.HibernateGroup;
import ackern.core.hibernate.HibernateStore;
import ackern.core.http.HttpClient;
import ackern.core.http.HttpClientGroup;
import ackern.core.jdbc.JdbcGroup;
import ackern.core.jdbc.JdbcStore;
import ackern.core.memcache.MemcacheGroup;
import ackern.core.memcache.MemcacheStore;
import ackern.core.rabbitmq.RabbitGroup;
import ackern.core.rabbitmq.RabbitStore;
import ackern.core.redis.CodisStore;
import ackern.core.redis.RedisClusterStore;
import ackern.core.redis.RedisGroup;
import ackern.core.redis.RedisStore;
import ackern.core.spark.SparkServer;

public abstract class GuiceModule extends AbstractModule {

	private AppConfig config;

	public GuiceModule(AppConfig config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		bind(AppConfig.class).toInstance(config);
		this.bindSpark();
		this.bindRedis();
		this.bindMemcache();
		this.bindHttpClient();
		this.bindRabbitmq();
		this.bindHibernate();
		this.bindJdbc();
		this.bindHBase();
		this.onBindOk();
	}

	private void bindSpark() {
		SparkServer spark = new SparkServer(config.getSpark());
		bind(SparkServer.class).toInstance(spark);
	}

	private void bindHBase() {
		HBaseStoreGroup group = new HBaseStoreGroup();
		group.config(self -> {
			for (Entry<String, HBaseConfig> entry : config.getHbases().entrySet()) {
				self.register(entry.getKey(), new HBaseStore(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			HBaseStore store = group.get(name);
			bind(HBaseStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindHibernate() {
		HibernateGroup group = new HibernateGroup();
		group.config(self -> {
			for (Entry<String, HibernateConfig> entry : config.getHibernates().entrySet()) {
				self.register(entry.getKey(), new HibernateStore(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			HibernateStore store = group.get(name);
			bind(HibernateStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindJdbc() {
		JdbcGroup group = new JdbcGroup();
		group.config(self -> {
			for (Entry<String, JdbcConfig> entry : config.getJdbcs().entrySet()) {
				self.register(entry.getKey(), new JdbcStore(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			JdbcStore store = group.get(name);
			bind(JdbcStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindHttpClient() {
		HttpClientGroup group = new HttpClientGroup();
		group.config(self -> {
			for (Entry<String, HttpClientConfig> entry : config.getHttpClients().entrySet()) {
				self.register(entry.getKey(), new HttpClient(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			HttpClient client = group.get(name);
			bind(HttpClient.class).annotatedWith(Names.named(name)).toInstance(client);
		}
	}

	private void bindMemcache() {
		MemcacheGroup group = new MemcacheGroup();
		group.config(self -> {
			for (Entry<String, MemcacheConfig> entry : config.getMemcaches().entrySet()) {
				self.register(entry.getKey(), new MemcacheStore(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			MemcacheStore store = group.get(name);
			bind(MemcacheStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindRabbitmq() {
		RabbitGroup group = new RabbitGroup();
		group.config(self -> {
			for (Entry<String, RabbitConfig> entry : config.getRabbitmqs().entrySet()) {
				self.register(entry.getKey(), new RabbitStore(entry.getValue()));
			}
		});
		for (String name : group.names()) {
			RabbitStore store = group.get(name);
			bind(RabbitStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	private void bindRedis() {
		RedisGroup group = new RedisGroup();
		group.config(self -> {
			for (Entry<String, RedisConfig> entry : config.getRedises().entrySet()) {
				self.register(entry.getKey(), new RedisStore(entry.getValue()));
			}
		});
		for (String name : group.redisNames()) {
			RedisStore store = group.redis(name);
			bind(RedisStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
		group.config(self -> {
			for (Entry<String, CodisConfig> entry : config.getCodises().entrySet()) {
				self.register(entry.getKey(), new CodisStore(entry.getValue()));
			}
		});
		for (String name : group.codisNames()) {
			CodisStore store = group.codis(name);
			bind(CodisStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
		group.config(self -> {
			for (Entry<String, RedisClusterConfig> entry : config.getRedisClusters().entrySet()) {
				self.register(entry.getKey(), new RedisClusterStore(entry.getValue()));
			}
		});
		for (String name : group.clusterNames()) {
			RedisClusterStore store = group.cluster(name);
			bind(RedisClusterStore.class).annotatedWith(Names.named(name)).toInstance(store);
		}
	}

	public abstract void onBindOk();

}
