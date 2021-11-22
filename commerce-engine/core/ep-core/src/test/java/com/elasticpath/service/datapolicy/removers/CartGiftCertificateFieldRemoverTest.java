/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import static com.elasticpath.service.datapolicy.removers.CartGiftCertificateFieldRemover.SELECT_MODIFIER_FIELDS_JPQL;
import static com.elasticpath.service.datapolicy.removers.CartGiftCertificateFieldRemover.UPDATE_MODIFIER_FIELDS_JPQL;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class CartGiftCertificateFieldRemoverTest extends AbstractJsonDataPointValueRemoverTest {

	private static final String LOCATION = DataPointLocationEnum.CART_GIFT_CERTIFICATE.getName();

	@InjectMocks
	private CartGiftCertificateFieldRemover remover;

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
