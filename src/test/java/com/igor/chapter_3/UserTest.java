package com.igor.chapter_3;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class UserTest {

	@Test
	public void shouldStoreInDB() throws Exception {

		DbTestClient client = new DbTestClient();

		User user = new User();
		user.setUsername("Igor");

		Address homeAddress = new Address("Home street", "11111", "Home city");
		user.setHomeAddress(homeAddress);

		Address billingAddress = new Address("Work street", "22222", "Work city");
		user.setBillingAddress(billingAddress);

		// Save user to the database
		Assertions.assertThat(user.getId()).isNull();
		client.persist(user);
		Assertions.assertThat(user.getId()).isNotNull();

		// Load user from the database
		List<User> usersDb = client.selectAll(User.class);

		// Assertion
		Assertions.assertThat(usersDb).hasSize(1);
		Assertions.assertThat(usersDb.get(0)).isEqualToComparingFieldByFieldRecursively(user);

		client.close();
	}

}
