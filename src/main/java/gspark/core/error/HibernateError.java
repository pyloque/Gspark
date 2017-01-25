package gspark.core.error;

public class HibernateError extends KernError {

	private static final long serialVersionUID = -4042808087638693257L;

	public HibernateError(String message, Throwable e) {
		super(message, e);
	}

}
