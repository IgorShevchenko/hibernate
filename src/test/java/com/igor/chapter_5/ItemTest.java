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

		ThrowingCallable throwing = () -> client.persist(item);
		Assertions.assertThatThrownBy(throwing).hasCauseInstanceOf(PropertyValueException.class);

		client.close();
	}

}
