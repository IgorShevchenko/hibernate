package com.igor.chapter_4.generated_value;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Without @GeneratedValue, the JPA provider assumes you’ll take care of
 * creating and assigning an identifier before you save an instance. Necessary
 * when you’re dealing with a legacy database and/or natural primary keys.
 */
@Entity
public class ItemNaturalId {

	@Id
	private Integer id;

	@Basic
	private String name;

	public Integer getId() {
		return id;
	}

	// We need to have setter
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
