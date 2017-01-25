package gspark.core.memcache;

import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.exception.MemcachedException;

@FunctionalInterface
public interface MemcacheOperation<T> {
	public void accept(T t) throws TimeoutException, InterruptedException, MemcachedException;
}
