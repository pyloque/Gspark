package ackern.core.errors;

public class RabbitError extends KernError {

	private static final long serialVersionUID = -2566955800798772606L;

	public RabbitError(String message, Throwable e) {
		super(message, e);
	}

}
