package ackern.core.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import ackern.core.errors.HibernateError;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;

/**
 * 
 * 对分表的通用功能进行封装
 *
 */
public abstract class ShardingSupport {

	// 用于生成唯一类名的后缀
	public static int seq = 0;

	// 存储多个分表模型类
	private Map<String, Class<?>> shards = new HashMap<>();

	/**
	 * 根据散列参数找到相应的模型类
	 */
	public Class<?> shardBy(String[] keys) {
		String index = indexFor(keys);
		return shards.get(index);
	}

	/**
	 * 使用散列参数构造一个空模型实例
	 */
	@SuppressWarnings("unchecked")
	public <T> T newEmptyInstance(String[] keys) {
		Class<?> shardClass = this.shardBy(keys);
		try {
			return (T) shardClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new HibernateError("create new empty instance error", e);
		}
	}

	/**
	 * 根据模型类和分表索引，动态构造单个分表模型类
	 */
	protected Class<?> createShardClass(Class<?> entityClass, String index) {
		Class<?> shardClass = this.shards.get(index);
		if (shardClass != null) {
			return shardClass;
		}
		String parentName = entityClass.getDeclaredAnnotation(Table.class).name();
		String tableName = String.format("%s_%s", parentName, index);
		String className = String.format("%s_%s_seq%s", entityClass.getSimpleName(), index, seq);
		seq++;
		shardClass = new ByteBuddy().subclass(entityClass).name(className)
				.annotateType(AnnotationDescription.Builder.ofType(Entity.class).build())
				.annotateType(AnnotationDescription.Builder.ofType(Table.class).define("name", tableName).build())
				.make().load(entityClass.getClassLoader()).getLoaded();
		this.shards.put(index, shardClass);
		return shardClass;
	}

	/**
	 * 获取所有的模型类
	 */
	public Collection<Class<?>> all() {
		return shards.values();
	}

	/**
	 * 根据散列参数映射相应的分表索引
	 */
	public abstract String indexFor(String[] keys);

	/**
	 * 构造所有的分表模型类
	 */
	public abstract void createShards();

}
