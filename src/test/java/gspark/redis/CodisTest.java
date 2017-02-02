package gspark.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import gspark.core.AppConfig;
import gspark.core.AppEnv;
import gspark.core.GuiceModule;
import gspark.core.codis.CodisStore;
import redis.clients.jedis.Tuple;

public class CodisTest {
	private static Injector injector;

	@Inject
	@Named("test")
	private CodisStore redis;

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
	public void testKv() {
		redis.execute(jedis -> {
			assertThat(jedis.set("test-key", "test-value")).isEqualTo("OK");
			assertThat(jedis.get("test-key")).isEqualTo("test-value");
			assertThat(jedis.del("test-key")).isEqualTo(1);
			assertThat(jedis.del("test-key")).isEqualTo(0);
			assertThat(jedis.incr("test-key")).isEqualTo(1);
			assertThat(jedis.incrBy("test-key", 3)).isEqualTo(4);
			assertThat(jedis.decrBy("test-key", 2)).isEqualTo(2);
			assertThat(jedis.decr("test-key")).isEqualTo(1);
			assertThat(jedis.get("test-key")).isEqualTo("1");
			assertThat(jedis.del("test-key")).isEqualTo(1);
			assertThat(jedis.del("test-key")).isEqualTo(0);
		});
	}

	@Test
	public void testHash() {
		redis.execute(jedis -> {
			assertThat(jedis.hset("test-hash", "test-name", "test-value")).isEqualTo(1);
			assertThat(jedis.hget("test-hash", "test-name")).isEqualTo("test-value");
			assertThat(jedis.hset("test-hash", "test-name1", "test-value1")).isEqualTo(1);
			assertThat(jedis.hget("test-hash", "test-name1")).isEqualTo("test-value1");
			assertThat(jedis.hset("test-hash", "test-name2", "test-value2")).isEqualTo(1);
			assertThat(jedis.hget("test-hash", "test-name2")).isEqualTo("test-value2");
			assertThat(jedis.hgetAll("test-hash")).containsKeys("test-name", "test-name1", "test-name2").hasSize(3);
			assertThat(jedis.hmget("test-hash", "test-name1", "test-name2")).contains("test-value1", "test-value2")
					.hasSize(2);
			assertThat(jedis.hlen("test-hash")).isEqualTo(3);
			assertThat(jedis.hkeys("test-hash")).contains("test-name", "test-name1", "test-name2").hasSize(3);
			assertThat(jedis.hvals("test-hash")).contains("test-value", "test-value1", "test-value2").hasSize(3);
			assertThat(jedis.del("test-hash")).isEqualTo(1);
		});
	}

	@Test
	public void testSet() {
		redis.execute(jedis -> {
			assertThat(jedis.sadd("test-set", "value1")).isEqualTo(1);
			assertThat(jedis.sadd("test-set", "value2")).isEqualTo(1);
			assertThat(jedis.sadd("test-set", "value3")).isEqualTo(1);
			assertThat(jedis.sadd("test-set", "value1")).isEqualTo(0);
			assertThat(jedis.smembers("test-set")).contains("value1", "value2", "value3").hasSize(3);
			assertThat(jedis.scard("test-set")).isEqualTo(3);
			for (int i = 0; i < 100; i++) {
				assertThat(jedis.srandmember("test-set")).isIn("value1", "value2", "value3");
			}
			assertThat(jedis.srem("test-set", "value1")).isEqualTo(1);
			assertThat(jedis.smembers("test-set")).contains("value2", "value3").hasSize(2);
			assertThat(jedis.scard("test-set")).isEqualTo(2);
			assertThat(jedis.del("test-set")).isEqualTo(1);
		});
	}

