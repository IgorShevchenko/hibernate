package com.igor.chapter_5;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.AttributeConverter;

/**
 * Converters aren’t limited to custom classes: you can even override
 * Hibernate’s built-in type adapters. For example, you could create a custom
 * converter for some or even all java.util.Date properties in your domain mode
 */
public class DateConverter implements AttributeConverter<Date, Date> {

	@Override
	public Date convertToDatabaseColumn(Date attribute) {
		return attribute;
	}

	@Override
	public Date convertToEntityAttribute(Date dbData) {
		if (dbData == null) {
			return null;
		}

		// Convert from java.sql.Datetime
		if (dbData instanceof Timestamp) {
			return new Date(dbData.getTime());
		}

		return dbData;
	}

}
