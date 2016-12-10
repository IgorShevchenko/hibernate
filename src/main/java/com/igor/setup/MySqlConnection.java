package com.igor.setup;

import bitronix.tm.resource.jdbc.PoolingDataSource;

class MySqlConnection {

	private final static String USER = "root";
	private final static String PASSWORD = "root";

	public static String getConnectionUrl() {
		return String.format("jdbc:mysql://localhost/hibernate_db?user=%s&password=%s", USER, PASSWORD);
	}

	public static String getHibernateDialect() {
		return org.hibernate.dialect.MySQL57InnoDBDialect.class.getName();
	}

	public static void configure(PoolingDataSource ds) {
		// ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		ds.getDriverProperties().put("url", getConnectionUrl());
		ds.getDriverProperties().put("driverClassName", "com.mysql.jdbc.Driver");
	}
}