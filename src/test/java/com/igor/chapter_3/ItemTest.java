package com.igor.chapter_3;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class ItemTest {

	/**
	 * Declares only class definition in persistence unit, no XML files.
	 * Includes package-info.java
	 */
	private static final String PERSISTENCE_UNIT = "Chapter3";

	/**
	 * Declares <b>metadata complete</b> JPA XML mapping files: global
	 * persistence metadata XML file, item metadata XML file, and named queries
	 * XML file.
	 */
	private static final String PERSISTENCE_UNIT_JPA_XML = "Chapter3_JPA_XML";

	/**
	 * Defines only "item" HMB XML file.
	 */
	private static final String PERSISTENCE_UNIT_HMB_XML = "Chapter3_HBM_XML";

	@Test
	public void shouldLinkBidAndItem() {
		Item anItem = new Item();
		Bid aBid = new Bid();

		anItem.getBids().add(aBid);
		aBid.setItem(anItem);

		Assertions.assertThat(anItem.getBids()).containsOnly(aBid);
		Assertions.assertThat(aBid.getItem()).isEqualTo(anItem);

		// Again with convenience method
		Bid secondBid = new Bid();
		anItem.addBid(secondBid);

		Assertions.assertThat(anItem.getBids()).containsOnly(aBid, secondBid);
		Assertions.assertThat(secondBid.getItem()).isEqualTo(anItem);
	}

	/**
	 * Don't need to write this code. Usually is verified by UI, UI-Validation
	 * integration.
	 */
	@Test
	public void shouldValidateItem() {

		// Setup
		Item item = new Item();

		// Too short name
		item.setName("a");

		// Date in the past
		item.setAuctionEnd(new Date(100000));

		// Action
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<Item>> violations = validator.validate(item);

		// Assertion
		Assertions.assertThat(violations).hasSize(2);
		Assert.assertThat(violations.size(), CoreMatchers.is(2));

		ConstraintViolation<Item> violation = violations.iterator().next();
		String failedPropertyName = violation.getPropertyPath().iterator().next().getName();

		Assertions.assertThat(failedPropertyName).isEqualTo("auctionEnd");

		if (Locale.getDefault().getLanguage().equals("en")) {
			Assertions.assertThat(violation.getMessage()).isEqualTo("must be in the future");
		}
	}

	@Test
	public void shouldStoreInDB() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		// Category
		Category category = new Category();
		category.setName("root");

		// Item 1
		Item item1 = new Item();
		item1.setName("item1");
		item1.setBuyNowPrice(BigDecimal.TEN);
		item1.setAuctionEnd(new Date(System.currentTimeMillis() + 10000));

		item1.setCategory(category);

		// Item 1 Bid
		Bid item1Bid1 = new Bid();
		item1Bid1.setAmount(BigDecimal.valueOf(5));
		item1.addBid(item1Bid1);

		// Item 2
		Item item2 = new Item();
		item2.setName("item2");
		item2.setBuyNowPrice(BigDecimal.valueOf(20));
		item2.setCategory(null);
		item2.version = 5;

		// Item 2 Bid
		Bid item2Bid1 = new Bid();
		item2Bid1.setAmount(BigDecimal.valueOf(10));
		item2.addBid(item2Bid1);

		// Save items to the database, order is important
		client.persist(category, item1, item2, item1Bid1, item2Bid1);

		// Load items from the database
		client.executeTransaction((em) -> {
			List<Item> itemsDb = em.createQuery("SELECT i FROM Item i ORDER BY i.id", Item.class).getResultList();

			Item itemDb1 = itemsDb.get(0);

			// Lazy loaded, initialized as a proxy. Select is not yet executed 
			Category itemDb1Category = itemDb1.getCategory();
			Date itemDb1AuctionEnd = itemDb1.getAuctionEnd();

			// Select to fetch category is executed on getId()
			Long categoryId = itemDb1Category.getId();
			String categoryName = itemDb1Category.getName();

			Assertions.assertThat(categoryId).isEqualTo(category.getId());
			Assertions.assertThat(categoryName).isEqualTo(category.getName());
			Assertions.assertThat(itemDb1AuctionEnd).isEqualTo(item1.getAuctionEnd());
		});

		// Verify if named queries form package-info.java are visible
		List<Item> itemsQuery = client.executeNamedQuery("findItemsOrderByName_package", Item.class);
		Assertions.assertThat(itemsQuery).isNotEmpty();

		client.close();
	}

	@Test
	public void shouldUseJpaXmlMetadata() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML);

		// Item
		Item item = new Item();
		item.version = 5;
		item.setName("item");
		item.setBuyNowPrice(BigDecimal.TEN);
		item.setAuctionEnd(new Date(System.currentTimeMillis() + 10000));

		// Save item to the database
		client.persist(item);

		// Load item from the database
		Item itemDb = client.select(Item.class, item.getId());

		Assertions.assertThat(itemDb.version).isEqualTo(item.version);
		Assertions.assertThat(itemDb.getName()).isEqualTo(item.getName());
		Assertions.assertThat(itemDb.getAuctionEnd()).isEqualTo(item.getAuctionEnd());

		// BigDecimals are checked by compareTo
		Assertions.assertThat(itemDb.getBuyNowPrice()).isEqualByComparingTo(item.getBuyNowPrice());

		// Check results with NAMED QUERY
		List<Item> itemsQuery1 = client.executeNamedQuery("findItems_queries.xml", Item.class);
		List<Item> itemsQuery2 = client.executeNamedQuery("findItemsWithHints_queries.xml", Item.class);
		Assertions.assertThat(itemsQuery1).hasSameSizeAs(itemsQuery2);

		client.close();
	}

	@Test
	public void shouldNotSeePackageInfoJava() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML);

		ThrowingCallable throwing = () -> {
			client.executeNamedQuery("findItemsOrderByName_package", Item.class);
		};

		Assertions.assertThatThrownBy(throwing).isInstanceOf(IllegalArgumentException.class);

		client.close();
	}

	@Test
	public void shouldUseHbmXmlMetadata() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_HMB_XML);

		// Only category is mapped
		Assertions.assertThat(client.getMetamodel().getManagedTypes()).hasSize(1);

		// Item
		Item item = new Item();
		item.version = 5;
		item.setName("item");
		item.setBuyNowPrice(BigDecimal.TEN);
		item.setAuctionEnd(new Date(System.currentTimeMillis() + 10000));

		// Save item to the database
		client.persist(item);

		// Load item from the database
		Item itemDb = client.select(Item.class, item.getId());

		// Version is not mapped, not persisted, default value 
		Assertions.assertThat(itemDb.version).isNotEqualTo(item.version).isEqualTo(1);

		// BuyNowPrice is not mapped, not persisted
		Assertions.assertThat(itemDb.getBuyNowPrice()).isNull();

		Assertions.assertThat(itemDb.getName()).isEqualTo(item.getName());
		Assertions.assertThat(itemDb.getAuctionEnd()).isEqualTo(item.getAuctionEnd());

		// Check results with NAMED QUERY
		List<Item> itemsQuery1 = client.executeNamedQuery("findItems_HBMXML", Item.class);
		List<Item> itemsQuery2 = client.executeNamedQuery("findItemsOrderByIdDesc_HBMXML", Item.class);
		Assertions.assertThat(itemsQuery1).hasSameSizeAs(itemsQuery2).hasSize(1);

		client.close();
	}

}
