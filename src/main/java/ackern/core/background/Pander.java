package ackern.core.background;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Pander<T> {
	private final static Logger LOG = LoggerFactory.getLogger(Pander.class);

	private BlockingThreadPoolExecutor executor;
	private boolean stop;
	private int sleepAwait;

	public Pander(int capacity, int sleepAwaitInMillis) {
		this.sleepAwait = sleepAwaitInMillis;
		this.executor = new BlockingThreadPoolExecutor(capacity);
	}

	public Pander(int capacity) {
		this(capacity, 1000);
	}

	/**
	 * 这里可以做一些汇报性的工作
	 */
	public abstract void idle() ;
	/**
	 * 初始化工作
	 */
	public abstract void beforeStart();

	/**
	 * 取一个任务
	 * 
	 * @return
	 */
	public abstract T take();

	/**
	 * 处理一个任务
	 * 
	 * @param t
	 */
	public abstract void process(T t);

	/**
	 * 开始干活吧
	 */
	public void suckUp() {
		try {
			this.beforeStart();
		} catch (Exception e) {
			LOG.error("before start error in pander", e);
		}
		while (!stop) {
			T t = null;
			try {
				t = take();
			} catch (Exception e) {
				LOG.error("take error in pander", e);
			}
			if (t != null) {
				final T x = t;
				executor.submit(() -> {
					try {
						process(x);
					} catch (Exception e) {
						LOG.error("process error in pander", e);
					}
				});
			} else {
				try {
					idle();
				} catch (Exception e) {
					LOG.error("idle error in pander", e);
				}
				try {
					Thread.sleep(sleepAwait);
				} catch (InterruptedException e) {
				}
			}
		}
		executor.shutdown();
	}

	/**
	 * 触发停止
	 */
	public void stop() {
		stop = true;
	}

	/**
	 * 等待停止
	 */
	public void awaitForTermination(int seconds) {
		try {
			if (!executor.awaitTermination(seconds, TimeUnit.SECONDS)) {
				LOG.warn("tasks maybe lost in pander");
			}
		} catch (InterruptedException e) {
		}
	}

}
