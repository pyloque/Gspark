package gspark.core.bg;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

@Singleton
public class Boss {

	private List<Worker<?>> workers = new ArrayList<>();
	private int termAwait = 5;

	public Boss worker(Worker<?> worker) {
		this.workers.add(worker);
		return this;
	}

	public Boss termAwait(int seconds) {
		this.termAwait = seconds;
		return this;
	}

	public void start() {
		for (Worker<?> worker : workers) {
			new Thread() {
				public void run() {
					worker.suckUp();
				}
			}.start();
		}
	}

	public void stop() {
		for (Worker<?> worker : workers) {
			worker.stop();
		}
		for (Worker<?> worker : workers) {
			worker.awaitForTermination(termAwait);
		}
	}

}
