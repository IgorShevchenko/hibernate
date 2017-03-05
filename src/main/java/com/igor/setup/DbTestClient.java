package com.igor.setup;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.UserTransaction;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Every interaction with your database should occur within explicit transaction
 * boundaries, even if you are only reading data. Creates an
 * EntityManagerFactory. Configuration of the persistence units is taken from
 * <code>META-INF/persistence.xml</code> and other sources. What to know for:
 * <p>
 * EntityManager: persist, flush, getTransaction, getReference, close, find,
 * remove, contains, setFlushMode, createQuery, merge, unwrap.
 * <p>
 * Extra: createEntityManager, getPersistenceUnitUtil, isLoaded, initialize,
 * doWork.
 * <p>
 * Exceptions: EntityNotFoundException, LazyInitializationException,
 * BitronixRollbackException.
 */
public class DbTestClient implements Closeable {

	private final TransactionManager tm;

	/**
	 * One instance in an application per data source.
	 */
	private final EntityManagerFactory emf;

	public DbTestClient(String persistenceUnit) {
		this.tm = TransactionManager.getInstance();
		this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return this.emf;
	}

	public UserTransaction getUserTransaction() {
		return this.tm.getUserTransaction();
	}

	public Metamodel getMetamodel() {
		return this.emf.getMetamodel();
	}

	public PersistenceUnitUtil getPersistenceUnitUtil() {
		return this.emf.getPersistenceUnitUtil();
	}

	public SessionImplementor getDelegateEmf() {
		return this.emf.unwrap(SessionImplementor.class);
	}

	public SessionImplementor getDelegateEm() {
		return this.emf.createEntityManager().unwrap(SessionImplementor.class);
	}

	public Session unwrapSession() {
		return this.emf.createEntityManager().unwrap(Session.class);
	}

	public boolean isInitialized(Object proxy) {
		return Hibernate.isInitialized(proxy);
	}

	/**
	 * Execute required code given by the consumer, inside a transaction.
	 * <b>Persistence context is closed</b> when the method finishes, so further
	 * persistence actions are not tracked.
	 * 
	 * @param consumer
	 *            Represents code to execute.
	 * @throws Exception
	 */
	public void executeTransaction(Consumer<EntityManager> consumer) throws Exception {
		UserTransaction tx = this.tm.getUserTransaction();
		try {
			tx.begin();

			EntityManager em = this.emf.createEntityManager();

			consumer.accept(em);

			tx.commit();
			em.close();
		} finally {
			TransactionManager.rollback(tx);
		}
	}

	/**
	 * Persist entities in order, inside a single transaction. <b>Persistence
	 * context is closed</b>, so further changes to persisted entities are not
	 * tracked.
	 * 
	 * @param entities
	 * @throws Exception
	 */
	public void persist(Object... entities) throws Exception {
		executeTransaction((EntityManager em) -> {
			for (Object entity : entities) {
				em.persist(entity);
			}
		});
	}

	/**
	 * Get persisted entity with the specified ID, inside a single transaction.
	 * <b>Persistence context is closed</b>, so changes to returned entity are
	 * not tracked. Lazy loading is not supported.
	 * 
	 * @param entityClass
	 * @param id
	 * @return Persisted entity of specified type with the specified ID.
	 * @throws Exception
	 */
	public <T> T select(Class<T> entityClass, long id) throws Exception {
		try {
			UserTransaction tx = this.tm.getUserTransaction();
			tx.begin();

			EntityManager em = this.emf.createEntityManager();

			// Better approach
			// em.find(entityClass, id);

			String qlString = "SELECT t FROM " + entityClass.getSimpleName() + " t WHERE t.id = :id";
			TypedQuery<T> query = em.createQuery(qlString, entityClass).setParameter("id", id);
			T entity = query.getSingleResult();

			tx.commit();
			em.close();

			return entity;
		} finally {
			TransactionManager.rollback();
		}
	}

	/**
	 * Get all persisted entities, inside a single transaction. <b>Persistence
	 * context is closed</b>, so changes to returned entities are not tracked.
	 * Lazy loading is not supported.
	 * 
	 * @param entityClass
	 * @return All persisted entities of specified type.
	 * @throws Exception
	 */
	public <T> List<T> selectAll(Class<T> entityClass) throws Exception {
		try {
			UserTransaction tx = this.tm.getUserTransaction();
			tx.begin();

			EntityManager em = this.emf.createEntityManager();

			String qlString = "SELECT t FROM " + entityClass.getSimpleName() + " t";
			List<T> entities = em.createQuery(qlString, entityClass).getResultList();

			tx.commit();
			em.close();

			return entities;
		} finally {
			TransactionManager.rollback();
		}
	}

	/**
	 * Get persisted entities, inside a single transaction, by executing a named
	 * query. <b>Persistence context is closed</b>, so changes to returned
	 * entities are not tracked. Lazy loading is not supported.
	 * 
	 * @param namedQuey
	 * @param entityClass
	 * @return Persisted entities of specified type.
	 * @throws Exception
	 */
	public <T> List<T> executeNamedQuery(String namedQuey, Class<T> entityClass) throws Exception {
		try {
			UserTransaction tx = this.tm.getUserTransaction();
			tx.begin();

			EntityManager em = this.emf.createEntityManager();

			List<T> entities = em.createNamedQuery(namedQuey, entityClass).getResultList();

			tx.commit();
			em.close();

			return entities;
		} finally {
			TransactionManager.rollback();
		}
	}

	public <T> List<T> selectAll(Class<T> entityClass, long... ids) throws Exception {
		try {
			UserTransaction tx = this.tm.getUserTransaction();
			tx.begin();

			EntityManager em = this.emf.createEntityManager();

			String qlString = "SELECT t FROM " + entityClass.getSimpleName() + " t WHERE t.id IN :ids";
			TypedQuery<T> query = em.createQuery(qlString, entityClass).setParameter("ids", Arrays.asList(ids));
			List<T> entities = query.getResultList();

			tx.commit();
			em.close();

			return entities;
		} finally {
			TransactionManager.rollback();
		}
	}

	/**
	 * Close client when all actions are done (in the end of the test).
	 */
	@Override
	public void close() {
		this.emf.close();
	}
}
