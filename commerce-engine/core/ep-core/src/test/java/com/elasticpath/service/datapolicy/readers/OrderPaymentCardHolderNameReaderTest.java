/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.readers;

import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentCardHolderNameReaderTest extends AbstractDataPointValueReaderTest {

	private static final String LOCATION = DataPointLocationEnum.ORDER_PAYMENT_CARD_HOLDER_NAME.getName();

	private static final String DATAPOINT_1_KEY = "ORDER_PAYMENT_CARD_HOLDER_NAME";
	private static final String DATAPOINT_1_DB_FIELD_NAME = "cardHolderName";
	private static final String DATAPOINT_1_DB_VALUE = "card holder name";

	private static final String DATAPOINT_2_KEY = "ORDER_PAYMENT_CARD_HOLDER_NAME";

	@InjectMocks
	private OrderPaymentCardHolderNameReader reader;

	@Override
	protected String getLocation() {
		return LOCATION;
	}


	@Override
	protected AbstractDataPointValueReader getReader() {
		return reader;
	}

	@Override
	protected String getDataPoint1Key() {
		return DATAPOINT_1_KEY;
	}

	@Override
	protected String getDataPoint2Key() {
		return DATAPOINT_2_KEY;
	}

	@Override
	protected String getDataPoint1Value() {
		return DATAPOINT_1_DB_VALUE;
	}

	@Override
	protected String getDataPoint1DbFieldName() {
		return DATAPOINT_1_DB_FIELD_NAME;
	}

	@Override
	protected String getExpectedReadQuery(final String... dataPointKeys) {

		return "SELECT p.uidPk, p.createdDate, p.lastModifiedDate".concat(",'")
			.concat(DATAPOINT_1_KEY)
			.concat("',p.")
			.concat(DATAPOINT_1_DB_FIELD_NAME)
			.concat(" FROM OrderImpl o")
			.concat(" INNER JOIN o.orderPayments p")
			.concat(" WHERE o.customer.guid = ?1");
	}
}
