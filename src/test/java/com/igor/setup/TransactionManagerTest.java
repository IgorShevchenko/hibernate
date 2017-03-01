package com.igor.setup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TransactionManagerTest {

	@Test
	public void shouldRegisterWithJndi() throws NamingException {

		TransactionManager manager = TransactionManager.getInstance();
		Assertions.assertThat(manager).isNotNull();

		Context context = new InitialContext();
		Object transaction = context.lookup("java:comp/UserTransaction");
		Object dataSource = context.lookup("myDS");

		System.out.println(transaction);
		System.out.println(dataSource);

		Assertions.assertThat(transaction).isNotNull();
		Assertions.assertThat(dataSource).isNotNull();
	}

}
