package ackern.core.hibernate;

import java.util.function.Consumer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * 
 * 使用Java8的Consumer特性封装hibernate的session操作，避免忘记关闭session导致资源泄露
 *
 */
public class HibernateStore {

	private SessionFactory factory;

	public HibernateStore(SessionFactory factory) {
		this.factory = factory;
	}

	public void close() {
		factory.close();
	}

	public void execute(Consumer<Session> func) {
		Session session = factory.openSession();
		try {
			func.accept(session);
		} finally {
			session.close();
		}
	}

}