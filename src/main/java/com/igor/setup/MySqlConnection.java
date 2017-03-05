package com.igor.setup;

import java.util.Properties;

import bitronix.tm.resource.jdbc.PoolingDataSource;

class MySqlConnection {

	private final static String DB = "hibernate_db";
	private final static String USER = "root";
	private final static String PASSWORD = "root";

	private static String getConnectionUrl() {
		return String.format("jdbc:mysql://localhost/%s?user=%s&password=%s", DB, USER, PASSWORD);
	}

	public static String getHibernateDialect() {
		return org.hibernate.dialect.MySQL57InnoDBDialect.class.getName();
	}

	public static void configure(PoolingDataSource ds) {
		// MySQL XA support is completely broken, we use the BTM XA wrapper
		// We can't enlist two non-XA connections in the same transaction
		// on MySQL. XA is broken in MySQL, so we have to use the Bitronix XA wrapper, it can
		// only handle one non-XA resource per transaction
		// ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		Properties driverProperties = ds.getDriverProperties();
		driverProperties.put("url", getConnectionUrl());
		driverProperties.put("driverClassName", "com.mysql.jdbc.Driver");
	}
}