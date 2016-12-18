package com.igor.setup;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUnit {

	private static final String PERSISTENCE_UNIT_NAME = "HelloWorldPU";
	private static final EntityManagerFactory FACTORY_INSTANCE = createEntityManagerFactory();

	private static EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	public static EntityManagerFactory getInstance() {
		return FACTORY_INSTANCE;
	}

	public static void close() {
		FACTORY_INSTANCE.close();
	}
}