package com.igor.chapter_3;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Bid {

	// Can define own generator, see model/package-info.java
	// @GeneratedValue(generator = "ID_GENERATOR")

	// Primary key, bigint(20)
	@Id
	@GeneratedValue
	protected Long id;

	// Not null, decimal(19,2)
	@NotNull
	protected BigDecimal amount;

	// Not null, with index, FK on item(item_id -> id), column in the Bid table, bigint(20)
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	// @JoinColumn(name = "ITEM_ID") // Default name, name of column in Bid table 
	protected Item item;

	protected Bid() {
		// For Hibernate, protected is better than package-visible
	}

	public Bid(Item item) {
		// Bidirectional link
		this.item = item;
		item.getBids().add(this);
	}

	public Bid(BigDecimal amount, Item item) {
		this.amount = amount;
		this.item = item;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
}
