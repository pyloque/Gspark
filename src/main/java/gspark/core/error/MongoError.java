package gspark.core.error;

public class MongoError extends KernError {

	private static final long serialVersionUID = -4042808087638693257L;

	public MongoError(String message, Throwable e) {
		super(message, e);
	}

}
