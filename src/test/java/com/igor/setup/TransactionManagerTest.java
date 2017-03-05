package com.igor.setup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TransactionManagerTest {

	@Test
	public void shouldRegisterWithJndi() throws NamingException {

		TransactionManager manager = TransactionManager.getInstance();
		Assertions.assertThat(manager).isNotNull();

		UserTransaction userTransaction = manager.getUserTransaction();
		DataSource dataSource = manager.getDataSource();

		// This is how JTA transaction manager retrieves thread-based transaction
		Context context = new InitialContext();
		Object userTransactionContext = context.lookup("java:comp/UserTransaction");
		Object dataSourceContext = context.lookup("myDS");

		System.out.println(userTransaction);
		System.out.println(userTransactionContext);
		System.out.println(dataSource);
		System.out.println(dataSourceContext);

		Assertions.assertThat(userTransactionContext).isNotNull();
		Assertions.assertThat(dataSourceContext).isNotNull();
		Assertions.assertThat(userTransaction).isEqualTo(userTransactionContext);
		Assertions.assertThat(dataSource).isEqualTo(dataSourceContext);
	}

}
