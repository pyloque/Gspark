package gspark.core.view;

public class ApiError extends RuntimeException {

	private static final long serialVersionUID = -2486664259162937617L;

	private int code;
	private final static int CODE_BACKEND_ERROR = 1;
	private final static int CODE_UNKNOWN_ERROR = 2;

	public ApiError(int code, String msg) {
		super(msg);
		this.code = code;
	}

	public static ApiError newServerError(String msg) {
		return new ApiError(CODE_BACKEND_ERROR, msg);
	}

	public static ApiError newUnknownError(String msg) {
		return new ApiError(CODE_UNKNOWN_ERROR, msg);
	}

	public int code() {
		return code;
	}
}
