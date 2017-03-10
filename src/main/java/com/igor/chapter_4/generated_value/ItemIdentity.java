package com.igor.chapter_4.generated_value;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Hibernate expects a special auto-incremented primary key column that
 * automatically generates a numeric value on INSERT, in the database. Produces
 * a value on INSERT that has to be returned to the application afterward.
 */
@Entity
public class ItemIdentity {

	// bigint(20) NOT NULL AUTO_INCREMENT
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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