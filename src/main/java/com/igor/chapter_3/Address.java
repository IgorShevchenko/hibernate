package com.igor.chapter_3;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * 
 * Instead of <code>@Entity</code>, this component POJO is marked with
 * <code>@Embeddable</code>. It has no identifier property.
 */
@Embeddable
public class Address {

	// @NotNull is ignored for DDL generation here!
	// @Column(nullable = false) used for DDL generation!
	@NotNull
	@Column(nullable = false, name = "street_2")
	protected String street;

	// Not null, varchar(5)
	// Defines fixed column name
	@NotNull
	@Column(nullable = false, length = 5, name = "zipcode_2")
	protected String zipcode;

	// Must have both annotations
	@NotNull
	@Column(nullable = false, name = "city_2")
	protected String city;

	/**
	 * Hibernate will call this no-argument constructor to create an instance,
	 * and then populate the fields directly.
	 */
	protected Address() {
	}

	/**
	 * You can have additional (public) constructors for convenience.
	 */
	public Address(String street, String zipcode, String city) {
		this.street = street;
		this.zipcode = zipcode;
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
}
