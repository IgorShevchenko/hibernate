package com.igor.setup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * Provides a database connection pool with the Bitronix JTA transaction manager
 * (http://docs.codehaus.org/display/BTM/Home).
 * <p>
 * Hibernate will look up the datasource and <code>UserTransaction</code>
 * through JNDI, that's why you also need a <code>jndi.properties</code> file. A
 * minimal JNDI context is bundled with and started by Bitronix.
 * </p>
 */
public class TransactionManager {

	private static final String SERVER_ID = "myServer1234";
	private static final String JTA_DATA_SOURCE = "myDS";
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);
	private static final TransactionManager INSTANCE = new TransactionManager();

	public static TransactionManager getInstance() {
		return INSTANCE;
	}

	private final Context context;
	private final PoolingDataSource ds;

	private TransactionManager() {

		// Actually can survive without context
		this.context = createInitialContext();

		LOGGER.info("Starting database connection pool");
		Configuration configuration = TransactionManagerServices.getConfiguration();

		LOGGER.info("Setting stable unique identifier for transaction recovery");
		configuration.setServerId(SERVER_ID);

		LOGGER.info("Disabling JMX binding of manager in unit tests");
		configuration.setDisableJmx(true);

		LOGGER.info("Disabling transaction logging for unit tests");
		configuration.setJournal("null");

		LOGGER.info("Disabling warnings when the database isn't accessed in a transaction");
		configuration.setWarnAboutZeroResourceTransaction(false);

		LOGGER.info("Creating connection pool");
		this.ds = new PoolingDataSource();
		this.ds.setUniqueName(JTA_DATA_SOURCE);
		this.ds.setMinPoolSize(1);
		this.ds.setMaxPoolSize(5);
		this.ds.setPreparedStatementCacheSize(10);

		// Our locking/versioning tests assume READ COMMITTED transaction isolation. 
		// This is not the default on MySQL InnoDB, so we set it here explicitly.
		this.ds.setIsolationLevel("READ_COMMITTED");

		// Hibernate's SQL schema generator calls connection.setAutoCommit(true)
		// and we use auto-commit mode when the EntityManager is in suspended
		// mode and not joined with a transaction.
		this.ds.setAllowLocalTransactions(true);

		LOGGER.info("Setting up database connection");
		MySqlConnection.configure(this.ds);

		LOGGER.info("Initializing transaction and resource management");
		this.ds.init();
	}

	private static InitialContext createInitialContext() {
		try {
			return new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public UserTransaction getUserTransaction() {
		// return TransactionManagerServices.getTransactionManager();
		try {
			return (UserTransaction) this.context.lookup("java:comp/UserTransaction");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public DataSource getDataSource() {
		try {
			String name = this.ds.getUniqueName();
			// Check if ds and this.context.lookup(name) are the same
			return (DataSource) this.context.lookup(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() throws Exception {
		LOGGER.info("Stopping database connection pool");
		this.ds.close();
		TransactionManagerServices.getTransactionManager().shutdown();
	}

	public static void rollback() {
		UserTransaction tx = INSTANCE.getUserTransaction();
		try {
			int status = tx.getStatus();
			if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
				LOGGER.error("Rolling back transaction");
				tx.rollback();
			}
		} catch (Exception e) {
			LOGGER.error("Rollback of transaction failed, trace follows!", e);
		}
	}
}
