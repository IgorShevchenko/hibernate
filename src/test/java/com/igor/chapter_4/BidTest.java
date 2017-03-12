package com.igor.chapter_4;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class BidTest {

	private static final String PERSISTENCE_UNIT = "Chapter4";

	@Test
	public void shouldQueryWithFullClassName() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setName("item");

		Bid bid = new Bid(item, 100);

		client.persist(item, bid);

		client.executeTransaction(em -> {

			// Works even with @Entity(name = "BidEntity")
			String query1 = "SELECT i FROM com.igor.chapter_4.Bid i";
			List<Bid> bidsDb1 = em.createQuery(query1, Bid.class).getResultList();
			Assertions.assertThat(bidsDb1).hasSize(1);

			// Now try with entity name 
			String query2 = "SELECT i FROM BidEntity i";
			List<Bid> bidsDb2 = em.createQuery(query2, Bid.class).getResultList();
			Assertions.assertThat(bidsDb2).hasSize(1);

		});

		client.close();
	}

}
