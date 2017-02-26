package com.igor.chapter_3;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USERS")
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
	@Embedded
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

	public Address getBillingAddress() {
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