	@Test
	public void testList() {
		redis.execute(jedis -> {
			assertThat(jedis.rpush("test-list", "a", "b", "c", "d", "e", "f")).isEqualTo(6);
			assertThat(jedis.llen("test-list")).isEqualTo(6);
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("a", "b", "c", "d", "e", "f").hasSize(6);
			assertThat(jedis.lrange("test-list", 3, 4)).containsSequence("d", "e").hasSize(2);
			assertThat(jedis.lrange("test-list", -4, -2)).containsSequence("c", "d", "e").hasSize(3);
			assertThat(jedis.lrange("test-list", -4, 10)).containsSequence("c", "d", "e", "f").hasSize(4);
			assertThat(jedis.lrange("test-list", -10, 10)).containsSequence("a", "b", "c", "d", "e", "f").hasSize(6);
			assertThat(jedis.rpop("test-list")).isEqualTo("f");
			assertThat(jedis.llen("test-list")).isEqualTo(5);
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("a", "b", "c", "d", "e").hasSize(5);
			assertThat(jedis.lpop("test-list")).isEqualTo("a");
			assertThat(jedis.llen("test-list")).isEqualTo(4);
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("b", "c", "d", "e").hasSize(4);
			assertThat(jedis.rpoplpush("test-list", "test-list")).isEqualTo("e");
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("e", "b", "c", "d").hasSize(4);
			assertThat(jedis.lset("test-list", 1, "f")).isEqualTo("OK");
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("e", "f", "c", "d").hasSize(4);
			assertThat(jedis.del("test-list")).isEqualTo(1);
			assertThat(jedis.llen("test-list")).isEqualTo(0);
			assertThat(jedis.rpush("test-list", "a", "b", "c", "d", "e", "f")).isEqualTo(6);
			assertThat(jedis.ltrim("test-list", 4, 4)).isEqualTo("OK");
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("e").hasSize(1);
			assertThat(jedis.del("test-list")).isEqualTo(1);
			assertThat(jedis.rpush("test-list", "a", "b", "c", "d", "e", "f")).isEqualTo(6);
			assertThat(jedis.ltrim("test-list", 2, 4)).isEqualTo("OK");
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("c", "d", "e").hasSize(3);
			assertThat(jedis.del("test-list")).isEqualTo(1);
			assertThat(jedis.rpush("test-list", "a", "b", "c", "c", "c", "d")).isEqualTo(6);
			assertThat(jedis.lrem("test-list", 2, "c")).isEqualTo(2);
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("a", "b", "c", "d").hasSize(4);
			assertThat(jedis.lrem("test-list", 0, "c")).isEqualTo(1);
			assertThat(jedis.lrange("test-list", 0, -1)).containsSequence("a", "b", "d").hasSize(3);
			assertThat(jedis.del("test-list")).isEqualTo(1);
		});
	}

