package ackern.core.error;

public class HttpError extends KernError {

	private static final long serialVersionUID = 7311105386328383548L;

	public HttpError(String message, Throwable e) {
		super(message, e);
	}

}
