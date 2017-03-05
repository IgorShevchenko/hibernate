package com.igor.chapter_2;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorBuilderImpl;
import org.junit.Test;

import com.igor.setup.TransactionManager;

/**
 * Shows raw code to persist\retrieve entity using native Hibernate.
 */
public class HelloWorldHibernate {

	protected SessionFactory unusedSimpleBoot() {
		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
		StandardServiceRegistry serviceRegistry = serviceRegistryBuilder.configure("hibernate.cfg.xml").build();
		Metadata metadata = new MetadataSources(serviceRegistry).buildMetadata();
		SessionFactory sessionFactory = metadata.buildSessionFactory();
		return sessionFactory;
	}

	@Test
	public void shouldPersistAndUpdateMessage() throws Exception {

		SessionFactory sessionFactory = createSessionFactory();

		try {
			saveMessage(sessionFactory);
			updateMessage(sessionFactory);
		} finally {
			TransactionManager.rollback();
		}
	}

	private SessionFactory createSessionFactory() {

		// Initialize data source required by JPA/Hibernate
		TransactionManager.getInstance();

		// This builder helps you create the immutable service registry with chained method calls
		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();

		// Configure the services registry by applying settings
		serviceRegistryBuilder
				.applySetting("hibernate.connection.datasource", "myDS")
				.applySetting("hibernate.format_sql", "true")
				.applySetting("hibernate.use_sql_comments", "true")
				.applySetting("hibernate.hbm2ddl.auto", "create-drop");

		// Enable JTA (this is a bit crude because Hibernate devs still believe that JTA is
		// used only in monstrous application servers and you'll never see this code)
		serviceRegistryBuilder.applySetting(
				Environment.TRANSACTION_COORDINATOR_STRATEGY,
				JtaTransactionCoordinatorBuilderImpl.class);
		StandardServiceRegistry serviceRegistry = serviceRegistryBuilder.build();

		// You can only enter this configuration stage with an existing service registry
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);

		// Add your persistent classes to the (mapping) metadata sources
		metadataSources.addAnnotatedClass(Message.class);

		// Add hbm.xml mapping files
		// metadataSources.addFile(...);

		// Read all hbm.xml mapping files from a JAR
		// metadataSources.addJar(...);

		MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
		Metadata metadata = metadataBuilder.build();
		Assertions.assertThat(metadata.getEntityBindings()).hasSize(1);

		SessionFactory sessionFactory = metadata.buildSessionFactory();
		return sessionFactory;
	}

	private void saveMessage(SessionFactory sessionFactory) throws Exception {

		TransactionManager tm = TransactionManager.getInstance();

		/*
		 * Get access to the standard transaction API UserTransaction and begin
		 * a transaction on this thread of execution
		 */
		UserTransaction tx = tm.getUserTransaction();
		tx.begin();

		/*
		 * Whenever you call getCurrentSession() in the same thread you get the
		 * same org.hibernate.Session. It's bound automatically to the ongoing
		 * transaction and is closed for you automatically when that transaction
		 * commits or rolls back
		 */
		Session session = sessionFactory.getCurrentSession();

		// Don't need to use session.getTransaction() in JTA context
		// Transaction transaction = session.getTransaction();

		/*
		 * Create a new instance of the mapped domain model class Message and
		 * set its text property
		 */
		Message message = new Message();
		message.setText("Hello World Hibernate!");

		/*
		 * The native Hibernate API is very similar to the standard Java
		 * Persistence API and most methods have the same name
		 */
		session.persist(message);
		
		// Id is assigned after persist, before commit 
		Assertions.assertThat(message.getId()).isNotNull();

		/*
		 * Hibernate synchronizes the session with the database and closes the
		 * "current" session on commit of the bound transaction automatically
		 */
		tx.commit();
		// INSERT into MESSAGE (ID, TEXT) values (1, 'Hello World Hibernate!')

		// IllegalStateException: no transaction started on this thread
		// Message messageDb = session.find(Message.class, message.getId());
		
		// Not needed
		// session.close();
	}

	private void updateMessage(SessionFactory sessionFactory) throws Exception {

		TransactionManager tm = TransactionManager.getInstance();

		/*
		 * Every interaction with your database should occur within explicit
		 * transaction boundaries, even if you are only reading data
		 */
		UserTransaction tx = tm.getUserTransaction();
		tx.begin();

		/*
		 * Whenever you call getCurrentSession() in the same thread you get the
		 * same org.hibernate.Session. It's bound automatically to the ongoing
		 * transaction and is closed for you automatically when that transaction
		 * commits or rolls back
		 */
		// Unable to locate current JTA transaction, if JTA transaction is not started
		Session session = sessionFactory.getCurrentSession();

		/*
		 * A Hibernate criteria query is a type-safe programmatic way to express
		 * queries, automatically translated into SQL
		 */
		// Deprecated:
		// List<Message> messages = sessionFactory.getCurrentSession().createCriteria(Message.class).list();

		// One way
		List<Message> messages1 = session.createQuery("SELECT m FROM Message m", Message.class).getResultList();
		// SELECT * from MESSAGE

		// Second way
		CriteriaQuery<Message> criteriaQuery = session.getCriteriaBuilder().createQuery(Message.class);
		Root<Message> fromMessage = criteriaQuery.from(Message.class);
		criteriaQuery.select(fromMessage);
		List<Message> messages2 = session.createQuery(criteriaQuery).getResultList();

		/*
		 * You can change the value of a property, Hibernate will detect this
		 * automatically because the loaded Message is still attached to the
		 * persistence context it was loaded in
		 */
		messages1.get(0).setText("Take me to your Hibernate!");

		/*
		 * On commit, Hibernate checks the persistence context for dirty state
		 * and executes the SQL UPDATE automatically to synchronize the
		 * in-memory with the database state, e.g. em.persist(messages.get(0));
		 */
		tx.commit();
		// UPDATE MESSAGE set TEXT = 'Take me to your Hibernate!' where ID = 1

		// Assertions
		Assertions.assertThat(messages1).hasSize(1).hasSameSizeAs(messages2);
	}
}
