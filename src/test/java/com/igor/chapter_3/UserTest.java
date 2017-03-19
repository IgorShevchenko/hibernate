package com.igor.chapter_3;

import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.transaction.RollbackException;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class UserTest {

	private static final String PERSISTENCE_UNIT = "Chapter3";

	@Test
	public void shouldStoreInDB() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		City homeCity = new City("Home city", "Home country");
		City billingCity = new City("Work city", "Work country");

		Address homeAddress = new Address("Home street", "11111", homeCity);
		Address billingAddress = new Address("Work street", "22222", billingCity);

		User user = new User();
		user.setUsername("Igor");
		user.setHomeAddress(homeAddress);
		user.setBillingAddress(billingAddress);

		// Save user to the database
		Assertions.assertThat(user.getId()).isNull();
		client.persist(user);
		Assertions.assertThat(user.getId()).isNotNull();

		// Load user from the database
		List<User> usersDb = client.selectAll(User.class);

		// Assertion
		Assertions.assertThat(usersDb).hasSize(1);
		User userDb = usersDb.get(0);

		Assertions.assertThat(userDb).isEqualToComparingFieldByFieldRecursively(user);
		Assertions.assertThat(userDb.getBillingAddress().getCity().getName()).isEqualTo(billingCity.getName());

		// Retrieve property of the embedded component by query
		client.executeTransaction(em -> {

			// Nested access: u.homeAddress.city.name
			// Though city.name is stored in "city"
			String qlQuery = "SELECT u.homeAddress.city.name FROM User u WHERE u.id = :id";
			TypedQuery<String> query = em.createQuery(qlQuery, String.class);
			query.setParameter("id", user.getId());
			String cityName = query.getSingleResult();

			Assertions.assertThat(cityName).isEqualTo(homeCity.getName());
		});

		client.close();
	}

	@Test
	public void shouldStoreNullBillingAddress() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Home address can't be null
		City homeCity = new City("Home city", "Home country");
		Address homeAddress = new Address("Home street", "11111", homeCity);

		// Billing address is null itself -> get null
		Address billingAddress = null;

		User user = new User();
		user.setUsername("Igor");
		user.setHomeAddress(homeAddress);
		user.setBillingAddress(billingAddress);

		client.persist(user);

		User userDb = client.find(User.class, user.getId());

		// Assertion
		// Billing address is NULL
		Assertions.assertThat(userDb.getHomeAddress()).isNotNull();
		Assertions.assertThat(userDb.getBillingAddress()).isNull();

		client.close();
	}

	@Test
	public void shouldStoreEmptyBillingAddress() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Home address can't be null
		City homeCity = new City("Home city", "Home country");
		Address homeAddress = new Address("Home street", "11111", homeCity);

		// Billing address is not null, but fields are nulls -> get null
		Address billingAddress = new Address(null, null, null);

		User user = new User();
		user.setUsername("Igor");
		user.setHomeAddress(homeAddress);
		user.setBillingAddress(billingAddress);

		client.persist(user);

		// On a user billing address is not null
		Assertions.assertThat(user.getBillingAddress()).isNotNull();

		User userDb = client.find(User.class, user.getId());

		// Assertion
		// Billing address is NULL, AGAIN!
		Assertions.assertThat(userDb.getHomeAddress()).isNotNull();
		Assertions.assertThat(userDb.getBillingAddress()).isNull();

		// Verify dirty checking
		client.executeTransaction(em -> {

			User userDb2 = em.find(User.class, user.getId());
			userDb2.setBillingAddress(billingAddress);

			// No UPDATE is executed
		});

		client.close();
	}

	@Test
	public void shouldFailToPersist() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Null is not allowed for the home city
		Address homeAddress = new Address("Home street", "11111", null);
		Address billingAddress = new Address(null, null, null);

		User user = new User();
		user.setUsername("Igor");
		user.setHomeAddress(homeAddress);
		user.setBillingAddress(billingAddress);

		// Save user to the database
		try {

			// Hibernate will rollback transaction automatically
			// We receive RollbackException
			client.persist(user);

			// With em.flush() our finally-rollback will handle it
			Assertions.fail("Should not reach");

		} catch (RollbackException e) {
			Assertions.assertThat(e).hasCauseInstanceOf(PersistenceException.class);
			Assertions.assertThat(e.getCause()).hasCauseInstanceOf(ConstraintViolationException.class);
		}

		client.close();
	}

}
