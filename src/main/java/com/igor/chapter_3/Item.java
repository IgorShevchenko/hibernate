package com.igor.chapter_3;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// Has circular dependency with Bid.
// @Entity makes all fields/properties persistent.
@Entity
public class Item {

	// Can define own generator, see model/package-info.java
	// @GeneratedValue(generator = "ID_GENERATOR")

	// Primary key, bigint(20)
	@Id
	@GeneratedValue
	protected Long id;

	// No getter/setter, still persisted, bigint(20)
	@Version
	// Optional
	@Basic
	protected long version = 1;

	// varchar(255)
	// Validations can also be on getter
	@NotNull
	@Size(min = 2, max = 255, message = "Name is required, maximum 255 characters.")
	protected String name;

	// java.util.Date does not store milliseconds and by default maps to DATETIME
	// LocalDateTime is also by default mapped to Datetime, no milliseconds
	// @Future works with java.util.Date and subtypes, and Calendar only
	// Apply column definition to enforce milliseconds
	@Future
	@Column(columnDefinition = "DATETIME(3)")
	// protected Timestamp auctionEnd;
	protected Date auctionEnd;

	// Persistent with auto settings, decimal(19,2)
	protected BigDecimal buyNowPrice;

	// Column in the Item table, FK + index, bigint(20)
	// Lay loaded fields are not displayed in Eclipse
	@ManyToOne(fetch = FetchType.LAZY)
	protected Category category;

	// Not persisted. How do we get bids then?
	@Transient
	protected Set<Bid> bids = new HashSet<Bid>();

	// Not persisted
	public final boolean isData1() {
		return true;
	}

	// Not persisted
	public final int getData2() {
		return -100;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getAuctionEnd() {
		// MUST do this for 2 reasons:
		// 1) Return immutable instance
		// 2) Loaded this.auctionEnd is java.sql.Timestamp
		// Can also define a custom converter
		if (this.auctionEnd == null) {
			return null;
		} else {
			System.out.println(this.auctionEnd.getClass());
			return new Date(this.auctionEnd.getTime());
		}
	}

	public void setAuctionEnd(Date auctionEnd) {
		this.auctionEnd = new Date(auctionEnd.getTime());
	}

	public BigDecimal getBuyNowPrice() {
		return buyNowPrice;
	}

	public void setBuyNowPrice(BigDecimal buyNowPrice) {
		this.buyNowPrice = buyNowPrice;
	}

	public Set<Bid> getBids() {
		// In case JPA uses field access, can return unmodifiableSet
		// Access type is defined by the position of the @Id or @EmbeddedId annotations
		// Need to add logic => property access
		// Need to maintain encapsulation => field access
		return bids;
	}

	public void setBids(Set<Bid> bids) {
		this.bids = bids;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void addBid(Bid bid) {
		// Be defensive
		if (bid == null) {
			throw new NullPointerException("Can't add null Bid");
		}
		if (bid.getItem() != null) {
			throw new IllegalStateException("Bid is already assigned to an Item");
		}

		getBids().add(bid);
		bid.setItem(this);
	}

	//	public Bid placeBid(Bid currentHighestBid, BigDecimal bidAmount) {
	//		if (currentHighestBid == null ||
	//				bidAmount.compareTo(currentHighestBid.getAmount()) > 0) {
	//			return new Bid(bidAmount, this);
	//		}
	//		return null;
	//	}
}
