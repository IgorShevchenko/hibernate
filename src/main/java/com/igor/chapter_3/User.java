package com.igor.chapter_3;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
// Override the table name
// The User entity would map to the USER table; a reserved keyword in most SQL DBMSs
// Also has catalog and schema options, if your database layout requires these as naming prefixes
@Table(name = "USERS", schema = "hibernate_db")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	// Can define own generator, see model/package-info.java
	// @GeneratedValue(generator = "ID_GENERATOR")

	// ID is required for each entity
	@Id
	@GeneratedValue
	protected Long id;

	protected String username;

	// The Address is @Embeddable, no annotation needed here
	// Hibernate detects that the Address class is annotated with @Embeddable
	@Embedded
	// Can specify access strategy here, if embedded class does not specify it
	// Here is ignored, as embedded class specifies @Access(AccessType.FIELD)
	@Access(AccessType.PROPERTY)
	protected Address homeAddress;

	// There should be another way?
	// @Embedded is optional annotation here
	// NULL-able overrides!
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "street", column = @Column(name = "BILLING_STREET")),
			@AttributeOverride(name = "zipcode", column = @Column(name = "BILLING_ZIPCODE", length = 5)),
			@AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY"))
	})
	protected Address billingAddress;

	public User() {
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Address getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(Address homeAddress) {
		this.homeAddress = homeAddress;
	}

	/*
	 * Consider what would happen if all embedded fields are NULLs. Hibernate
	 * returns a null in this case. Hibernate also stores a null embedded
	 * property as NULL values in all mapped columns of the component.
	 * Consequently, if you store a User with an empty Address (you have an
	 * Address instance but all its properties are null), no Address instance
	 * will be returned when you load the User.
	 */
	public Address getBillingAddress() {
		// Document that will return null, not an instance with all null fields
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	public BigDecimal calcShippingCosts(Address fromLocation) {
		// Empty implementation of business method
		return null;
	}
}
