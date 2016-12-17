package com.igor.setup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class TransactionManager {

	private static final String SERVER_ID = "myServer1234";
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);
	private static final TransactionManager INSTANCE = new TransactionManager();

	public static TransactionManager getInstance() {
		return INSTANCE;
	}

	private final Context context;
	private final PoolingDataSource ds;

	private TransactionManager() {

		this.context = createInitialContext();

		LOGGER.info("Starting database connection pool");
		LOGGER.info("Setting stable unique identifier for transaction recovery");
		TransactionManagerServices.getConfiguration().setServerId(SERVER_ID);

		LOGGER.info("Disabling JMX binding of manager in unit tests");
		TransactionManagerServices.getConfiguration().setDisableJmx(true);

		LOGGER.info("Disabling transaction logging for unit tests");
		TransactionManagerServices.getConfiguration().setJournal("null");

		LOGGER.info("Disabling warnings when the database isn't accessed in a transaction");
		TransactionManagerServices.getConfiguration().setWarnAboutZeroResourceTransaction(false);

		LOGGER.info("Creating connection pool");
		this.ds = new PoolingDataSource();
		this.ds.setUniqueName(PersistenceUnit.JTA_DATA_SOURCE);
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
		try {
			return (UserTransaction) this.context.lookup("java:comp/UserTransaction");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public DataSource getDataSource() {
		try {
			return (DataSource) this.context.lookup(PersistenceUnit.JTA_DATA_SOURCE);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void rollback() {
		UserTransaction tx = getUserTransaction();
		try {
			if (tx.getStatus() == Status.STATUS_ACTIVE || tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
				tx.rollback();
		} catch (Exception ex) {
			System.err.println("Rollback of transaction failed, trace follows!");
			ex.printStackTrace(System.err);
		}
	}

	public void stop() throws Exception {
		LOGGER.trace("Stopping database connection pool");
		this.ds.close();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
}
