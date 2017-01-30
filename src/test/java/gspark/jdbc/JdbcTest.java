package gspark.jdbc;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
			System.out.println(pool);
			Mockito.doNothing().when(mockJedis).close();
			Mockito.when(pool.getResource()).thenReturn(mockJedis);
		});
		redis.execute(jedis -> {
			System.out.println(jedis);
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
	
	public static void main(String[] args) {
		Jedis jedis = Mockito.mock(Jedis.class);
		System.out.println(jedis);
	}

}
