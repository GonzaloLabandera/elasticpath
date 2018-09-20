/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentCardHolderNameRemoverTest extends AbstractDataPointValueRemoverTest {

	private static final String LOCATION = DataPointLocationEnum.ORDER_PAYMENT_CARD_HOLDER_NAME.getName();

	@InjectMocks
	private OrderPaymentCardHolderNameRemover remover;

	@Override
	protected String getLocation() {
		return LOCATION;
	}

	@Override
	protected AbstractDataPointValueRemover getRemover() {
		return remover;
	}

	@Override
	protected String getExpectedRemoveQuery(final boolean updateMoreFields) {

		return "UPDATE OrderPaymentImpl p SET p.lastModifiedDate=CURRENT_TIMESTAMP, p.cardHolderName='‐' WHERE p.uidPk IN (:uidpks)";
	}
}
