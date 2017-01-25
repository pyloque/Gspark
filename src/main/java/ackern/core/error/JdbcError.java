package ackern.core.error;

public class JdbcError extends KernError {

	private static final long serialVersionUID = -6291926348550953912L;

	public JdbcError(String message, Throwable e) {
		super(message, e);
	}

}
