package ackern.core.error;

public class RedisError extends KernError {

	private static final long serialVersionUID = 4794538968906263490L;

	public RedisError(String message, Throwable e) {
		super(message, e);
	}

}
