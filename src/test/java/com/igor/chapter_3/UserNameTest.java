package com.igor.chapter_3;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class UserNameTest {

	private static final String PERSISTENCE_UNIT = "Chapter3";
	
	/**
	 * Database stores the name of a user as a single NAME column, but UserName
	 * class has separate firstName and lastName fields.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldStoreInDB() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		UserName userName = new UserName();
		userName.setName("Igor Shevchenko");

		// Save user to the database
		client.persist(userName);

		// Load user from the database
		UserName userNameDb = client.select(UserName.class, userName.getId());

		// AccessType.PROPERTY executes setter, so values are set
		String firstNameDb = userNameDb.getFirstName();
		String lastNameDb = userNameDb.getLastName();

		// Assertions
		Assertions.assertThat(firstNameDb).isEqualTo("Igor");
		Assertions.assertThat(lastNameDb).isEqualTo("Shevchenko");

		client.close();
	}

}
