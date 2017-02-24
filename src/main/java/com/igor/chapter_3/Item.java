package com.igor.chapter_3;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
	protected long version = 1;

	// varchar(255)
	@NotNull
	@Size(min = 2, max = 255, message = "Name is required, maximum 255 characters.")
	protected String name;

	// Datetime
	@Future
	protected Date auctionEnd;

	// Persistent with auto settings, decimal(19,2)
	protected BigDecimal buyNowPrice;

	// Column in the Item table, FK + index, bigint(20) 
	@ManyToOne(fetch = FetchType.LAZY)
	protected Category category;

	// Not persisted. How do we get bids then?
	@Transient
	protected Set<Bid> bids = new HashSet<Bid>();

	// Not persisted
	public boolean isData1() {
		return true;
	}

	// Not persisted
	public int getData2() {
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
		return auctionEnd;
	}

	public void setAuctionEnd(Date auctionEnd) {
		this.auctionEnd = auctionEnd;
	}

	public BigDecimal getBuyNowPrice() {
		return buyNowPrice;
	}

	public void setBuyNowPrice(BigDecimal buyNowPrice) {
		this.buyNowPrice = buyNowPrice;
	}

	public Set<Bid> getBids() {
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
