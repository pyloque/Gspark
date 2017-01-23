package ackern.core.errors;

public class MemcacheError extends KernError {

	private static final long serialVersionUID = -675444478190226757L;

	public MemcacheError(String message, Throwable e) {
		super(message, e);
	}
	
}
