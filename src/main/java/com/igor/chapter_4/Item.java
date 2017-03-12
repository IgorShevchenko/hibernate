package com.igor.chapter_4;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

@Entity
// Quoting SQL identifiers: `ITEM`
// Generates SQL accordingly: [USER] for MS SQL Server, 'USER' for MySQL, "USER" for H2, and so on.
// Also has catalog and schema options, if your database layout requires these as naming prefixes
@Table(name = "\"ITEM\"", schema = "hibernate_db")
// By enabling dynamic insertion and updates, you tell Hibernate to produce the SQL
// strings when needed, not up front. The UPDATE will only contain columns with
// updated values, and the INSERT will only contain non-nullable columns.
@DynamicInsert
@DynamicUpdate
@SelectBeforeUpdate
public class Item {

	// Primary key, int(11), for long: bigint(20)
	// Identifier is required: no identifier specified for entity. Must be immutable
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "IGOR_GENERATOR")
	private Integer id;

	@Basic
	private String name;

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
