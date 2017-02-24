package com.igor.chapter_3;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;

public class ItemTest {

	@Test
	public void validate() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Item item = new Item();
		item.setBuyNowPrice(BigDecimal.TEN);
		item.setAuctionEnd(new Date(500000));
		item.setName("a");

		Set<ConstraintViolation<Item>> constraintViolations = validator.validate(item);

		Assert.assertEquals(2, constraintViolations.size());
	}

}
