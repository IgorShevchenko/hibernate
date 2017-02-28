package com.igor.setup;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;

import com.igor.chapter_3.Category;

public class DbTestClientTest {

	private static final String PERSISTENCE_UNIT = "Chapter3";

	@Test
	public void shouldRollbackTransaction() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Category
		Category category = new Category();
		category.setName("root");

		try {

			ThrowingCallable throwing = () -> {

				// Persist with with mid-transaction exception
				client.executeTransaction((em) -> {

					em.persist(category);
					em.flush();
					Assertions.assertThat(category.getId()).isNotNull();

					throw new IllegalStateException("Exception inside transaction");
				});
			};

			Assertions.assertThatThrownBy(throwing).isInstanceOf(IllegalStateException.class);

			List<Category> categoriesDb = client.selectAll(Category.class);
			Assertions.assertThat(categoriesDb).isEmpty();

		} finally {
			client.close();
		}
	}

}
