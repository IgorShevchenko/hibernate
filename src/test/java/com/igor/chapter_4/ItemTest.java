package com.igor.chapter_4;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class ItemTest {

	private static final String PERSISTENCE_UNIT = "Chapter4";

	@Test
	public void shouldUseCustomIdGenerator() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item1 = new Item();
		Item item2 = new Item();

		item1.setName("item-1");
		item2.setName("item-2");

		client.persist(item1, item2);

		List<Item> items = client.selectAll(Item.class);

		Assertions.assertThat(items).hasSize(2);
		Assertions.assertThat(items.get(0).getId()).isEqualTo(1000);
		Assertions.assertThat(items.get(1).getId()).isEqualTo(1001);

		client.close();
	}

}
