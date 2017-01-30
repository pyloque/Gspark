package gspark.redis;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gspark.core.redis.RedisStore;
import redis.clients.jedis.Jedis;

public class MockRedis {

	private Map<String, String> kvs = new HashMap<>();
	private Jedis mockJedis;

	public MockRedis(RedisStore redis) {
		mockJedis = Mockito.mock(Jedis.class);
		redis.mock(pool -> {
			Mockito.doNothing().when(mockJedis).close();
			Mockito.when(pool.getResource()).thenReturn(mockJedis);
		});
	}

	public void mockKv() {
		Mockito.when(mockJedis.set(Mockito.anyString(), Mockito.anyString())).then(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgument(0);
				String value = invocation.getArgument(1);
				kvs.put(key, value);
				return "OK";
			}

		});
		Mockito.when(mockJedis.get(Mockito.anyString())).then(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgument(0);
				return kvs.get(key);
			}

		});
	}
}
