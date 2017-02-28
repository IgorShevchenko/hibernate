package com.igor.chapter_3;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

/**
 * Override "category" annotation mappings with JPA XML mappings.
 */
public class CategoryTest {

	private static final String PERSISTENCE_UNIT = "Chapter3_JPA_XML_override";

	@Test
	public void shouldOverrideMetadata() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Category
		Category category = new Category();
		category.setName("Very long category name with length overriden in XML mapping");

		// Save category to the database
		client.persist(category);

		// Load category from the database
		Category categoryDb = client.select(Category.class, category.getId());

		// Category name should be reserved
		Assertions.assertThat(categoryDb.getName()).isEqualTo(category.getName());

		client.close();
	}
}
