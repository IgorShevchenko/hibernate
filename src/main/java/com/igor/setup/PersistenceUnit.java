package com.igor.setup;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUnit {

	private static final String PERSISTENCE_UNIT_NAME = "HelloWorldPU";
	private static final EntityManagerFactory FACTORY_INSTANCE = createEntityManagerFactory();

	private static EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	/**
	 * Returns EntityManagerFactory instance constructed during application
	 * startup. Whole application is supposed to use this and one this instance.
	 * 
	 * @return Instance constructed during application startup.
	 */
	public static EntityManagerFactory getInstance() {
		return FACTORY_INSTANCE;
	}

	/**
	 * Returns an <b>open</b> EntityManagerFactory instance. This will be either
	 * the instance constructed during application startup, if it is still open,
	 * or newly created instance. In case a new instance is created, the
	 * database schema may be dropped/recreated according to persistence.xml
	 * schema generation action. This method is useful for testing when a
	 * separate open instance is created for each test.
	 * 
	 * @return An open instance.
	 */
	static EntityManagerFactory getOpenInstance() {
		if (FACTORY_INSTANCE.isOpen()) {
			return FACTORY_INSTANCE;
		} else {
			return createEntityManagerFactory();
		}
	}
}