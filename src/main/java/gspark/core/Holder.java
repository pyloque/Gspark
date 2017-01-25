package gspark.core;

public class Holder<T> {

	private T t;

	public void set(T t) {
		this.t = t;
	}

	public T value() {
		return this.t;
	}

}
