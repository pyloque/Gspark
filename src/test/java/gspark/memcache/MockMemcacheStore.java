package gspark.memcache;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gspark.core.memcache.MemcacheStore;
import net.rubyeye.xmemcached.MemcachedClient;

public class MockMemcacheStore extends MemcacheStore {
	private Map<String, String> kvs = new HashMap<>();

	public MockMemcacheStore() {
		super(mock(MemcachedClient.class));
		try {
			mockKv();
		} catch (Exception e) {
		}
	}

	public void mockKv() throws Exception {
		when(client.set(anyString(), anyInt(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(2);
			return kvs.put(key, value) == null;
		});
		when(client.get(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			return kvs.get(key);
		});
		when(client.get(anyCollection())).then(invocation -> {
			Collection<String> keys = invocation.getArgument(0);
			Map<String, String> result = new HashMap<>();
			for (String key : keys) {
				if (kvs.containsKey(key)) {
					result.put(key, kvs.get(key));
				}
			}
			return result;
		});
		when(client.add(anyString(), anyInt(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(2);
			if (kvs.containsKey(key)) {
				return false;
			}
			kvs.put(key, value);
			return true;
		});
		when(client.delete(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			return kvs.remove(key) != null;
		});
		when(client.touch(anyString(), anyInt())).then(invocation -> {
			String key = invocation.getArgument(0);
			return kvs.containsKey(key);
		});
	}

}
