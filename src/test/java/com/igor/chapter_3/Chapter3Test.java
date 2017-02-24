package com.igor.chapter_3;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.junit.Test;

import com.igor.setup.PersistenceUnit;
import com.igor.setup.TransactionManager;

public class Chapter3Test {

	@Test
	public void testDB() throws Exception {
		try {
			TransactionManager tm = TransactionManager.getInstance();
			EntityManagerFactory emf = PersistenceUnit.getInstance();

			UserTransaction tx = tm.getUserTransaction();
			tx.begin();

			EntityManager em = emf.createEntityManager();

			// Category
			Category category = new Category();
			category.setName("root");

			// Item 1
			Item item1 = new Item();
			item1.setName("item1");
			item1.setBuyNowPrice(BigDecimal.TEN);
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

			em.persist(category);
			em.persist(item1);
			em.persist(item2);
			em.persist(item1Bid1);
			em.persist(item2Bid1);

			tx.commit();
			em.close();
		} finally {
			TransactionManager.rollback();
			PersistenceUnit.close();
		}
	}

}
