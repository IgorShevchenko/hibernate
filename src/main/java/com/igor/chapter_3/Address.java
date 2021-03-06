package com.igor.chapter_3;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * Instead of <code>@Entity</code>, this component POJO is marked with
 * <code>@Embeddable</code>. It has no identifier property. Inherits the default
 * or explicitly declared access strategy of its owning root entity class.
 */
@Embeddable
// Can specify access strategy.
// Hibernate will use the specified strategy for reading mapping 
// annotations on the embeddable class and runtime access.
@Access(AccessType.FIELD)
public class Address implements Serializable {

	private static final long serialVersionUID = 1L;

	// @NotNull is ignored for DDL generation here (old bug)!
	// Will be used at runtime, for Bean Validation 
	@NotNull
	// Used for DDL generation
	@Column(nullable = false, name = "street_2")
	protected String street;

	// Must have both annotations
	// Not null, varchar(5), defines fixed column name
	@NotNull
	@Column(nullable = false, length = 5, name = "zipcode_2")
	protected String zipcode;

	@NotNull
	// Override column "name" to be "city", length is defaulted here!
	// Could move "column name" to nested column definition 
	@AttributeOverrides({ @AttributeOverride(name = "name", column = @Column(name = "city", nullable = false)) })
	private City city;

	/**
	 * Hibernate will call this no-argument constructor to create an instance,
	 * and then populate the fields directly.
	 */
	protected Address() {
	}

	/**
	 * You can have additional (public) constructors for convenience.
	 */
	public Address(String street, String zipcode, City city) {
		this.street = street;
		this.zipcode = zipcode;
		this.city = city;
	}

	// Ignored, as will always use @Access(AccessType.FIELD)
	@Column(nullable = false, name = "street_3")
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

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

}
