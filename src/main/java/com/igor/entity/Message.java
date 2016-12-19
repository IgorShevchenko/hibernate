package com.igor.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * Every persistent entity class must have at least the @Entity annotation.
 * Hibernate maps this class to a table called MESSAGE.
 */
@Entity
public class Message {

	/*
	 * Every persistent entity class must have an identifier attribute annotated
	 * with @Id. Hibernate maps this attribute to a column named ID. Must
	 * generate identifiers; annotation enables automatic generation of IDs.
	 */
	@Id
	@GeneratedValue
	private int id;

	/*
	 * You usually implement regular attributes of a persistent class with
	 * private or protected fields, and public getter/setter method pairs.
	 * Hibernate maps this attribute to a column called TEXT.
	 */
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
