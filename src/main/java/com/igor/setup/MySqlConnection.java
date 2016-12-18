package com.igor.setup;

import java.util.Properties;

import bitronix.tm.resource.jdbc.PoolingDataSource;

class MySqlConnection {

	private final static String USER = "root";
	private final static String PASSWORD = "root";

	private static String getConnectionUrl() {
		return String.format("jdbc:mysql://localhost/hibernate_db?user=%s&password=%s", USER, PASSWORD);
	}

	public static String getHibernateDialect() {
		return org.hibernate.dialect.MySQL57InnoDBDialect.class.getName();
	}

	public static void configure(PoolingDataSource ds) {
		// ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		Properties driverProperties = ds.getDriverProperties();
		driverProperties.put("url", getConnectionUrl());
		driverProperties.put("driverClassName", "com.mysql.jdbc.Driver");
	}
}