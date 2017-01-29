package gspark.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;

import gspark.core.config.CodisConfig;
import gspark.core.config.HBaseConfig;
import gspark.core.config.HibernateConfig;
import gspark.core.config.HttpClientConfig;
import gspark.core.config.JdbcConfig;
import gspark.core.config.MemcacheConfig;
import gspark.core.config.MongoConfig;
import gspark.core.config.RabbitConfig;
import gspark.core.config.RedisClusterConfig;
import gspark.core.config.RedisConfig;
import gspark.core.config.SparkConfig;
import gspark.core.error.ConfigError;

public class AppConfig<T extends AppConfig<T>> {

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
	@JSONField(name = "jdbc")
	private Map<String, JdbcConfig> jdbcs = new HashMap<>();
	@JSONField(name = "hbase")
	private Map<String, HBaseConfig> hbases = new HashMap<>();
	@JSONField(name = "mongo")
	private Map<String, MongoConfig> mongoes = new HashMap<>();

	private static void settle() {
		String envName = System.getenv().getOrDefault("zhangyue.env", "dev");
		envName = System.getProperty("zhangyue.env", envName).toUpperCase();
		env = AppEnv.valueOf(envName);
	}

	public static <K extends AppConfig<K>> K load(Class<K> clazz) {
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
		return JSON.parseObject(content, clazz);
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

	public Map<String, JdbcConfig> getJdbcs() {
		return jdbcs;
	}

	public void setJdbcs(Map<String, JdbcConfig> jdbcs) {
		this.jdbcs = jdbcs;
	}

	public Map<String, MongoConfig> getMongoes() {
		return mongoes;
	}

	public void setMongoes(Map<String, MongoConfig> mongoes) {
		this.mongoes = mongoes;
	}

}
