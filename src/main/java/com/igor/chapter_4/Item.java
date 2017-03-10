package com.igor.chapter_4;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Item {

	// Primary key, bigint(20)
	// Identifier is required: no identifier specified for entity. Must be immutable
	@Id
	@GeneratedValue(generator = "ID_GENERATOR")
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
