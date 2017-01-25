package ackern.core.memcache;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ackern.core.Holder;
import ackern.core.config.MemcacheConfig;
import ackern.core.error.MemcacheError;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MemcacheStore {
	private final static Logger LOG = LoggerFactory.getLogger(MemcacheStore.class);

	private MemcachedClientBuilder builder;
	private MemcachedClient client;

	public MemcacheStore(MemcacheConfig config) {
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(config.getAddrs()));
		builder.setConnectionPoolSize(config.getPoolSize()); // 链接池
		builder.setSessionLocator(new KetamaMemcachedSessionLocator()); // ketama算法计算hash
		builder.setConnectTimeout(config.getConnectTimeout());
		builder.setOpTimeout(config.getSoTimeout());
		builder.setHealSessionInterval(config.getHealSessionInterval()); // 10秒重试链接恢复
		this.builder = builder;
	}

	public MemcacheStore connectionPoolSize(int size) {
		this.builder.setConnectionPoolSize(size);
		return this;
	}

	public MemcacheStore opTimeout(int timeoutInMillis) {
		this.builder.setOpTimeout(timeoutInMillis);
		return this;
	}

	public MemcacheStore connectTimeout(int timeoutInMillis) {
		this.builder.setConnectTimeout(timeoutInMillis);
		return this;
	}

	public MemcacheStore healSessionInterval(int intervalInMillis) {
		this.builder.setHealSessionInterval(intervalInMillis);
		return this;
	}

	private MemcachedClient client() {
		if (client == null) {
			try {
				this.client = builder.build();
			} catch (IOException e) {
				LOG.error("build memcache client error", e);
				throw new MemcacheError("build memcache client error", e);
			}
		}
		return this.client;
	}

	public void execute(MemcacheOperation<MemcachedClient> op) {
		MemcachedClient client = this.client();
		try {
			op.accept(client);
		} catch (TimeoutException | InterruptedException | MemcachedException e) {
			LOG.error("memcache client operation error", e);
			throw new MemcacheError("memcache client operation error", e);
		}
	}

	public String get(String key) {
		Holder<String> result = new Holder<>();
		this.execute(client -> {
			result.set(client.get(key));
		});
		return result.value();
	}

	public Map<String, String> multiGet(Collection<String> keys) {
		Holder<Map<String, String>> result = new Holder<>();
		this.execute(client -> {
			result.set(client.get(keys));
		});
		return result.value();
	}

	public boolean set(String key, int exp, String value) {
		Holder<Boolean> result = new Holder<>();
		this.execute(client -> {
			result.set(client.set(key, exp, value));
		});
		return result.value();
	}

	public boolean set(String key, String value) {
		return this.set(key, 0, value);
	}

	public boolean add(String key, int exp, String value) {
		Holder<Boolean> result = new Holder<>();
		this.execute(client -> {
			result.set(client.add(key, exp, value));
		});
		return result.value();
	}

	public boolean add(String key, String value) {
		return this.add(key, 0, value);
	}

	public boolean delete(String key) {
		Holder<Boolean> result = new Holder<>();
		this.execute(client -> {
			result.set(client.delete(key));
		});
		return result.value();
	}

	public boolean touch(String key, int exp) {
		Holder<Boolean> result = new Holder<>();
		this.execute(client -> {
			result.set(client.touch(key, exp));
		});
		return result.value();
	}

}
