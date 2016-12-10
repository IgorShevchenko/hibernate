package com.igor;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import com.igor.setup.TransactionManager;

public class App {

	public static void main(String[] args) throws Exception {

		TransactionManager TM = TransactionManager.getInstance();
		EntityManagerFactory emf = TransactionManager.createEntityManagerFactory();

		try {
			saveMessage(TM, emf);
			updateMessage(TM, emf);
		} finally {
			TM.rollback();
			emf.close();
		}
	}

	private static void saveMessage(TransactionManager TM, EntityManagerFactory emf) throws Exception {
		/*
		 * Get access to the standard transaction API
		 * <code>UserTransaction</code> and begin a transaction on this thread
		 * of execution.
		 */
		UserTransaction tx = TM.getUserTransaction();
		tx.begin();

		/*
		 * Begin a new session with the database by creating an
		 * <code>EntityManager</code>, this is your context for all persistence
		 * operations.
		 */
		EntityManager em = emf.createEntityManager();

		/*
		 * Create a new instance of the mapped domain model class
		 * <code>Message</code> and set its <code>text</code> property.
		 */
		Message message = new Message();
		message.setText("Hello World!");

		/*
		 * Enlist the transient instance with your persistence context, you make
		 * it persistent. Hibernate now knows that you wish to store that data,
		 * it doesn't necessarily call the database immediately, however.
		 */
		em.persist(message);

		/*
		 * Commit the transaction, Hibernate now automatically checks the
		 * persistence context and executes the necessary SQL
		 * <code>INSERT</code> statement.
		 */
		tx.commit();
		// INSERT into MESSAGE (ID, TEXT) values (1, 'Hello World!')

		/*
		 * If you create an <code>EntityManager</code>, you must close it.
		 */
		em.close();
	}

	private static void updateMessage(TransactionManager TM, EntityManagerFactory emf) throws Exception {
		/*
		 * Every interaction with your database should occur within explicit
		 * transaction boundaries, even if you are only reading data.
		 */
		UserTransaction tx = TM.getUserTransaction();
		tx.begin();

		EntityManager em = emf.createEntityManager();

		/*
		 * Execute a query to retrieve all instances of <code>Message</code>
		 * from the database.
		 */
		List<Message> messages = em.createQuery("select m from Message m", Message.class).getResultList();
		// SELECT * from MESSAGE

		/*
		 * You can change the value of a property, Hibernate will detect this
		 * automatically because the loaded <code>Message</code> is still
		 * attached to the persistence context it was loaded in.
		 */
		messages.get(0).setText("Take me to your leader!");

		/*
		 * On commit, Hibernate checks the persistence context for dirty state
		 * and executes the SQL <code>UPDATE</code> automatically to synchronize
		 * the in-memory with the database state.
		 */
		tx.commit();
		// UPDATE MESSAGE set TEXT = 'Take me to your leader!' where ID = 1

		em.close();
	}
}
