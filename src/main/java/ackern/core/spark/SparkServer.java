package ackern.core.spark;

import java.util.function.Consumer;

import com.google.inject.Singleton;

import ackern.core.config.SparkConfig;
import spark.Service;

@Singleton
public class SparkServer {

	private Service inst;
	
	public SparkServer(SparkConfig config) {
		inst = Service.ignite();
		inst.ipAddress(config.getHost()).port(config.getPort()).threadPool(config.getThreads())
				.staticFileLocation(config.getStaticDir());
	}

	public SparkServer start(Consumer<Service> configFunc) {
		configFunc.accept(inst);
		return this;
	}

	public void stop() {
		inst.stop();
	}

}
