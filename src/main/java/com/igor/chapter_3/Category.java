package com.igor.chapter_3;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Category {

	// Can define own generator, see model/package-info.java
	// @GeneratedValue(generator = "ID_GENERATOR")

	// Primary key, bigint(20)
	@Id
	@GeneratedValue
	protected Long id;

	// Persistent with auto settings, varchar(255) 
	protected String name;

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
