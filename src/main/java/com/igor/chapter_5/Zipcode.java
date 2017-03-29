package com.igor.chapter_5;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeConverter;

public abstract class Zipcode {

	protected final String value;

	public Zipcode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Zipcode fromString(String value) {
		switch (value.length()) {
		case 4:
			return new ZipcodeFR(value);
		case 5:
			return new ZipcodeDE(value);
		default:
			// Consider cleaning up your database
			// Or create an InvalidZipCode subclass
			throw new IllegalArgumentException("Unsupported zipcode in database: " + value);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Zipcode zipcode = (Zipcode) o;
		return value.equals(zipcode.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	// A custom converter will take care of inheritance
	public static class Converter implements AttributeConverter<Zipcode, String> {

		@Override
		public String convertToDatabaseColumn(Zipcode attribute) {
			return attribute.getValue();
		}

		@Override
		public Zipcode convertToEntityAttribute(String dbData) {
			return Zipcode.fromString(dbData);
		}
	}

	// Can apply converter on a set/collections/etc.
	public static class ConverterSet implements AttributeConverter<Set<Zipcode>, String> {

		@Override
		public String convertToDatabaseColumn(Set<Zipcode> attributes) {

			if (attributes == null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			for (Zipcode zipCode : attributes) {
				sb.append(zipCode.getValue());
				sb.append(",");
			}

			return sb.toString();
		}

		@Override
		public Set<Zipcode> convertToEntityAttribute(String dbData) {

			String[] parts = dbData.split(",");
			Set<Zipcode> zipCodes = new HashSet<>();
			for (String part : parts) {
				zipCodes.add(Zipcode.fromString(part));
			}

			return zipCodes;
		}
	}
}
