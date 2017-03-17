package com.igor.chapter_5;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
// Annotations will be considered only if they placed according to access
// strategy.
// When overriding access, annotations must be on the same element as @Access
// annotation.
@Access(AccessType.FIELD)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "IGOR_GENERATOR")
	private Integer id;

	// Three ways to declare whether a property value is required
	// Hibernate does a null check on-save, and generates a NOT NULL constraint
	// org.hibernate.PropertyValueException:
	// Insert is always executed for @Basic(optional = false) and
	// @Column(nullable = false),
	// if bean validation is enabled. Then it relies only on @NotNull
	@NotNull
	@Basic(optional = false)
	@Column(nullable = false, name = "price")
	private Double initialPrice;

	// Can have a setter, but value will not be saved to database
	// Syntax: "(SQL here)". Executed in the database
	// select a, b, (select ... where b.item_id = id) from ... where id = ?
	// Unqualified columns refer to entity table
	@Formula("(SELECT count(*) FROM bid b WHERE b.item_id = id)")
	private Integer bidCount;

	@Column(name = "weight_pounds")
	// Executed in the database on SELECT / INSERT / UPDATE
	@ColumnTransformer(read = "weight_pounds * 2", write = "? / 2")
	private double weight;

	// Maintained by database trigger
	// Select is executed on every INSERT/UPDATE to get generated value for every generated property
	// Generated to DATETIME(6)
	// Also means column is not insertable/updatable, e.g. @Column(insertable = false, updatable = false)
	@Generated(GenerationTime.ALWAYS)
	private Date lastModified;

	// Lets' take default value on insert
	@ColumnDefault("10")
	// Also means column is not insertable, e.g. @Column(insertable = false) 
	@Generated(GenerationTime.INSERT)
	private Integer amount;

	public Integer getId() {
		return id;
	}

	public Double getInitialPrice() {
		return initialPrice;
	}

	public void setInitialPrice(Double initialPrice) {
		this.initialPrice = initialPrice;
	}

	public Integer getBidCount() {
		return bidCount;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

}
