package gspark.hibernate;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
import gspark.core.hibernate.HibernateStore;

public class HibernateTest {
	private static Injector injector;

	@Inject
	@Named("test")
	private HibernateStore store;

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
	public void testExpert() {
		store.execute(session -> {
			ExpertEntity expert = new ExpertEntity();
			expert.setRealName("qianwp");
			expert.setPasswordHash("wtf");
			expert.setUserName("qianwp");
			expert.setUpdateTs(System.currentTimeMillis());
			expert.setCreateTs(System.currentTimeMillis());
			session.save(expert);
			expert = (ExpertEntity) session.get(ExpertEntity.class, expert.getUserName());
			assertThat(expert.getPasswordHash()).isEqualTo("wtf");
		});
	}

	static class TestConfig extends AppConfig<TestConfig> {
	}

	@Entity
	@Table(name = "expert")
	class ExpertEntity {
		@Id
		@Column(name = "user_name")
		private String userName;
		@Column(name = "real_name")
		private String realName;
		@Column(name = "password_hash")
		private String passwordHash;
		@Column(name = "create_ts")
		private long createTs;
		@Column(name = "update_ts")
		private long updateTs;

		public ExpertEntity() {
		}

		public ExpertEntity(String userName, String realName, String passwordHash, long createTs, long updateTs) {
			this.userName = userName;
			this.realName = realName;
			this.passwordHash = passwordHash;
			this.createTs = createTs;
			this.updateTs = updateTs;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getRealName() {
			return realName;
		}

		public void setRealName(String realName) {
			this.realName = realName;
		}

		public String getPasswordHash() {
			return passwordHash;
		}

		public void setPasswordHash(String passwordHash) {
			this.passwordHash = passwordHash;
		}

		public long getCreateTs() {
			return createTs;
		}

		public void setCreateTs(long createTs) {
			this.createTs = createTs;
		}

		public long getUpdateTs() {
			return updateTs;
		}

		public void setUpdateTs(long updateTs) {
			this.updateTs = updateTs;
		}

	}

	static class TestModule extends GuiceModule<TestConfig> {

		public TestModule(TestConfig config) {
			super(config, TestConfig.class);
		}

		@Override
		public void bindMore() {
			HibernateStore store = new MockHibernateStore();
			store.config(cfg -> {
				cfg.addAnnotatedClass(ExpertEntity.class);
			});
			this.bind(HibernateStore.class).annotatedWith(Names.named("test")).toInstance(store);
		}

	}

	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(HibernateTest.class);
		System.out.println(result.getRunCount());
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.getMessage());
			System.out.println(failure.getTrace());
		}
	}
}
