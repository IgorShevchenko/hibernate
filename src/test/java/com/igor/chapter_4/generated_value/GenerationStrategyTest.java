package com.igor.chapter_4.generated_value;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

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
