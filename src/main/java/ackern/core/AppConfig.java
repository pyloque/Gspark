package ackern.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;

import ackern.core.config.CodisConfig;
import ackern.core.config.HBaseConfig;
import ackern.core.config.HibernateConfig;
import ackern.core.config.HttpClientConfig;
import ackern.core.config.MemcacheConfig;
import ackern.core.config.RabbitConfig;
import ackern.core.config.RedisClusterConfig;
import ackern.core.config.RedisConfig;
import ackern.core.config.SparkConfig;
import ackern.core.error.ConfigError;

public class AppConfig {

	public static AppEnv env = AppEnv.DEV;

	@JSONField(name = "spark")
	private SparkConfig spark = new SparkConfig();
	@JSONField(name = "redis")
	private Map<String, RedisConfig> redises = new HashMap<>();
	@JSONField(name = "codis")
	private Map<String, CodisConfig> codises = new HashMap<>();
	@JSONField(name = "redis-cluster")
	private Map<String, RedisClusterConfig> redisClusters = new HashMap<>();
	@JSONField(name = "memcache")
	private Map<String, MemcacheConfig> memcaches = new HashMap<>();
	@JSONField(name = "rabbitmq")
	private Map<String, RabbitConfig> rabbitmqs = new HashMap<>();
	@JSONField(name = "http-client")
	private Map<String, HttpClientConfig> httpClients = new HashMap<>();
	@JSONField(name = "hibernate")
	private Map<String, HibernateConfig> hibernates = new HashMap<>();
	@JSONField(name = "hbase")
	private Map<String, HBaseConfig> hbases = new HashMap<>();

	private static void settle() {
		String envName = System.getenv().getOrDefault("zhangyue.env", "dev");
		envName = System.getProperty("zhangyue.env", envName).toUpperCase();
		env = AppEnv.valueOf(envName);
	}

	public static AppConfig load() {
		settle();
		String content;
		try {
			content = new String(
					IOUtils.toByteArray(AppConfig.class
							.getResourceAsStream(String.format("/config/%s.js", env.name().toLowerCase()))),
					Charsets.UTF_8);
		} catch (IOException e) {
			throw new ConfigError("env config file content illegal", e);
		}
		return JSON.parseObject(content, AppConfig.class);
	}

	public SparkConfig getSpark() {
		return spark;
	}

	public Map<String, RedisConfig> getRedises() {
		return redises;
	}

	public Map<String, CodisConfig> getCodises() {
		return codises;
	}

	public Map<String, RedisClusterConfig> getRedisClusters() {
		return redisClusters;
	}

	public Map<String, MemcacheConfig> getMemcaches() {
		return memcaches;
	}

	public Map<String, RabbitConfig> getRabbitmqs() {
		return rabbitmqs;
	}

	public Map<String, HttpClientConfig> getHttpClients() {
		return httpClients;
	}

	public Map<String, HibernateConfig> getHibernates() {
		return hibernates;
	}

	public Map<String, HBaseConfig> getHbases() {
		return hbases;
	}

	public void setSpark(SparkConfig spark) {
		this.spark = spark;
	}

	public void setRedises(Map<String, RedisConfig> redises) {
		this.redises = redises;
	}

	public void setCodises(Map<String, CodisConfig> codises) {
		this.codises = codises;
	}

	public void setRedisClusters(Map<String, RedisClusterConfig> redisClusters) {
		this.redisClusters = redisClusters;
	}

	public void setMemcaches(Map<String, MemcacheConfig> memcaches) {
		this.memcaches = memcaches;
	}

	public void setRabbitmqs(Map<String, RabbitConfig> rabbitmqs) {
		this.rabbitmqs = rabbitmqs;
	}

	public void setHttpClients(Map<String, HttpClientConfig> httpClients) {
		this.httpClients = httpClients;
	}

	public void setHibernates(Map<String, HibernateConfig> hibernates) {
		this.hibernates = hibernates;
	}

	public void setHbases(Map<String, HBaseConfig> hbases) {
		this.hbases = hbases;
	}

	public static void main(String[] args) {
		AppConfig config = new AppConfig();
		config.redises.put("hello", new RedisConfig());
		config = JSON.parseObject(JSON.toJSONString(config), AppConfig.class);
		System.out.println(config);
	}

}
