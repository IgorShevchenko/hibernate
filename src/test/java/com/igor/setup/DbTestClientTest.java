package com.igor.setup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.junit.Test;

import com.igor.chapter_3.Category;
import com.igor.chapter_3.Item;
import com.igor.chapter_3.Item_;

import bitronix.tm.internal.BitronixRollbackException;

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

	@Test
	public void shouldUseJtaTransactions() {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		ThrowingCallable throwing = () -> {
			client.executeTransaction((em) -> {

				// A JTA EntityManager cannot use getTransaction()
				em.getTransaction();
			});
		};

		Assertions.assertThatThrownBy(throwing).isInstanceOf(IllegalStateException.class);
		client.close();
	}

	@Test
	public void retrieveReference() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		PersistenceUnitUtil persistenceUnitUtil = client.getPersistenceUnitUtil();

		Category category = new Category();
		category.setName("root");

		Item item = new Item();
		item.setName("Some Item");
		item.setCategory(category);

		client.persist(category, item);

		EntityManagerFactory emf = client.getEntityManagerFactory();
		UserTransaction tx = client.getUserTransaction();

		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			/*
			 * If the persistence context already contains an Item with the
			 * given identifier, that Item instance is returned by
			 * getReference() without hitting the database. Furthermore, if no
			 * persistent instance with that identifier is currently managed, a
			 * hollow placeholder will be produced by Hibernate, a proxy. This
			 * means getReference() will not access the database, and it doesn't
			 * return null, unlike find()
			 */
			Item itemReference = em.getReference(Item.class, item.getId());
			// No SQL is executed

			/*
			 * JPA offers PersistenceUnitUtil helper methods such as isLoaded()
			 * to detect if you are working with an uninitialized proxy
			 */
			boolean isLoadedItem = persistenceUnitUtil.isLoaded(itemReference);
			Assertions.assertThat(isLoadedItem).isFalse();

			Long itemReferenceId = itemReference.getId();
			// SELECT is executed even on getId()

			isLoadedItem = persistenceUnitUtil.isLoaded(itemReference);
			Assertions.assertThat(isLoadedItem).isTrue();

			/*
			 * Hibernate has a convenient static initialize() method, loading
			 * the proxy's data. But this does not load lazy fetched category.
			 */
			Hibernate.initialize(item);

			// Still does not initialize lazy fetched category, cause not a proxy
			Hibernate.initialize(item.getCategory());

			/*
			 * Item category (lazy fetch) is not loaded
			 */
			boolean isLoadedItemCategory = persistenceUnitUtil.isLoaded(itemReference, Item_.category.getName());
			Assertions.assertThat(isLoadedItemCategory).isFalse();

			String nameDb = itemReference.getName();
			// SELECT is not executed

			Category categoryDb = itemReference.getCategory();

			Assertions.assertThat(itemReferenceId).isEqualTo(item.getId());
			Assertions.assertThat(nameDb).isEqualTo(item.getName());
			Assertions.assertThat(categoryDb.getName()).isEqualTo(category.getName());
			// SELECT is executed on getName()

			tx.commit();
			em.close();
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void retrieveReferenceNoSuchItem() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		EntityManagerFactory emf = client.getEntityManagerFactory();
		UserTransaction tx = client.getUserTransaction();
		Long itemId = 999L;

		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			/*
			 * As soon as you call any method such as Item#getName() on the
			 * proxy, a SELECT is executed to fully initialize the placeholder.
			 * The exception to this rule is a method that is a mapped database
			 * identifier getter method, such as getId(). A proxy might look
			 * like the real thing but it is only a placeholder carrying the
			 * identifier value of the entity instance it represents. If the
			 * database record doesn't exist anymore when the proxy is
			 * initialized, an EntityNotFoundException will be thrown
			 */
			Item itemReference = em.getReference(Item.class, itemId);
			// No SQL is executed

			itemReference.getId();
			// SELECT is executed even on getId()

			Assertions.fail("Should throw exception");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(EntityNotFoundException.class);
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void retrieveReferenceNoSuchItemPersist() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		EntityManagerFactory emf = client.getEntityManagerFactory();
		UserTransaction tx = client.getUserTransaction();
		Long itemId = 999L;

		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemReference = em.getReference(Item.class, itemId);
			// No SQL is executed

			// Causes SELECT because initialized entity is required for lifecycle transitions
			itemReference.setName("Igor");

			Assertions.fail("Should throw exception");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(EntityNotFoundException.class);
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void retrieveReferenceClosedContext() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setName("Some Item");

		client.persist(item);

		EntityManagerFactory emf = client.getEntityManagerFactory();
		UserTransaction tx = client.getUserTransaction();

		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemReference = em.getReference(Item.class, item.getId());
			// No SQL is executed

			tx.commit();
			em.close();

			/*
			 * After the persistence context is closed, item is in detached
			 * state. If you do not initialize the proxy while the persistence
			 * context is still open, you get a LazyInitializationException if
			 * you access the proxy. You can't load data on-demand once the
			 * persistence context is closed. The solution is simple: Load the
			 * data before you close the persistence context.
			 */
			itemReference.getId();
			// SELECT is executed even on getId()

			Assertions.fail("Should throw exception");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(LazyInitializationException.class);
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void removeItem() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setName("Some Item");

		client.persist(item);

		EntityManagerFactory emf = client.getEntityManagerFactory();
		UserTransaction tx = client.getUserTransaction();

		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			/*
			 * If you call find(), Hibernate will execute a SELECT to load the
			 * Item. If you call getReference(), Hibernate will attempt to avoid
			 * the SELECT and return a proxy
			 */
			Item itemDb = em.find(Item.class, item.getId());
			// SELECT is executed, but not for reference-proxy

			/*
			 * Calling remove() will queue the entity instance for deletion when
			 * the unit of work completes, it is now in removed state. If
			 * remove() is called on a proxy, Hibernate will execute a SELECT to
			 * load the data. An entity instance has to be fully initialized
			 * during life cycle transitions. You may have life cycle callback
			 * methods or an entity listener enabled (see <a
			 * href="#EventListenersInterceptors"/>), and the instance must pass
			 * through these interceptors to complete its full life cycle
			 */
			// itemDb is detached
			em.remove(itemDb);
			// SELECT is executed for reference-proxy

			/*
			 * An entity in removed state is no longer in persistent state, this
			 * can be checked with the contains() operation
			 */
			Assertions.assertThat(em.contains(itemDb)).isFalse();

			/*
			 * You can make the removed instance persistent again, canceling the
			 * deletion. Then no DELETE will be executed
			 */
			// em.persist(itemDB);

			/*
			 * When remove(entity) is called, reset the identifier value of
			 * entity. so it's considered transient after removal <property
			 * name="hibernate.use_identifier_rollback" value="true"/>
			 */
			// assertNull(itemDb.getId());

			/*
			 * When the transaction commits, Hibernate synchronizes the state
			 * transitions with the database and executes the SQL DELETE. The
			 * JVM garbage collector detects that the item is no longer
			 * referenced by anyone and finally deletes the last trace of the
			 * data
			 */
			tx.commit();
			// DELETE is executed

			em.close();

			// Check that item was deleted
			tx.begin();
			em = emf.createEntityManager();
			itemDb = em.find(Item.class, item.getId());
			tx.commit();
			em.close();

			Assertions.assertThat(itemDb).isNull();
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void persistItemOverwriteDbChange() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Some Item");

		client.persist(item);

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemDb = em.find(Item.class, item.getId());
			itemDb.setName("Some Name");

			// Someone updates this row in the database!
			updateItemInDb(client, item.getId());

			// Can refresh to the latest persisted state
			// em.refresh(itemDb);

			em.persist(itemDb);

			// Will overwrite new state in database
			tx.commit();
			em.close();

			Item itemDb2 = client.select(Item.class, item.getId());
			Assertions.assertThat(itemDb2.getName()).isEqualTo(itemDb.getName());

		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void persistItemFailOnConcurrentDelete() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Some Item");

		client.persist(item);

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemDb = em.find(Item.class, item.getId());
			itemDb.setName("Some Name");

			// Someone deletes this row in the database!
			deleteItemInDb(client, item.getId());

			em.persist(itemDb);

			// Will fail on update
			tx.commit();

			Assertions.fail("Should throw exception");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(BitronixRollbackException.class);
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	private void updateItemInDb(final DbTestClient client, Long itemId) throws Exception {
		Object res = Executors.newSingleThreadExecutor().submit(() -> {

			UserTransaction tx2 = client.getUserTransaction();
			try {
				tx2.begin();
				Session session = client.unwrapSession();
				session.doWork((Connection connection) -> {
					PreparedStatement ps = connection.prepareStatement("update ITEM set name = ? where ID IN (?)");
					ps.setString(1, "Concurrent Update Name");
					ps.setLong(2, itemId);
					if (ps.executeUpdate() != 1) {
						throw new SQLException("ITEM row was not updated");
					}
				});

				tx2.commit();
				session.close();
			} catch (Exception e) {
				throw new RuntimeException("Concurrent operation failure", e);
			} finally {
				TransactionManager.rollback(tx2);
			}
		}).get();

		Assertions.assertThat(res).isNull();
	}

	private void deleteItemInDb(final DbTestClient client, Long itemId) throws Exception {
		Object res = Executors.newSingleThreadExecutor().submit(() -> {

			UserTransaction tx2 = client.getUserTransaction();
			try {
				tx2.begin();
				Session session = client.unwrapSession();
				session.doWork((Connection connection) -> {
					PreparedStatement ps = connection.prepareStatement("delete from ITEM where ID = ?");
					ps.setLong(1, itemId);
					if (ps.executeUpdate() != 1) {
						throw new SQLException("ITEM row was not updated");
					}
				});

				tx2.commit();
				session.close();
			} catch (Exception e) {
				throw new RuntimeException("Concurrent operation failure", e);
			} finally {
				TransactionManager.rollback(tx2);
			}
		}).get();

		Assertions.assertThat(res).isNull();
	}

	@Test
	public void replicateAndVersioning() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Some Item");

		client.persist(item);

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager emA = emf.createEntityManager();
			Item itemDb = emA.find(Item.class, item.getId());

			// Has to do with versioning
			EntityManager emB = emf.createEntityManager();
			Session sessionB = emB.unwrap(Session.class);
			sessionB.replicate(itemDb, ReplicationMode.LATEST_VERSION);
			// UPDATE with WHERE id=? and version=?

			tx.commit();
			emA.close();
			emB.close();
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void flushModeTypeAuto() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		client.persist(item);

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemDb1 = em.find(Item.class, item.getId());
			itemDb1.setName("New Name");

			// Set COMMIT flush mode
			em.setFlushMode(FlushModeType.AUTO);

			// With default flush mode, dirty changes are flushed
			String qlString = "SELECT t FROM Item t WHERE t.id = :id";
			TypedQuery<Item> query = em.createQuery(qlString, Item.class).setParameter("id", item.getId());
			Item itemDb2 = query.getSingleResult();

			// DB is not changed, but item is up-to-date
			Assertions.assertThat(itemDb2.getName()).isEqualTo(itemDb1.getName());

			tx.commit();
			// Silent flush, not visible in the log

			em.close();
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void flushModeTypeCommit() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		client.persist(item);

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			Item itemDb1 = em.find(Item.class, item.getId());
			itemDb1.setName("New Name");

			// Set COMMIT flush mode
			em.setFlushMode(FlushModeType.COMMIT);

			String qlString = "SELECT t FROM Item t WHERE t.id = :id";
			TypedQuery<Item> query = em.createQuery(qlString, Item.class).setParameter("id", item.getId());
			Item itemDb2 = query.getSingleResult();

			// DB is not changed, but item is up-to-date
			Assertions.assertThat(itemDb2.getName()).isEqualTo(itemDb1.getName());

			tx.commit();
			// Flush, update name

			em.close();
		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void mergeDetached() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		client.persist(item);

		// Update name in detached state
		item.setName("Detached name");

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			// Merge
			Item item2 = em.merge(item);
			// SELECT is executed. Name is "Detached name"

			Assertions.assertThat(item2.getName()).isEqualTo("Detached name");

			tx.commit();
			// UPDATE is executed

			em.close();
		} finally {
			TransactionManager.rollback(tx);
		}

		Item item3 = client.select(Item.class, item.getId());
		Assertions.assertThat(item3.getName()).isEqualTo("Detached name");

		client.close();
	}

	@Test
	public void mergeDetachedToExistingContext() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		client.persist(item);

		// Update name in detached state
		item.setName("Detached name");

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			// Find
			Item item2 = em.find(Item.class, item.getId());
			// SELECT is executed. Name is "Original Name"

			// Merge
			Item item3 = em.merge(item);
			// SELECT is not executed. Name is "Detached name"

			Assertions.assertThat(item2).isSameAs(item3);
			Assertions.assertThat(item3.getName()).isEqualTo("Detached name");

			tx.commit();
			// UPDATE is executed

			em.close();
		} finally {
			TransactionManager.rollback(tx);
		}

		Item item4 = client.select(Item.class, item.getId());
		Assertions.assertThat(item4.getName()).isEqualTo("Detached name");

		client.close();
	}

	@Test
	public void mergeDetachedDeleted() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		client.persist(item);

		// Update name in detached state
		item.setName("Detached name");

		// Somebody deleted the item in database!
		deleteItemInDb(client, item.getId());

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			// Merge
			Item item2 = em.merge(item);
			// SELECT is executed, nothing found. New item is created

			Assertions.assertThat(item2.getName()).isEqualTo("Detached name");

			tx.commit();
			// INSERT is executed

			em.close();

			Item item3 = client.select(Item.class, item2.getId());
			Assertions.assertThat(item3.getName()).isEqualTo("Detached name");

		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}

	@Test
	public void mergeNotYetPersisted() throws Exception {

		final DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);
		final EntityManagerFactory emf = client.getEntityManagerFactory();

		Item item = new Item();
		item.setName("Original Name");

		UserTransaction tx = client.getUserTransaction();
		try {
			tx.begin();
			EntityManager em = emf.createEntityManager();

			// Merge
			Item item2 = em.merge(item);
			// New sequence value is selected. New item is created

			Assertions.assertThat(item2.getName()).isEqualTo("Original Name");

			tx.commit();
			// INSERT is executed

			em.close();

			Item item3 = client.select(Item.class, item2.getId());
			Assertions.assertThat(item3.getName()).isEqualTo("Original Name");

		} finally {
			TransactionManager.rollback(tx);
			client.close();
		}
	}
}
