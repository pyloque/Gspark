package ackern.core.hbase;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ackern.core.errors.HBaseError;

public class HBaseStore implements Closeable {
	private final static Logger LOG = LoggerFactory.getLogger(HBaseStore.class);

	private Configuration config;
	private HTablePool pool;

	public HBaseStore(String zkAddrs, int port) {
		config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zkAddrs);
		config.setInt("hbase.zookeeper.property.clientPort", port);
		config.setInt("hbase.htable.threads.max", 1);
		pool = new HTablePool(config, 5);
	}

	public void execute(String name, TableOperation<HTableInterface> op) {
		HTableInterface table = pool.getTable(name);
		try {
			op.accept(table);
		} catch (IOException e) {
			LOG.error("hbase operation error", e);
			throw new HBaseError("hbase operation error", e);
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				LOG.error("return hbase instance to pool error", e);
			}
		}
	}

	public void admin(TableOperation<HBaseAdmin> consumer) {
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(config);
		} catch (IOException e) {
			LOG.error("open hbase admin instance error", e);
			throw new HBaseError("open hbase admin instance error", e);
		}
		try {
			consumer.accept(admin);
		} catch (IOException e) {
			LOG.error("hbase admin operation error", e);
			throw new HBaseError("hbase admin operation error", e);
		} finally {
			try {
				admin.close();
			} catch (IOException e) {
				LOG.error("close hbase admin instance error", e);
			}
		}
	}

	@Override
	public void close() {
		try {
			pool.close();
		} catch (IOException e) {
			LOG.error("close hbase table pool error", e);
		}
	}

	public static void main(String[] args) {
		HBaseStore store = new HBaseStore("localhost", 2181);
		store.admin(admin -> {
			HTableDescriptor schema = new HTableDescriptor("computer");
			schema.addFamily(new HColumnDescriptor("memory"));
			schema.addFamily(new HColumnDescriptor("cpu"));
			admin.createTable(schema);
		});
		store.execute("computer", table -> {
			Put put = new Put(Bytes.toBytes("qianwp"));
			put.add(Bytes.toBytes("memory"), Bytes.toBytes("size"), Bytes.toBytes("8G"));
			put.add(Bytes.toBytes("memory"), Bytes.toBytes("type"), Bytes.toBytes("DDR3"));
			put.add(Bytes.toBytes("cpu"), Bytes.toBytes("cores"), Bytes.toBytes(2));
			put.add(Bytes.toBytes("cpu"), Bytes.toBytes("frequency"), Bytes.toBytes("2.4GHz"));
			table.put(put);
		});
		store.close();
	}
}
