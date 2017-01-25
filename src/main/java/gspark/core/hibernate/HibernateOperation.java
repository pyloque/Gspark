package gspark.core.hibernate;

import org.hibernate.HibernateException;

@FunctionalInterface
public interface HibernateOperation<T> {
	public void accept(T t) throws HibernateException;
}
