package com.igor.chapter_4;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

@Entity(name = "BidEntity")
@Table(name = "bid")
// Instances of a particular class may be immutable. For example, a Bid made for an item 
// is immutable. Hence, Hibernate never needs to execute UPDATE statements on the BID table. 
// Hibernate can also make a few other optimizations, such as avoiding dirty checking.
@Immutable
public class Bid {

	// Primary key, int(11)
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "IGOR_GENERATOR")
	protected Integer id;

	@NotNull
	@Column(updatable = false, nullable = false)
	protected Integer amount;

	// Index + FK
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	// If the join is for a OneToOne or ManyToOne mapping using a foreign key mapping strategy,
	// the foreign key column is in the table of the source entity or embeddable. 
	@JoinColumn(name = "ITEM_ID") // Actually the default name
	private Item item;

	protected Bid() {
		// For Hibernate
	}

	public Bid(Item item, int amount) {
		this.item = item;
		this.amount = amount;
	}

	public Integer getId() {
		return id;
	}

	public Integer getAmount() {
		return amount;
	}

	public Item getItem() {
		return item;
	}

}