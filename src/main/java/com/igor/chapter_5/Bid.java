package com.igor.chapter_5;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converts;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

@Entity
@Immutable
// Apply converter on attribute, or embedded class, or nested embedded class
@Convert(converter = Zipcode.Converter.class, attributeName = "deliveryZipCode")
public class Bid {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "IGOR_GENERATOR")
	private Integer id;

	@Column(nullable = false)
	private Double amount;

	@ManyToOne(optional = false)
	private Item item;

	// Instead of BIT, this boolean now maps to a CHAR column with values Y/N
	// Column type: char(1)
	@Type(type = "yes_no")
	private boolean verified;

	// The annotation is optional: apply it to override or disable a converter
	// for a particular property
	@Convert(converter = MonetaryAmount.Converter.class)
	// Override default length from the converter
	@Column(length = 10)
	private MonetaryAmount monetaryAmount;

	// In case several converters are required on embedded property
	@Converts({ @Convert(converter = DateConverter.class) })
	private Date createdOn;

	// Converter is applied through entity annotation
	private Zipcode deliveryZipCode;

	@Convert(converter = Zipcode.ConverterSet.class)
	@ElementCollection
	private Set<Zipcode> alternativeZipCodes;

	public Integer getId() {
		return id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public MonetaryAmount getMonetaryAmount() {
		return monetaryAmount;
	}

	public void setMonetaryAmount(MonetaryAmount monetaryAmount) {
		this.monetaryAmount = monetaryAmount;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Zipcode getDeliveryZipCode() {
		return deliveryZipCode;
	}

	public void setDeliveryZipCode(Zipcode deliveryZipCode) {
		this.deliveryZipCode = deliveryZipCode;
	}

	public Set<Zipcode> getAlternativeZipCodes() {
		return alternativeZipCodes;
	}

	public void setAlternativeZipCodes(Set<Zipcode> alternativeZipCodes) {
		this.alternativeZipCodes = alternativeZipCodes;
	}

}
