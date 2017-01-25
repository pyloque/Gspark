package ackern.core.view;

public class ApiError extends RuntimeException {

	private static final long serialVersionUID = -2486664259162937617L;

	private int code;
	private final static int CODE_SERVER_ERROR = 1;

	public ApiError(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public static ApiError newServerError(String msg) {
		return new ApiError(CODE_SERVER_ERROR, msg);
	}

	public int code() {
		return code;
	}
}
