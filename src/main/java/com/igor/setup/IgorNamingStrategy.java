package com.igor.setup;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Prefixes all SQL table names and columns with "igor_". Custom naming of
 * columns, sequences, and other artifacts. You have to enable the
 * naming-strategy implementation in persistence.xml with property
 * name="hibernate.physical_naming_strategy"
 */
public class IgorNamingStrategy extends PhysicalNamingStrategyStandardImpl {

	private static final long serialVersionUID = 1L;

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return new Identifier("igor_" + name.getText(), name.isQuoted());
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		return new Identifier("igor_" + name.getText(), name.isQuoted());
	}

}