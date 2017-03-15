package com.igor.chapter_4.generated_value;

import java.util.List;

import javax.persistence.PersistenceException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

import bitronix.tm.internal.BitronixRollbackException;

public class GenerationStrategyTest {

	private static final String PERSISTENCE_UNIT = "Chapter4_generated_value";

	@Test
	public void itemIdWithoutGeneratedValue() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		ItemNaturalId item1 = new ItemNaturalId();
		ItemNaturalId item2 = new ItemNaturalId();

		item1.setId(10);
		item2.setId(20);

		item1.setName("item-1");
		item2.setName("item-2");

		client.persist(item1, item2);

		List<ItemNaturalId> items = client.selectAll(ItemNaturalId.class);
		Assertions.assertThat(items).hasSize(2);

		// Try to update natural key
		// Updating primary key is NOT allowed
		try {
			client.executeTransaction(em -> {

				ItemNaturalId itemDb = em.find(ItemNaturalId.class, item1.getId());
				Assertions.assertThat(itemDb).isNotNull();

				itemDb.setId(30);

				em.persist(itemDb);
			});
			Assertions.fail("Should throw exception");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(BitronixRollbackException.class)
					.hasCauseInstanceOf(PersistenceException.class);
		}

		client.close();
	}

	@Test
	public void itemWithTableStrategy() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		ItemTable item = new ItemTable();
		item.setName("item-name");

		client.executeTransaction(em -> {

			em.persist(item);

			// Id is assigned before commit
			Assertions.assertThat(item.getId()).isNotNull();

		});

		ItemTable itemDb = client.select(item.getClass(), item.getId());
		Assertions.assertThat(itemDb.getName()).isEqualTo(item.getName());

		client.close();
	}

	@Test
	public void itemWithIdentityStrategy() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		ItemIdentity item = new ItemIdentity();
		item.setName("item-name");

		client.executeTransaction(em -> {

			em.persist(item);
			// INSERT is executed

			// Id is assigned before commit
			Assertions.assertThat(item.getId()).isNotNull();
		});

		ItemIdentity itemDb = client.select(item.getClass(), item.getId());
		Assertions.assertThat(itemDb.getName()).isEqualTo(item.getName());

		client.close();
	}
}
