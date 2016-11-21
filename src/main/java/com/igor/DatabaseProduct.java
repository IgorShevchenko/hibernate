package com.igor;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public enum DatabaseProduct {

	MYSQL(
			new DataSourceConfiguration() {

				@Override
				public void configure(PoolingDataSource ds, String connectionURL) {

					// TODO: MySQL XA support is completely broken, we use the BTM XA wrapper
					//ds.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");

					if (connectionURL == null) {
						// connectionURL = "jdbc:mysql://localhost/test?sessionVariables=sql_mode='PIPES_AS_CONCAT'";
						connectionURL = "jdbc:mysql://localhost/hibernate_db?user=root&password=root";
					}

					ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
					ds.getDriverProperties().put("url", connectionURL);
					ds.getDriverProperties().put("driverClassName", "com.mysql.jdbc.Driver");
				}
			},

			org.hibernate.dialect.MySQL57InnoDBDialect.class.getName());

	public DataSourceConfiguration configuration;
	public String hibernateDialect;

	private DatabaseProduct(DataSourceConfiguration configuration, String hibernateDialect) {
		this.configuration = configuration;
		this.hibernateDialect = hibernateDialect;
	}

	public interface DataSourceConfiguration {

		void configure(PoolingDataSource ds, String connectionURL);
	}

}
