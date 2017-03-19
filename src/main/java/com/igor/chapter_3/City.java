package com.igor.chapter_3;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
public class City {

	// @NotNull is ignored for DDL generation here (old bug)!
	// Will be used at runtime, for Bean Validation
	@NotNull
	// Used for DDL generation
	// Not null, varchar(10)
	@Column(nullable = false, length = 10)
	private String name;

	@NotNull
	// Not null, varchar(20)
	@Column(nullable = false, length = 20)
	private String country;

	/**
	 * Hibernate will call this no-argument constructor to create an instance,
	 * and then populate the fields directly.
	 */
	protected City() {
	}

	/**
	 * You can have additional (public) constructors for convenience.
	 */
	public City(String name, String country) {
		this.name = name;
		this.country = country;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
