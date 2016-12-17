package com.igor.setup;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceUnit {

	private static final String PERSISTENCE_UNIT_NAME = "HelloWorldPU";
	public static final String JTA_DATA_SOURCE = "myDS2";

	public static EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}
}