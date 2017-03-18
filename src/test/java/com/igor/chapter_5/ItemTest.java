package com.igor.chapter_5;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

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
		// select a, b, (formula) FROM ... WHERE ...

		Item itemDb = client.find(Item.class, item.getId());
		Assertions.assertThat(itemDb.getBidCount()).isEqualTo(1);

		client.close();
	}

	@Test
	public void shouldComputeFormulaOnQuery() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(100.0);

		Bid bid = new Bid();
		bid.setItem(item);
		bid.setAmount(10.0);

		client.persist(item, bid);

		client.executeTransaction(em -> {

			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<Item> query = builder.createQuery(Item.class);

			Root<Item> fromItem = query.from(Item.class);
			Path<Integer> idPath = fromItem.get(Item_.id);

			query.where(builder.equal(idPath, item.getId()));
			query.select(fromItem);

			// Execute
			Item itemDb = em.createQuery(query).getSingleResult();
			Assertions.assertThat(itemDb.getBidCount()).isEqualTo(1);
			// select a, b, (SELECT count(*) FROM bid b WHERE b.item_id = id) as
			// formula ... FROM ...
		});

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

	@Test
	public void shouldApplyColumnTransformer() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(10.0);
		item.setWeight(10);

		client.persist(item);
		// insert into Item (price, weight_pounds, id) values (?, ? / 2, ?)

		Item itemDb = client.find(Item.class, item.getId());
		Assertions.assertThat(itemDb.getWeight()).isEqualTo(10);
		// select id, price, weight_pounds * 2, (SELECT count(*) FROM bid b
		// WHERE b.item_id = id) as formula from Item i where i.id = ?

		client.close();
	}

	@Test
	public void shouldApplyColumnTransformerOnQuery() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(10.0);
		item.setWeight(10);

		client.persist(item);
		// insert into Item (price, weight_pounds, id) values (?, ? / 2, ?)

		client.executeTransaction(em -> {

			TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE weight = 10", Item.class);
			Item itemDb = query.getSingleResult();
			// where weight_pounds * 2 = 10

			Assertions.assertThat(itemDb).isNotNull();
		});

		client.close();
	}

	@Test
	public void shouldRetrieveGeneratedValueAfterUpdate() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(10.0);
		item.setWeight(10);

		// Ignored, non insertable
		item.setAmount(12345);

		client.persist(item);

		client.executeTransaction(em -> {

			Item itemDb = em.find(Item.class, item.getId());
			itemDb.setWeight(200);
			
			// Has default value, still nullable
			itemDb.setAmount(null);

			// 3 SQL queries:
			// 1) Get item
			// 2) Dirty state updated. Generated values are not in UPDATE
			// 3) Generated values are retrieved
		});

		client.close();
	}

}
