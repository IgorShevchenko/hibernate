package com.igor.chapter_5;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.hibernate.PropertyValueException;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class ItemTest {

	private static final String PERSISTENCE_UNIT = "Chapter5";

	@Test
	public void shouldCatchNullInApplication() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(null);

		// Should not hit the database
		ThrowingCallable throwing = () -> client.persist(item);
		Assertions.assertThatThrownBy(throwing).hasCauseInstanceOf(PropertyValueException.class);

		client.close();
	}

	@Test
	public void shouldComputeFormulaZeroBids() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(100.0);

		client.persist(item);

		Item itemDb = client.find(Item.class, item.getId());
		Assertions.assertThat(itemDb.getBidCount()).isEqualTo(0);

		client.close();
	}

	@Test
	public void shouldComputeFormulaOneBid() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(100.0);

		Bid bid = new Bid();
		bid.setItem(item);
		bid.setAmount(10.0);

		client.persist(item, bid);

		Item itemDb = client.find(Item.class, item.getId());
		Assertions.assertThat(itemDb.getBidCount()).isEqualTo(1);

		client.close();
	}

	@Test
	public void shouldNotComputeFormulaTwice() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(100.0);

		Bid bid = new Bid();
		bid.setItem(item);
		bid.setAmount(10.0);

		client.persist(item, bid);

		client.executeTransaction(em -> {

			Item itemDb1 = em.find(Item.class, item.getId());
			Item itemDb2 = em.find(Item.class, item.getId());
			Assertions.assertThat(itemDb1).isSameAs(itemDb2);

			// Formula is computed only once
		});

		client.close();
	}

}
