package gspark.memcache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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
import gspark.core.memcache.MemcacheStore;

public class MemcacheTest {
	private static Injector injector;

	@Inject
	@Named("test")
	private MemcacheStore store;

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
		assertThat(store.set("test-key", "test-value")).isTrue();
		assertThat(store.get("test-key")).isEqualTo("test-value");
		assertThat(store.add("test-key", "test-value1")).isFalse();
		assertThat(store.delete("test-key")).isTrue();
		assertThat(store.add("test-key", "test-value1")).isTrue();
		assertThat(store.get("test-key")).isEqualTo("test-value1");
		assertThat(store.delete("test-key")).isTrue();
		assertThat(store.set("test-key1", "test-value1")).isTrue();
		assertThat(store.set("test-key2", "test-value2")).isTrue();
		List<String> keys = new ArrayList<String>(2);
		keys.add("test-key1");
		keys.add("test-key2");
		keys.add("test-key3");
		assertThat(store.multiGet(keys)).containsEntry("test-key1", "test-value1")
				.containsEntry("test-key2", "test-value2").hasSize(2);
	}

	static class TestConfig extends AppConfig<TestConfig> {
	}

	static class TestModule extends GuiceModule<TestConfig> {

		public TestModule(TestConfig config) {
			super(config, TestConfig.class);
		}

		@Override
		public void bindMore() {
			MemcacheStore cache = new MockMemcacheStore();
			this.bind(MemcacheStore.class).annotatedWith(Names.named("test")).toInstance(cache);
		}

	}

	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(MemcacheTest.class);
		System.out.println(result.getRunCount());
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.getMessage());
			System.out.println(failure.getTrace());
		}
	}
}
