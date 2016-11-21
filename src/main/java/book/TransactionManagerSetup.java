package book;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class TransactionManagerSetup {

	public static final String DATASOURCE_NAME = "myDS2";
	public static final Logger LOGGER = Logger.getLogger(TransactionManagerSetup.class.getName());

	public final Context context = new InitialContext();
	public final PoolingDataSource datasource;
	public final DatabaseProduct databaseProduct;

	public TransactionManagerSetup(DatabaseProduct databaseProduct) throws NamingException {
		this(databaseProduct, null);
	}

	public TransactionManagerSetup(DatabaseProduct databaseProduct, String connectionURL) throws NamingException {

		Logger rootLog = Logger.getLogger("");
		rootLog.setLevel( Level.FINE );
		rootLog.getHandlers()[0].setLevel( Level.FINE ); // Default console handler

		LOGGER.fine("Starting database connection pool");

		LOGGER.fine("Setting stable unique identifier for transaction recovery");
		TransactionManagerServices.getConfiguration().setServerId("myServer1234");

		LOGGER.fine("Disabling JMX binding of manager in unit tests");
		TransactionManagerServices.getConfiguration().setDisableJmx(true);

		LOGGER.fine("Disabling transaction logging for unit tests");
		TransactionManagerServices.getConfiguration().setJournal("null");

		LOGGER.fine("Disabling warnings when the database isn't accessed in a transaction");
		TransactionManagerServices.getConfiguration().setWarnAboutZeroResourceTransaction(false);

		LOGGER.fine("Creating connection pool");
		this.datasource = new PoolingDataSource();
		this.datasource.setUniqueName(DATASOURCE_NAME);
		this.datasource.setMinPoolSize(1);
		this.datasource.setMaxPoolSize(5);
		this.datasource.setPreparedStatementCacheSize(10);

		// Our locking/versioning tests assume READ COMMITTED transaction
		// isolation. This is not the default on MySQL InnoDB, so we set
		// it here explicitly.
		this.datasource.setIsolationLevel("READ_COMMITTED");

		// Hibernate's SQL schema generator calls connection.setAutoCommit(true)
		// and we use auto-commit mode when the EntityManager is in suspended
		// mode and not joined with a transaction.
		this.datasource.setAllowLocalTransactions(true);

		LOGGER.info("Setting up database connection: " + databaseProduct);
		this.databaseProduct = databaseProduct;
		databaseProduct.configuration.configure(datasource, connectionURL);

		LOGGER.fine("Initializing transaction and resource management");
		this.datasource.init();
	}

	public Context getNamingContext() {
		return context;
	}

	public UserTransaction getUserTransaction() {
		try {
			return (UserTransaction) getNamingContext()
					.lookup("java:comp/UserTransaction");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public DataSource getDataSource() {
		try {
			return (DataSource) getNamingContext().lookup(DATASOURCE_NAME);
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
		LOGGER.fine("Stopping database connection pool");
		datasource.close();
		TransactionManagerServices.getTransactionManager().shutdown();
	}
}