	@Test
	public void testZset() {
		redis.execute(jedis -> {
			assertThat(jedis.zcard("test-zset")).isEqualTo(0);
			assertThat(jedis.zadd("test-zset", 4.0, "d")).isEqualTo(1);
			Map<String, Double> entries = new HashMap<>();
			entries.put("b", 2.0);
			entries.put("c", 3.0);
			entries.put("a", 1.0);
			entries.put("e", 5.0);
			entries.put("f", 6.0);
			assertThat(jedis.zadd("test-zset", entries)).isEqualTo(5);
			assertThat(jedis.zcard("test-zset")).isEqualTo(6);

			assertThat(jedis.zrank("test-zset", "c")).isEqualTo(2);
			assertThat(jedis.zscore("test-zset", "c")).isEqualTo(3.0);
			assertThat(jedis.zrank("test-zset", "g")).isNull();
			assertThat(jedis.zscore("test-zset", "g")).isNull();

			assertThat(jedis.zrange("test-zset", 2, 4)).containsSequence("c", "d", "e").hasSize(3);
			assertThat(jedis.zrangeWithScores("test-zset", 2, 4))
					.containsSequence(new Tuple("c", 3.0), new Tuple("d", 4.0), new Tuple("e", 5.0)).hasSize(3);

			assertThat(jedis.zrevrange("test-zset", 2, 4)).containsSequence("d", "c", "b").hasSize(3);
			assertThat(jedis.zrevrangeWithScores("test-zset", 2, 4))
					.containsSequence(new Tuple("d", 4.0), new Tuple("c", 3.0), new Tuple("b", 2.0)).hasSize(3);

			assertThat(jedis.zrange("test-zset", 3, -1)).containsSequence("d", "e", "f").hasSize(3);
			assertThat(jedis.zrangeWithScores("test-zset", 3, -1))
					.containsSequence(new Tuple("d", 4.0), new Tuple("e", 5.0), new Tuple("f", 6.0)).hasSize(3);

			assertThat(jedis.zrevrange("test-zset", 3, -1)).containsSequence("c", "b", "a").hasSize(3);
			assertThat(jedis.zrevrangeWithScores("test-zset", 3, -1))
					.containsSequence(new Tuple("c", 3.0), new Tuple("b", 2.0), new Tuple("a", 1.0)).hasSize(3);

			assertThat(jedis.zrange("test-zset", -2, -1)).containsSequence("e", "f").hasSize(2);
			assertThat(jedis.zrangeWithScores("test-zset", -2, -1))
					.containsSequence(new Tuple("e", 5.0), new Tuple("f", 6.0)).hasSize(2);

			assertThat(jedis.zrevrange("test-zset", -2, -1)).containsSequence("b", "a").hasSize(2);
			assertThat(jedis.zrevrangeWithScores("test-zset", -2, -1))
					.containsSequence(new Tuple("b", 2.0), new Tuple("a", 1.0)).hasSize(2);

			assertThat(jedis.zrangeByScore("test-zset", 2.0, 4.0)).containsSequence("b", "c", "d").hasSize(3);
			assertThat(jedis.zrangeByScoreWithScores("test-zset", 2.0, 4.0))
					.containsSequence(new Tuple("b", 2.0), new Tuple("c", 3.0), new Tuple("d", 4.0)).hasSize(3);

			assertThat(jedis.zrevrangeByScore("test-zset", 2.0, 4.0)).containsSequence("d", "c", "b").hasSize(3);
			assertThat(jedis.zrevrangeByScoreWithScores("test-zset", 2.0, 4.0))
					.containsSequence(new Tuple("d", 4.0), new Tuple("c", 3.0), new Tuple("b", 2.0)).hasSize(3);

			assertThat(jedis.zrangeByScore("test-zset", "(2.0", "4.0")).containsSequence("c", "d").hasSize(2);
			assertThat(jedis.zrangeByScoreWithScores("test-zset", "(2.0", "4.0"))
					.containsSequence(new Tuple("c", 3.0), new Tuple("d", 4.0)).hasSize(2);

			assertThat(jedis.zrevrangeByScore("test-zset", "(2.0", "4.0")).containsSequence("d", "c").hasSize(2);
			assertThat(jedis.zrevrangeByScoreWithScores("test-zset", "(2.0", "4.0"))
					.containsSequence(new Tuple("d", 4.0), new Tuple("c", 3.0)).hasSize(2);

			assertThat(jedis.zrangeByScore("test-zset", "2.0", "(4.0")).containsSequence("b", "c").hasSize(2);
			assertThat(jedis.zrangeByScoreWithScores("test-zset", "2.0", "(4.0"))
					.containsSequence(new Tuple("b", 2.0), new Tuple("c", 3.0)).hasSize(2);

			assertThat(jedis.zrevrangeByScore("test-zset", "2.0", "(4.0")).containsSequence("c", "b").hasSize(2);
			assertThat(jedis.zrevrangeByScoreWithScores("test-zset", "2.0", "(4.0"))
					.containsSequence(new Tuple("c", 3.0), new Tuple("b", 2.0)).hasSize(2);

			assertThat(jedis.zrangeByScore("test-zset", "(2.0", "(4.0")).containsSequence("c").hasSize(1);
			assertThat(jedis.zrangeByScoreWithScores("test-zset", "(2.0", "(4.0")).containsSequence(new Tuple("c", 3.0))
					.hasSize(1);

			assertThat(jedis.zrevrangeByScore("test-zset", "(2.0", "(4.0")).containsSequence("c").hasSize(1);
			assertThat(jedis.zrevrangeByScoreWithScores("test-zset", "(2.0", "(4.0"))
					.containsSequence(new Tuple("c", 3.0)).hasSize(1);

			assertThat(jedis.zrangeByScore("test-zset", "-inf", "+inf")).containsSequence("a", "b", "c", "d", "e", "f")
					.hasSize(6);
			assertThat(jedis.zrangeByScoreWithScores("test-zset", "-inf", "+inf"))
					.containsSequence(new Tuple("a", 1.0), new Tuple("b", 2.0), new Tuple("c", 3.0),
							new Tuple("d", 4.0), new Tuple("e", 5.0), new Tuple("f", 6.0))
					.hasSize(6);

			assertThat(jedis.zrevrangeByScore("test-zset", "-inf", "+inf"))
					.containsSequence("f", "e", "d", "c", "b", "a").hasSize(6);
			assertThat(jedis.zrevrangeByScoreWithScores("test-zset", "-inf", "+inf"))
					.containsSequence(new Tuple("f", 6.0), new Tuple("e", 5.0), new Tuple("d", 4.0),
							new Tuple("c", 3.0), new Tuple("b", 2.0), new Tuple("a", 1.0))
					.hasSize(6);

			assertThat(jedis.zrem("test-zset", "a", "b", "c")).isEqualTo(3);
			assertThat(jedis.zcard("test-zset")).isEqualTo(3);

			assertThat(jedis.del("test-zset")).isEqualTo(1);
			assertThat(jedis.zcard("test-zset")).isEqualTo(0);

			entries = new HashMap<>();
			entries.put("a", 1.0);
			entries.put("b", 2.0);
			entries.put("c", 3.0);
			entries.put("d", 4.0);
			entries.put("e", 5.0);
			entries.put("f", 6.0);
			assertThat(jedis.zadd("test-zset", entries)).isEqualTo(6);
			assertThat(jedis.zremrangeByRank("test-zset", 2, 4)).isEqualTo(3);
			assertThat(jedis.zrange("test-zset", 0, -1)).containsSequence("a", "b", "f").hasSize(3);
			assertThat(jedis.del("test-zset")).isEqualTo(1);

			assertThat(jedis.zadd("test-zset", entries)).isEqualTo(6);
			assertThat(jedis.zremrangeByScore("test-zset", 2.0, 4.0)).isEqualTo(3);
			assertThat(jedis.zcard("test-zset")).isEqualTo(3);
			assertThat(jedis.del("test-zset")).isEqualTo(1);

			assertThat(jedis.zadd("test-zset", entries)).isEqualTo(6);
			assertThat(jedis.zremrangeByScore("test-zset", "(2.0", "4.0")).isEqualTo(2);
			assertThat(jedis.zcard("test-zset")).isEqualTo(4);
			assertThat(jedis.del("test-zset")).isEqualTo(1);

			assertThat(jedis.zadd("test-zset", entries)).isEqualTo(6);
			assertThat(jedis.zremrangeByScore("test-zset", "(2.0", "(4.0")).isEqualTo(1);
			assertThat(jedis.zcard("test-zset")).isEqualTo(5);
			assertThat(jedis.del("test-zset")).isEqualTo(1);
		});
	};

	static class TestConfig extends AppConfig<TestConfig> {
	}

	static class TestModule extends GuiceModule<TestConfig> {

		public TestModule(TestConfig config) {
			super(config, TestConfig.class);
		}

		@Override
		public void bindMore() {
			CodisStore redis = new MockCodisStore();
			this.bind(CodisStore.class).annotatedWith(Names.named("test")).toInstance(redis);
		}

	}

	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(CodisTest.class);
		System.out.println(result.getRunCount());
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.getMessage());
			System.out.println(failure.getTrace());
		}
	}

}
