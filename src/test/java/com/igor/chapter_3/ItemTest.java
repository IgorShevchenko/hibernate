package com.igor.chapter_3;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.junit.Test;

import com.igor.chapter_3.Bid;
import com.igor.chapter_3.Item;
import com.igor.setup.PersistenceUnit;
import com.igor.setup.TransactionManager;

public class ItemTest {

	@Test
	public void testDB() throws Exception {
		try {
			TransactionManager tm = TransactionManager.getInstance();
			EntityManagerFactory emf = PersistenceUnit.getInstance();

			UserTransaction tx = tm.getUserTransaction();
			tx.begin();

			EntityManager em = emf.createEntityManager();

			// Create bid
			Bid bid1 = new Bid();
			bid1.setPrice(100);

			Bid bid2 = new Bid();
			bid2.setPrice(200);

			Set<Bid> bids = new HashSet<>();
			bids.add(bid1);
			bids.add(bid2);

			// Create item and assign bid
			Item item = new Item();
			item.setBids(bids);

			em.persist(bid1);
			em.persist(bid2);
			em.persist(item);
			tx.commit();
			em.close();
		} finally {
			TransactionManager.rollback();
			PersistenceUnit.close();
		}
	}
}
