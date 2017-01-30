package gspark.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import gspark.core.AppConfig;
import gspark.core.AppEnv;
import gspark.core.GuiceModule;
import gspark.core.redis.RedisStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JdbcTest {
	private static Injector injector;

	@Inject
	@Named("test")
	private RedisStore redis;

	@BeforeClass
	public static void initialize() {
		TestConfig.env = AppEnv.TEST;
		TestConfig config = AppConfig.load(TestConfig.class, "{}");
		TestModule module = new TestModule(config);
		injector = Guice.createInjector(module);
	}

	@Before
	public void setUp() {
		injector.injectMembers(this);
	}

	@Test
	public void testRedis() {
		final Jedis mockJedis = Mockito.mock(Jedis.class);
		redis.mock(pool -> {
			Mockito.doNothing().when(mockJedis).close();
			Mockito.when(pool.getResource()).thenReturn(mockJedis);
			Map<String, String> kvs = new HashMap<>();
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
		});
		redis.execute(jedis -> {
			System.out.println(jedis.set("test-key", "test-value"));
			System.out.println(jedis.get("test-key"));
		});
	}

	static class TestConfig extends AppConfig<TestConfig> {
	}

	static class TestModule extends GuiceModule<TestConfig> {

		public TestModule(TestConfig config) {
			super(config, TestConfig.class);
		}

		@Override
		public void bindMore() {
			JedisPool pool = Mockito.mock(JedisPool.class);
			RedisStore redis = new RedisStore(pool);
			this.bind(RedisStore.class).annotatedWith(Names.named("test")).toInstance(redis);
		}

	}

}
