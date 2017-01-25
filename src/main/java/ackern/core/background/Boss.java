package ackern.core.background;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;

@Singleton
public class Boss {

	private List<Pander<?>> panders = new ArrayList<>();
	private int termAwait = 5;

	public Boss pander(Pander<?> pander) {
		this.panders.add(pander);
		return this;
	}

	public Boss termAwait(int seconds) {
		this.termAwait = seconds;
		return this;
	}

	public void start() {
		for (Pander<?> pander : panders) {
			new Thread() {
				public void run() {
					pander.suckUp();
				}
			}.start();
		}
	}

	public void stop() {
		for (Pander<?> pander : panders) {
			pander.stop();
		}
		for (Pander<?> pander : panders) {
			pander.awaitForTermination(termAwait);
		}
	}

}
