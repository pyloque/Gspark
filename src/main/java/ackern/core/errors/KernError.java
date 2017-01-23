package ackern.core.errors;

public class KernError extends RuntimeException {

	private static final long serialVersionUID = -7896343389486512701L;

	public KernError(String message, Throwable e) {
		super(message, e);
	}
}
