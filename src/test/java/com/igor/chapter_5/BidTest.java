package com.igor.chapter_5;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class BidTest {

	private static final String PERSISTENCE_UNIT = "Chapter5";

	@Test
	public void shouldApplyYesNoTypeAndConverter() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT);

		Item item = new Item();
		item.setInitialPrice(10.0);
		item.setImageBytesLob(new byte[0]);

		// Converter on
		MonetaryAmount monetaryAmount = new MonetaryAmount(BigDecimal.TEN, Currency.getInstance("USD"));
		Date createdOn = new Date();
		Zipcode deliveryZipCode = new ZipcodeDE("12345");
		Zipcode deliveryZipCode2 = new ZipcodeDE("54321");

		Set<Zipcode> alternativeZipCodes = new HashSet<>();
		alternativeZipCodes.add(deliveryZipCode);
		alternativeZipCodes.add(deliveryZipCode2);
		// ===

		Bid bid = new Bid();
		bid.setAmount(100.0);
		bid.setVerified(true);
		bid.setItem(item);
		bid.setMonetaryAmount(monetaryAmount);
		bid.setCreatedOn(createdOn);
		bid.setDeliveryZipCode(deliveryZipCode);
		bid.setAlternativeZipCodes(alternativeZipCodes);

		client.persist(item, bid);
		// verified: stored as 'Y', column type: char(1)
		// monetaryAmount: stored as 10 USD, column length 10
		// createdOn: column type: datetime(6)
		// deliveryZipCode: column type: varchar(255)

		Bid bidDb = client.find(Bid.class, bid.getId());

		Assertions.assertThat(bidDb.isVerified()).isEqualTo(bid.isVerified());
		Assertions.assertThat(bidDb.getMonetaryAmount()).isEqualTo(monetaryAmount);
		Assertions.assertThat(bidDb.getCreatedOn()).isExactlyInstanceOf(Date.class);
		Assertions.assertThat(bidDb.getDeliveryZipCode()).isExactlyInstanceOf(ZipcodeDE.class);
		Assertions.assertThat(bidDb.getAlternativeZipCodes()).hasSize(2);

		client.close();
	}

}
