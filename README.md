```java
package acdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import acdemo.api.ErrorApi;
import acdemo.api.HelloApi;
import acdemo.ui.HelloUI;
import gspark.core.AppConfig;
import gspark.core.error.KernError;
import gspark.core.spark.SparkServer;
import gspark.core.view.ApiError;

@Singleton
public class WebStarter {
	private final static Logger LOG = LoggerFactory.getLogger(WebStarter.class);

	@Inject
	private SparkServer webServer;
	@Inject
	private HelloApi helloApi;
	@Inject
	private HelloUI helloUi;
	@Inject
	private ErrorApi errorApi;

	public static void main(String[] args) {
		AppConfig config = AppConfig.load();
		Injector injector = Guice.createInjector(new DemoModule(config));
		WebStarter app = injector.getInstance(WebStarter.class);
		app.start();
	}

	public void start() {
		webServer.start(spark -> {
			spark.path("/api/hello", () -> {
				helloApi.setup(spark);
			});
			spark.path("/ui/hello", () -> {
				helloUi.setup(spark);
			});
			spark.exception(ApiError.class, (exc, req, res) -> {
				ApiError error = (ApiError) exc;
				res.body(errorApi.renderError(error));
			});
			spark.exception(KernError.class, (exc, req, res) -> {
				LOG.error("api error in server", exc);
				ApiError error = ApiError.newServerError(exc.getMessage());
				res.body(errorApi.renderError(error));
			});
		});
	}

}
```

```java
package acdemo;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import acdemo.panders.redis.RedisPingPander;
import acdemo.panders.redis.RedisPongPander;
import gspark.core.AppConfig;
import gspark.core.background.Boss;

@Singleton
public class BossStarter {
	@Inject
	private Boss boss;
	@Inject
	private RedisPingPander pingPander;
	@Inject
	private RedisPongPander pongPander;

	public static void main(String[] args) {
		AppConfig config = AppConfig.load();
		Injector injector = Guice.createInjector(new DemoModule(config));
		BossStarter boss = injector.getInstance(BossStarter.class);
		boss.start();
	}

	public void start() {
		boss.pander(pingPander).pander(pongPander).start();
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				boss.stop();
			}

		});
	}

}
```
