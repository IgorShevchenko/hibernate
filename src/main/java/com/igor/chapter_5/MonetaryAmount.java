package com.igor.chapter_5;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.AttributeConverter;

/**
 * This value-typed class should be <code>java.io.Serializable</code>: When
 * Hibernate stores entity instance data in the shared second-level cache (see
 * <a href="#Caching"></a>), it <em>disassembles</em> the entity's state. If an
 * entity has a <code>MonetaryAmount</code> property, the serialized
 * representation of the property value will be stored in the second-level cache
 * region. When entity data is retrieved from the cache region, the property
 * value will be deserialized and reassembled.
 */
public class MonetaryAmount implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * The class does not need a special constructor, you can make it immutable,
	 * even with <code>final</code> fields, as your code will be the only place
	 * an instance is created.
	 */

	protected final BigDecimal value;
	protected final Currency currency;

	// No special constructor needed
	public MonetaryAmount(BigDecimal value, Currency currency) {
		this.value = value;
		this.currency = currency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public Currency getCurrency() {
		return currency;
	}

	/*
	 * You will need a <code>String</code> representation of a monetary amount.
	 * Implement the <code>toString()</code> method and a static method to
	 * create an instance from a <code>String</code>.
	 */
	@Override
	public String toString() {
		return this.value + " " + this.currency;
	}

	public static MonetaryAmount fromString(String s) {
		String[] split = s.split(" ");
		return new MonetaryAmount(new BigDecimal(split[0]), Currency.getInstance(split[1]));
	}

	@Override
	public int hashCode() {
		int result;
		result = value.hashCode();
		result = 29 * result + currency.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MonetaryAmount))
			return false;

		final MonetaryAmount monetaryAmount = (MonetaryAmount) o;

		if (!value.equals(monetaryAmount.value))
			return false;
		if (!currency.equals(monetaryAmount.currency))
			return false;

		return true;
	}

	public static class Converter implements AttributeConverter<MonetaryAmount, String> {

		@Override
		public String convertToDatabaseColumn(MonetaryAmount attribute) {
			return attribute.toString();
		}

		@Override
		public MonetaryAmount convertToEntityAttribute(String dbData) {
			return MonetaryAmount.fromString(dbData);
		}

	}
}
