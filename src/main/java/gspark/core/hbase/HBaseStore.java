package gspark.core.hbase;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gspark.core.config.HBaseConfig;
import gspark.core.error.HBaseError;

public class HBaseStore implements Closeable {
	private final static Logger LOG = LoggerFactory.getLogger(HBaseStore.class);

	private Configuration conf;
	private HTablePool pool;

	public HBaseStore(HBaseConfig config) {
		conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", config.getZkAddrs());
		conf.setInt("hbase.zookeeper.property.clientPort", config.getZkPort());
		conf.setInt("hbase.htable.threads.max", 1);
		pool = new HTablePool(conf, config.getMaxTableSize());
	}

	public void execute(String name, HBaseOperation<HTableInterface> op) {
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

	public void admin(HBaseOperation<HBaseAdmin> consumer) {
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(conf);
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
		HBaseConfig config = new HBaseConfig();
		config.setZkAddrs("localhost");
		config.setZkPort(2181);
		HBaseStore store = new HBaseStore(config);
		store.admin(admin -> {
			HTableDescriptor schema = new HTableDescriptor("computer");
			schema.addFamily(new HColumnDescriptor("memory"));
			schema.addFamily(new HColumnDescriptor("cpu"));
			if (!admin.isTableAvailable("computer")) {
				admin.createTable(schema);
			}
		});
		store.execute("computer", table -> {
			Put put = new Put(Bytes.toBytes("qianwp"));
			put.add(Bytes.toBytes("memory"), Bytes.toBytes("size"), Bytes.toBytes("8G"));
			put.add(Bytes.toBytes("memory"), Bytes.toBytes("type"), Bytes.toBytes("DDR3"));
			put.add(Bytes.toBytes("cpu"), Bytes.toBytes("cores"), Bytes.toBytes(2));
			put.add(Bytes.toBytes("cpu"), Bytes.toBytes("frequency"), Bytes.toBytes("2.4GHz"));
			table.put(put);
		});
		store.execute("computer", table -> {
			Get get = new Get(Bytes.toBytes("qianwp"));
			Result result = table.get(get);
			KeyValue kv = result.getColumnLatest(Bytes.toBytes("memory"), Bytes.toBytes("size"));
			System.out.println(new String(kv.getValue()));
			kv = result.getColumnLatest(Bytes.toBytes("memory"), Bytes.toBytes("type"));
			System.out.println(new String(kv.getValue()));
		});
		store.close();
	}
}
