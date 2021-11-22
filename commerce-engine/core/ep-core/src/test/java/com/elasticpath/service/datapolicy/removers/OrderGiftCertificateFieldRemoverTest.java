/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import static com.elasticpath.service.datapolicy.removers.OrderGiftCertificateFieldRemover.SELECT_MODIFIER_FIELDS_JPQL;
import static com.elasticpath.service.datapolicy.removers.OrderGiftCertificateFieldRemover.UPDATE_MODIFIER_FIELDS_JPQL;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class OrderGiftCertificateFieldRemoverTest extends AbstractJsonDataPointValueRemoverTest {

	private static final String LOCATION = DataPointLocationEnum.ORDER_GIFT_CERTIFICATE.getName();

	@InjectMocks
	private OrderGiftCertificateFieldRemover remover;

	@Override
	protected AbstractDataPointValueRemover getRemover() {
		return remover;
	}

	@Override
	protected String getLocation() {
		return LOCATION;
	}

	@Override
	protected String getSelectModifierFieldsJPQL() {
		return SELECT_MODIFIER_FIELDS_JPQL;
	}

	@Override
	protected String getUpdateModifierFieldsJPQL() {
		return UPDATE_MODIFIER_FIELDS_JPQL;
	}
}
