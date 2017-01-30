package gspark.redis;

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
import redis.clients.jedis.JedisPool;

public class RedisTest {
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
		MockRedis mockRedis = new MockRedis(redis);
		mockRedis.mockKv();
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
