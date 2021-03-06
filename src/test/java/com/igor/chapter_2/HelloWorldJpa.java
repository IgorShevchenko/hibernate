package com.igor.chapter_2;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.TransactionManager;

/**
 * Shows raw code to persist\retrieve entity using JPA.
 */
public class HelloWorldJpa {

	private static final String PERSISTENCE_UNIT = "Chapter2";

	@Test
	public void shouldPersistAndUpdateMessage() throws Exception {

		// Initialize data source required by JPA/Hibernate
		TransactionManager tm = TransactionManager.getInstance();
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);

		try {
			saveMessage(tm, emf);
			updateMessage(tm, emf);
		} finally {
			TransactionManager.rollback();
			emf.close();
		}
	}

	private void saveMessage(TransactionManager tm, EntityManagerFactory emf) throws Exception {

		/*
		 * Get access to the standard transaction API UserTransaction and begin
		 * a transaction on this thread of execution
		 */
		UserTransaction tx = tm.getUserTransaction();
		tx.begin();

		/*
		 * Begin a new SESSION with the database by creating an EntityManager,
		 * this is your context for all persistence operations
		 */
		EntityManager em = emf.createEntityManager();

		// EntityManager cannot access getTransaction() in JTA context
		// EntityTransaction entityTransaction = em.getTransaction();

		/*
		 * Create a new instance of the mapped domain model class Message and
		 * set its text property
		 */
		Message message = new Message();
		message.setText("Hello World JPA!");

		/*
		 * Enlist the transient instance with your persistence context, you make
		 * it persistent. Hibernate now knows that you wish to store that data,
		 * it doesn't necessarily call the database immediately, however
		 */
		// No transaction started on this thread, if JTA transaction is not started
		em.persist(message);

		// Id is assigned after persist, before commit 
		Assertions.assertThat(message.getId()).isNotNull();

		/*
		 * Commit the transaction, Hibernate now automatically checks the
		 * persistence context and executes the necessary SQL INSERT statement
		 */
		tx.commit();
		// INSERT into MESSAGE (ID, TEXT) values (1, 'Hello World JPA!')

		// No select is issued, because message is in the persistence context already
		Message messageDb1 = em.find(Message.class, message.getId());
		Message messageDb2 = em.find(Message.class, message.getId());
		
		Assertions.assertThat(messageDb1).isSameAs(messageDb2);
		Assertions.assertThat(messageDb1.getText()).isEqualTo(message.getText());

		/*
		 * If you create an EntityManager, you must close it
		 */
		em.close();
	}

	private void updateMessage(TransactionManager tm, EntityManagerFactory emf) throws Exception {

		/*
		 * Every interaction with your database should occur within explicit
		 * transaction boundaries, even if you are only reading data
		 */
		UserTransaction tx = tm.getUserTransaction();
		tx.begin();

		/*
		 * Begin a new SESSION with the database by creating an EntityManager,
		 * this is your context for all persistence operations
		 */
		EntityManager em = emf.createEntityManager();

		/*
		 * Execute a query to retrieve all instances of Message from the
		 * database
		 */
		List<Message> messages1 = em.createQuery("SELECT m FROM Message m", Message.class).getResultList();
		// SELECT * from MESSAGE

		// Second way
		CriteriaQuery<Message> criteriaQuery = em.getCriteriaBuilder().createQuery(Message.class);
		Root<Message> fromMessage = criteriaQuery.from(Message.class);
		criteriaQuery.select(fromMessage);
		List<Message> messages2 = em.createQuery(criteriaQuery).getResultList();

		/*
		 * You can change the value of a property, Hibernate will detect this
		 * automatically because the loaded Message is still attached to the
		 * persistence context it was loaded in
		 */
		messages1.get(0).setText("Take me to your JPA!");

		/*
		 * On commit, Hibernate checks the persistence context for dirty state
		 * and executes the SQL UPDATE automatically to synchronize the
		 * in-memory with the database state, e.g. em.persist(messages.get(0));
		 */
		// Without transaction, changes will not be saved, even with em.persist();
		tx.commit();
		// UPDATE MESSAGE set TEXT = 'Take me to your JPA!' where ID = 1

		/*
		 * If you create an EntityManager, you must close it
		 */
		em.close();

		// Assertions
		Assertions.assertThat(messages1).hasSize(1).hasSameSizeAs(messages2);
	}
}
