package gspark.redis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import gspark.core.redis.RedisStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisDataException;

public class MockRedisStore extends RedisStore {

	protected Map<String, String> kvs = new HashMap<>();
	protected Map<String, Map<String, String>> hashes = new HashMap<>();
	protected Map<String, LinkedList<String>> lists = new HashMap<>();
	protected Map<String, Set<String>> sets = new HashMap<>();
	protected Map<String, Set<String>> pfs = new HashMap<>();
	protected Map<String, Map<String, Double>> zsets = new HashMap<>();
	protected Jedis jedis;
	protected Pipeline pipe;

	public MockRedisStore() {
		super(mock(JedisPool.class));
		this.jedis = mock(Jedis.class);
		this.pipe = mock(Pipeline.class);
		doNothing().when(jedis).close();
		when(getPool().getResource()).thenReturn(jedis);
		this.mockPipe().mockKv().mockList().mockHash().mockSet().mockZset().mockPf().mockShare();
	}

	public MockRedisStore mockPipe() {
		when(jedis.pipelined()).then(invocation -> {
			return pipe;
		});
		return this;
	}

	public MockRedisStore mockKv() {
		when(jedis.set(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(1);
			kvs.put(key, value);
			return "OK";
		});
		when(jedis.setnx(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(1);
			if (kvs.containsKey(key)) {
				return 0L;
			}
			kvs.put(key, value);
			return 1L;
		});
		when(jedis.setex(anyString(), anyInt(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(2);
			return jedis.set(key, value);
		});
		when(jedis.set(anyString(), anyString(), anyString(), anyString(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			String value = invocation.getArgument(1);
			String nxxx = invocation.getArgument(2);
			String expx = invocation.getArgument(3);
			// int ttl = invocation.getArgument(4);
			nxxx = nxxx.toLowerCase();
			expx = expx.toLowerCase();
			if (!nxxx.equals("nx") && !nxxx.equals("xx")) {
				throw new JedisDataException("ERR syntax error");
			}
			if (!expx.equals("ex") && !expx.equals("px")) {
				throw new JedisDataException("ERR syntax error");
			}
			String oldVal = kvs.get(key);
			if (oldVal == null && nxxx.equals("nx")) {
				kvs.put(key, value);
				return "OK";
			}
			if (oldVal != null && nxxx.equals("xx")) {
				kvs.put(key, value);
				return "OK";
			}
			return null;
		});
		when(jedis.get(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			return kvs.get(key);
		});
		when(jedis.incrBy(anyString(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long by = invocation.getArgument(1);
			String value = kvs.get(key);
			if (value == null) {
				kvs.put(key, "" + by);
				return by;
			}
			try {
				Long intVal = Long.parseLong(value);
				kvs.put(key, "" + (intVal + by));
				return intVal + by;
			} catch (NumberFormatException e) {
				throw new JedisDataException("ERR value is not an integer or out of range");
			}
		});
		when(jedis.incrByFloat(anyString(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double by = invocation.getArgument(1);
			String value = kvs.get(key);
			if (value == null) {
				kvs.put(key, "" + by);
				return by;
			}
			try {
				Double floatVal = Double.parseDouble(value);
				kvs.put(key, "" + (floatVal + by));
				return floatVal + by;
			} catch (NumberFormatException e) {
				throw new JedisDataException("ERR value is not a valid float", e);
			}
		});
		when(jedis.incr(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			return jedis.incrBy(key, 1);
		});
		when(jedis.decr(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			return jedis.decrBy(key, 1);
		});
		when(jedis.decrBy(anyString(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long value = invocation.getArgument(1);
			return jedis.incrBy(key, -value);
		});
		return this;
	}

	public MockRedisStore mockHash() {
		when(jedis.hset(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			String value = invocation.getArgument(2);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				kvs = new HashMap<>();
				hashes.put(key, kvs);
			}
			if (kvs.put(name, value) == null) {
				return 1L;
			}
			return 0L;
		});
		when(jedis.hsetnx(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			String value = invocation.getArgument(2);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				kvs = new HashMap<>();
				hashes.put(key, kvs);
			}
			if (kvs.containsKey(name)) {
				return 0L;
			}
			kvs.put(name, value);
			return 1L;
		});
		when(jedis.hmset(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, String> items = invocation.getArgument(1);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				kvs = new HashMap<>();
				hashes.put(key, kvs);
			}
			kvs.putAll(items);
			return "OK";
		});
		when(jedis.hdel(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				return 0L;
			}
			if (kvs.remove(name) != null) {
				return 1L;
			}
			return 0L;
		});
		when(jedis.hlen(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return Long.valueOf(kvs.size());
		});
		when(jedis.hkeys(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return new HashSet<String>(kvs.keySet());
		});
		when(jedis.hvals(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return new ArrayList<String>(kvs.values());
		});
		when(jedis.hincrBy(anyString(), anyString(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			long by = invocation.getArgument(2);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				kvs = new HashMap<>();
				hashes.put(key, kvs);
			}
			String value = kvs.getOrDefault(name, "0");
			try {
				long intval = Long.parseLong(value);
				intval += by;
				kvs.put(name, "" + intval);
				return intval;
			} catch (NumberFormatException e) {
				throw new JedisDataException("ERR hash value is not an integer", e);
			}
		});
		when(jedis.hincrByFloat(anyString(), anyString(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			double by = invocation.getArgument(2);
			Map<String, String> kvs = hashes.get(key);
			if (kvs == null) {
				kvs = new HashMap<>();
				hashes.put(key, kvs);
			}
			String value = kvs.getOrDefault(name, "0");
			try {
				double floatval = Double.parseDouble(value);
				floatval += by;
				kvs.put(name, "" + floatval);
				return floatval;
			} catch (NumberFormatException e) {
				throw new JedisDataException("ERR hash value is not an float", e);
			}
		});
		when(jedis.hgetAll(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return new HashMap<String, String>(kvs);
		});
		when(jedis.hget(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return kvs.get(name);
		});
		when(jedis.hexists(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String name = invocation.getArgument(1);
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			return kvs.containsKey(name);
		});
		when(jedis.hmget(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			Map<String, String> kvs = hashes.getOrDefault(key, Collections.emptyMap());
			List<String> result = new ArrayList<>();
			for (int i = 1; i < argsNum; i++) {
				String name = invocation.getArgument(i);
				result.add(kvs.get(name));
			}
			return result;
		});
		return this;
	}

	public MockRedisStore mockSet() {
		when(jedis.sadd(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			Set<String> set = sets.get(key);
			if (set == null) {
				set = new HashSet<>();
				sets.put(key, set);
			}
			int count = 0;
			for (int i = 1; i < argsNum; i++) {
				String member = invocation.getArgument(i);
				if (!set.contains(member)) {
					set.add(member);
					count++;
				}
			}
			return count > 0 ? 1L : 0L;
		});
		when(jedis.srem(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			Set<String> set = sets.get(key);
			int count = 0;
			if (set != null) {
				for (int i = 1; i < argsNum; i++) {
					String member = invocation.getArgument(i);
					if (set.contains(member)) {
						set.remove(member);
						count++;
					}
				}
			}
			return count > 0 ? 1L : 0L;
		});
		when(jedis.scard(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Set<String> set = sets.getOrDefault(key, Collections.emptySet());
			return Long.valueOf(set.size());
		});
		when(jedis.smembers(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Set<String> set = sets.getOrDefault(key, Collections.emptySet());
			return new HashSet<>(set);
		});
		when(jedis.srandmember(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Set<String> set = sets.getOrDefault(key, Collections.emptySet());
			int pos = ThreadLocalRandom.current().nextInt(set.size());
			int idx = 0;
			for (String member : set) {
				if (idx == pos) {
					return member;
				}
				idx++;
			}
			return null;
		});
		return this;
	}

	public MockRedisStore mockZset() {
		when(jedis.zcard(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, Double> zset = zsets.getOrDefault(key, Collections.emptyMap());
			return Long.valueOf(zset.size());
		});
		when(jedis.zadd(anyString(), anyDouble(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			double score = invocation.getArgument(1);
			String name = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				zset = new HashMap<>();
				zsets.put(key, zset);
			}
			if (zset.put(name, score) != null) {
				return 0L;
			}
			return 1L;
		});
		when(jedis.zadd(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			Map<String, Double> tuples = invocation.getArgument(1);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				zset = new HashMap<>();
				zsets.put(key, zset);
			}
			long count = 0;
			for (Entry<String, Double> entry : tuples.entrySet()) {
				if (zset.put(entry.getKey(), entry.getValue()) == null) {
					count++;
				}
			}
			return count;
		});
		when(jedis.zrem(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return 0L;
			}
			long count = 0;
			for (int i = 1; i < argsNum; i++) {
				if (zset.remove(invocation.getArgument(i)) != null) {
					count++;
				}
			}
			if (zset.isEmpty()) {
				zsets.remove(key);
			}
			return count;
		});
		when(jedis.zincrby(anyString(), anyDouble(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			double by = invocation.getArgument(1);
			String name = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				zset = new HashMap<>();
				zsets.put(key, zset);
			}
			Double score = zset.get(name);
			if (score == null) {
				score = 0.0;
			}
			score += by;
			zset.put(name, score);
			return score;
		});
		when(jedis.zrangeWithScores(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return Collections.emptySet();
			}
			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(x.getScore(), y.getScore()));
			if (start < 0) {
				start = Math.max(list.size() + start, 0);
			} else {
				start = Math.min(list.size() - 1, start);
			}
			if (end < 0) {
				end = Math.max(list.size() + end + 1, 0);
			} else {
				end = Math.min(list.size(), end + 1);
			}
			int count = (int) (end - start);
			Set<Tuple> result = new LinkedHashSet<Tuple>();
			ListIterator<Tuple> iter = list.listIterator((int) start);
			for (int i = 0; i < count; i++) {
				result.add(iter.next());
			}
			return result;
		});
		when(jedis.zrevrangeWithScores(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return Collections.emptySet();
			}
			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(y.getScore(), x.getScore()));
			if (start < 0) {
				start = Math.max(list.size() + start, 0);
			} else {
				start = Math.min(list.size() - 1, start);
			}
			if (end < 0) {
				end = Math.max(list.size() + end + 1, 0);
			} else {
				end = Math.min(list.size(), end + 1);
			}
			int count = (int) (end - start);
			Set<Tuple> result = new LinkedHashSet<Tuple>();
			ListIterator<Tuple> iter = list.listIterator((int) start);
			for (int i = 0; i < count; i++) {
				result.add(iter.next());
			}
			return result;
		});
		when(jedis.zrange(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			Set<Tuple> pairs = jedis.zrangeWithScores(key, start, end);
			Set<String> result = new LinkedHashSet<>();
			for (Tuple pair : pairs) {
				result.add(pair.getElement());
			}
			return result;
		});
		when(jedis.zrevrange(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			Set<Tuple> pairs = jedis.zrevrangeWithScores(key, start, end);
			Set<String> result = new LinkedHashSet<>();
			for (Tuple pair : pairs) {
				result.add(pair.getElement());
			}
			return result;
		});
		when(jedis.zrangeByScoreWithScores(anyString(), anyDouble(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double start = invocation.getArgument(1);
			double end = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return Collections.emptySet();
			}
			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(x.getScore(), y.getScore()));
			Set<Tuple> result = new LinkedHashSet<>();
			for (Tuple tuple : list) {
				if (start <= tuple.getScore() && tuple.getScore() <= end) {
					result.add(tuple);
				}
			}
			return result;
		});
		when(jedis.zrangeByScore(anyString(), anyDouble(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double start = invocation.getArgument(1);
			double end = invocation.getArgument(2);
			Set<Tuple> tuples = jedis.zrangeByScoreWithScores(key, start, end);
			Set<String> result = new LinkedHashSet<>();
			for (Tuple tuple : tuples) {
				result.add(tuple.getElement());
			}
			return result;
		});
		when(jedis.zrevrangeByScoreWithScores(anyString(), anyDouble(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double start = invocation.getArgument(1);
			double end = invocation.getArgument(2);
			Set<Tuple> tuples = jedis.zrangeByScoreWithScores(key, start, end);
			Set<Tuple> result = new LinkedHashSet<>();
			Iterator<Tuple> iter = new LinkedList<>(tuples).descendingIterator();
			while (iter.hasNext()) {
				result.add(iter.next());
			}
			return result;
		});
		when(jedis.zrevrangeByScore(anyString(), anyDouble(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double start = invocation.getArgument(1);
			double end = invocation.getArgument(2);
			Set<Tuple> tuples = jedis.zrevrangeByScoreWithScores(key, start, end);
			Set<String> result = new LinkedHashSet<>();
			for (Tuple tuple : tuples) {
				result.add(tuple.getElement());
			}
			return result;
		});
		when(jedis.zrangeByScoreWithScores(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String startScore = invocation.getArgument(1);
			String endScore = invocation.getArgument(2);

			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return Collections.emptySet();
			}

			double start, end;
			boolean startExclude = false, endExclude = false;
			if (startScore.equals("-inf")) {
				start = Double.MIN_VALUE;
			} else if (startScore.equals("+inf")) {
				start = Double.MAX_VALUE;
			} else {
				if (startScore.startsWith("(")) {
					startExclude = true;
					startScore = startScore.substring(1);
				}
				try {
					start = Double.parseDouble(startScore);
				} catch (NumberFormatException e) {
					throw new JedisDataException("ERR min or max is not a float");
				}
			}

			if (endScore.equals("-inf")) {
				end = Double.MIN_VALUE;
			} else if (endScore.equals("+inf")) {
				end = Double.MAX_VALUE;
			} else {
				if (endScore.startsWith("(")) {
					endExclude = true;
					endScore = endScore.substring(1);
				}
				try {
					end = Double.parseDouble(endScore);
				} catch (NumberFormatException e) {
					throw new JedisDataException("ERR min or max is not a float");
				}
			}

			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(x.getScore(), y.getScore()));
			Set<Tuple> result = new LinkedHashSet<>();
			for (Tuple tuple : list) {
				double score = tuple.getScore();
				if ((start < score || !startExclude && start == score)
						&& (score < end || !endExclude && end == score)) {
					result.add(tuple);
				}
			}
			return result;
		});
		when(jedis.zrevrangeByScoreWithScores(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String startScore = invocation.getArgument(1);
			String endScore = invocation.getArgument(2);
			Set<Tuple> tuples = jedis.zrangeByScoreWithScores(key, startScore, endScore);
			Set<Tuple> result = new LinkedHashSet<>();
			Iterator<Tuple> iter = new LinkedList<>(tuples).descendingIterator();
			while (iter.hasNext()) {
				result.add(iter.next());
			}
			return result;
		});
		when(jedis.zrangeByScore(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String startScore = invocation.getArgument(1);
			String endScore = invocation.getArgument(2);
			Set<Tuple> tuples = jedis.zrangeByScoreWithScores(key, startScore, endScore);
			Set<String> result = new LinkedHashSet<>();
			for (Tuple tuple : tuples) {
				result.add(tuple.getElement());
			}
			return result;
		});
		when(jedis.zrevrangeByScore(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String startScore = invocation.getArgument(1);
			String endScore = invocation.getArgument(2);
			Set<String> tuples = jedis.zrangeByScore(key, startScore, endScore);
			Set<String> result = new LinkedHashSet<>();
			Iterator<String> iter = new LinkedList<>(tuples).descendingIterator();
			while (iter.hasNext()) {
				result.add(iter.next());
			}
			return result;
		});
		when(jedis.zrank(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String member = invocation.getArgument(1);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return null;
			}
			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(x.getScore(), y.getScore()));
			long rank = 0;
			for (Tuple tuple : list) {
				if (tuple.getElement().equals(member)) {
					return rank;
				}
				rank++;
			}
			return null;
		});
		when(jedis.zscore(anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String member = invocation.getArgument(1);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return null;
			}
			return zset.get(member);
		});
		when(jedis.zremrangeByRank(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return 0L;
			}
			LinkedList<Tuple> list = new LinkedList<>();
			for (Entry<String, Double> entry : zset.entrySet()) {
				list.add(new Tuple(entry.getKey(), entry.getValue()));
			}
			list.sort((x, y) -> Double.compare(x.getScore(), y.getScore()));
			if (start < 0) {
				start = Math.max(list.size() + start, 0);
			} else {
				start = Math.min(list.size() - 1, start);
			}
			if (end < 0) {
				end = Math.max(list.size() + end + 1, 0);
			} else {
				end = Math.min(list.size(), end + 1);
			}
			int count = (int) (end - start);
			ListIterator<Tuple> iter = list.listIterator((int) start);
			for (int i = 0; i < count; i++) {
				Tuple tuple = iter.next();
				zset.remove(tuple.getElement());
			}
			if (zset.isEmpty()) {
				zsets.remove(key);
			}
			return Long.valueOf(count);
		});
		when(jedis.zremrangeByScore(anyString(), anyDouble(), anyDouble())).then(invocation -> {
			String key = invocation.getArgument(0);
			double startScore = invocation.getArgument(1);
			double endScore = invocation.getArgument(2);
			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return 0L;
			}
			Iterator<Entry<String, Double>> iter = zset.entrySet().iterator();
			long count = 0;
			while (iter.hasNext()) {
				double score = iter.next().getValue();
				if (score >= startScore && score <= endScore) {
					iter.remove();
					count++;
				}
			}
			if (zset.isEmpty()) {
				zsets.remove(key);
			}
			return count;
		});
		when(jedis.zremrangeByScore(anyString(), anyString(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			String startScore = invocation.getArgument(1);
			String endScore = invocation.getArgument(2);

			Map<String, Double> zset = zsets.get(key);
			if (zset == null) {
				return 0L;
			}

			double start, end;
			boolean startExclude = false, endExclude = false;
			if (startScore.equals("-inf")) {
				start = Double.MIN_VALUE;
			} else if (startScore.equals("+inf")) {
				start = Double.MAX_VALUE;
			} else {
				if (startScore.startsWith("(")) {
					startExclude = true;
					startScore = startScore.substring(1);
				}
				try {
					start = Double.parseDouble(startScore);
				} catch (NumberFormatException e) {
					throw new JedisDataException("ERR min or max is not a float");
				}
			}

			if (endScore.equals("-inf")) {
				end = Double.MIN_VALUE;
			} else if (endScore.equals("+inf")) {
				end = Double.MAX_VALUE;
			} else {
				if (endScore.startsWith("(")) {
					endExclude = true;
					endScore = endScore.substring(1);
				}
				try {
					end = Double.parseDouble(endScore);
				} catch (NumberFormatException e) {
					throw new JedisDataException("ERR min or max is not a float");
				}
			}

			Iterator<Entry<String, Double>> iter = zset.entrySet().iterator();
			long count = 0;
			while (iter.hasNext()) {
				double score = iter.next().getValue();
				if ((start < score || !startExclude && start == score)
						&& (score < end || !endExclude && end == score)) {
					iter.remove();
					count++;
				}
			}
			if (zset.isEmpty()) {
				zsets.remove(key);
			}
			return count;
		});
		return this;
	}

	public MockRedisStore mockList() {
		when(jedis.rpush(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				list = new LinkedList<>();
				lists.put(key, list);
			}
			for (int i = 1; i < argsNum; i++) {
				list.addLast(invocation.getArgument(i));
			}
			return Long.valueOf(list.size());
		});
		when(jedis.rpushx(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return 0L;
			}
			for (int i = 1; i < argsNum; i++) {
				list.addLast(invocation.getArgument(i));
			}
			return Long.valueOf(list.size());
		});
		when(jedis.lpush(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				list = new LinkedList<>();
				lists.put(key, list);
			}
			for (int i = 1; i < argsNum; i++) {
				list.addFirst(invocation.getArgument(i));
			}
			return list.size();
		});
		when(jedis.lpushx(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return 0L;
			}
			for (int i = 1; i < argsNum; i++) {
				list.addFirst(invocation.getArgument(i));
			}
			return list.size();
		});
		when(jedis.rpop(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return null;
			}
			String value = list.removeLast();
			if (list.isEmpty()) {
				lists.remove(key);
			}
			return value;
		});
		when(jedis.lpop(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return null;
			}
			String value = list.removeFirst();
			if (list.isEmpty()) {
				lists.remove(key);
			}
			return value;
		});
		when(jedis.llen(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return 0L;
			}
			return Long.valueOf(list.size());
		});
		when(jedis.lrange(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return new LinkedList<>();
			}
			if (start < 0) {
				start = Math.max(list.size() + start, 0);
			} else {
				start = Math.min(list.size() - 1, start);
			}
			if (end < 0) {
				end = Math.max(list.size() + end + 1, 0);
			} else {
				end = Math.min(list.size(), end + 1);
			}
			int count = (int) (end - start);
			LinkedList<String> result = new LinkedList<String>();
			ListIterator<String> iter = list.listIterator((int) start);
			for (int i = 0; i < count; i++) {
				String value = iter.next();
				result.add(value);
			}
			return result;
		});
		when(jedis.lindex(anyString(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long idx = invocation.getArgument(1);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return null;
			}
			if (idx < 0) {
				idx += list.size();
			}
			if (idx < 0 || idx >= list.size()) {
				return null;
			}
			return list.get((int) idx);
		});
		when(jedis.lset(anyString(), anyLong(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			long idx = invocation.getArgument(1);
			String value = invocation.getArgument(2);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				throw new JedisDataException("ERR index out of range");
			}
			if (idx < 0) {
				idx += list.size();
			}
			if (idx < 0 || idx >= list.size()) {
				throw new JedisDataException("ERR index out of range");
			}
			list.set((int) idx, value);
			return "OK";
		});
		when(jedis.ltrim(anyString(), anyLong(), anyLong())).then(invocation -> {
			String key = invocation.getArgument(0);
			long start = invocation.getArgument(1);
			long end = invocation.getArgument(2);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return new LinkedList<>();
			}
			if (start < 0) {
				start = Math.max(list.size() + start, 0);
			} else {
				start = Math.min(list.size() - 1, start);
			}
			if (end < 0) {
				end = Math.max(list.size() + end + 1, 0);
			} else {
				end = Math.min(list.size(), end + 1);
			}
			int count = (int) (end - start);
			LinkedList<String> newList = new LinkedList<String>();
			ListIterator<String> iter = list.listIterator((int) start);
			for (int i = 0; i < count; i++) {
				String value = iter.next();
				newList.add(value);
			}
			if (newList.isEmpty()) {
				lists.remove(key);
			} else {
				lists.put(key, newList);
			}
			return "OK";
		});
		when(jedis.lrem(anyString(), anyLong(), anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			long count = invocation.getArgument(1);
			String value = invocation.getArgument(2);
			LinkedList<String> list = lists.get(key);
			if (list == null) {
				return 0L;
			}
			Iterator<String> iter = list.iterator();
			if (count < 0) {
				iter = list.descendingIterator();
			} else if (count == 0) {
				count = list.size();
			} else {
				count = Math.min(list.size(), count);
			}
			int current = 0;
			while (iter.hasNext() && current < count) {
				if (iter.next().equals(value)) {
					iter.remove();
					current++;
				}
			}
			if (list.isEmpty()) {
				lists.remove(key);
			}
			return Long.valueOf(current);
		});
		when(jedis.rpoplpush(anyString(), anyString())).then(invocation -> {
			String srcKey = invocation.getArgument(0);
			String dstKey = invocation.getArgument(1);
			LinkedList<String> srcList = lists.get(srcKey);
			if (srcList == null) {
				return null;
			}
			String value = srcList.removeLast();
			if (srcList.isEmpty()) {
				lists.remove(srcKey);
			}
			LinkedList<String> dstList = lists.get(dstKey);
			if (dstList == null) {
				dstList = new LinkedList<>();
				lists.put(dstKey, dstList);
			}
			dstList.addFirst(value);
			return value;
		});
		return this;
	}

	public MockRedisStore mockPf() {
		when(jedis.pfadd(anyString(), any())).then(invocation -> {
			String key = invocation.getArgument(0);
			int argsNum = invocation.getArguments().length;
			Set<String> set = pfs.get(key);
			if (set == null) {
				set = new HashSet<>();
				pfs.put(key, set);
			}
			boolean result = false;
			for (int i = 1; i < argsNum; i++) {
				if (set.add(invocation.getArgument(i))) {
					result = true;
				}
			}
			return result ? 1L : 0L;
		});
		when(jedis.pfcount(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			Set<String> set = pfs.getOrDefault(key, Collections.emptySet());
			return Long.valueOf(set.size());
		});
		return this;
	}

	public MockRedisStore mockShare() {
		when(jedis.del(anyString())).then(invocation -> {
			String key = invocation.getArgument(0);
			if (kvs.remove(key) != null) {
				return 1L;
			}
			if (hashes.remove(key) != null) {
				return 1L;
			}
			if (sets.remove(key) != null) {
				return 1L;
			}
			if (lists.remove(key) != null) {
				return 1L;
			}
			if (zsets.remove(key) != null) {
				return 1L;
			}
			if (pfs.remove(key) != null) {
				return 1L;
			}
			return 0L;
		});
		when(jedis.echo(anyString())).then(invocation -> {
			return invocation.getArgument(0);
		});
		when(jedis.ping()).then(invocation -> {
			return "OK";
		});
		when(jedis.expire(anyString(), anyInt())).then(invocation -> {
			return 1L;
		});
		when(jedis.expireAt(anyString(), anyLong())).then(invocation -> {
			return 1L;
		});
		when(jedis.pexpire(anyString(), anyLong())).then(invocation -> {
			return 1L;
		});
		when(jedis.pexpireAt(anyString(), anyLong())).then(invocation -> {
			return 1L;
		});
		when(jedis.persist(anyString())).then(invocation -> {
			return 1L;
		});
		return this;
	}

	public MockRedisStore mockMore(Consumer<Jedis> func) {
		func.accept(jedis);
		return this;
	}

}
