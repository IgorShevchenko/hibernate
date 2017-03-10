package com.igor.chapter_4.generated_value;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * GenerationType.TABLE — Hibernate will use an extra table in your database
 * schema that holds the next numeric primary key value, one row for each entity
 * class. This table will be read and updated accordingly, before INSERTs. The
 * default table name is HIBERNATE_SEQUENCES with columns SEQUENCE_NAME and
 * SEQUENCE_NEXT_HI_VALUE.
 */
@Entity
public class ItemTable {

	// With just this configuration will create table "hibernate_sequences":
	// sequence_name will be "default", varchar(255)
	// Can configure "initialValue" and "allocationSize", and "pkColumnValue"
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	// MUST be long for JPA Table strategy
	private Long id;

	@Basic
	private String name;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
