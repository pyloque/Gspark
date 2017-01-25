package gspark.core.background;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

	private final Semaphore semaphore;

	public BlockingThreadPoolExecutor(int capacity) {
		super(0, capacity, 10L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		semaphore = new Semaphore(capacity);
	}

	public <T> Future<T> submitButBlockIfFull(final Callable<T> task) throws InterruptedException {
		semaphore.acquire();
		return submit(task);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		semaphore.release();
	}
}
