package com.igor.chapter_3;

import java.util.StringTokenizer;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Database stores the name of a user as a single NAME column, but UserName
 * class has separate firstName and lastName fields.
 */
@Entity
public class UserName {

	@Id
	@GeneratedValue
	protected Long id;

	@Transient
	private String firstName;

	@Transient
	private String lastName;

	public Long getId() {
		return id;
	}

	@Access(value = AccessType.PROPERTY)
	@Column(name = "name")
	protected String getName() {
		// Access type is defined by the position of the @Id or @EmbeddedId annotations
		// Need to add logic => property access
		// Need to maintain encapsulation => field access
		return this.firstName + ' ' + this.lastName;
	}

	public void setName(String name) {
		StringTokenizer t = new StringTokenizer(name);
		this.firstName = t.nextToken();
		this.lastName = t.nextToken();
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

}