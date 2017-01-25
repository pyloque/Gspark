package ackern.core.error;

public class HBaseError extends KernError {

	private static final long serialVersionUID = 6086865911330999344L;

	public HBaseError(String message, Throwable e) {
		super(message, e);
	}

}
