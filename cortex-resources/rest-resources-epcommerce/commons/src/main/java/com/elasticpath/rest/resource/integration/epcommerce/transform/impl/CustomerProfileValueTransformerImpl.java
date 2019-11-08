/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.transform.impl;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;

/**
 * Implementation class for <code>CustomerProfileValueTransformer</code>.
 */
@Component
public class CustomerProfileValueTransformerImpl implements CustomerProfileValueTransformer {

	@Reference
	private DateForEditTransformer dateForEditTransformer;

	@Override
	@SuppressWarnings({ "PMD.MissingBreakInSwitch", "fallthrough" }) // PMD false positive bug - https://sourceforge.net/p/pmd/bugs/1262
	public String transformToString(final CustomerProfileValue customerProfileValue) {
		Objects.requireNonNull(customerProfileValue);

		final AttributeType attributeType = customerProfileValue.getAttributeType();
		switch (attributeType.getTypeId()) {
			case AttributeType.DATE_TYPE_ID:
			case AttributeType.DATETIME_TYPE_ID:
				return dateForEditTransformer.transformToString(attributeType, customerProfileValue.getDateValue()).orElse(StringUtils.EMPTY);
			default:
				return customerProfileValue.toString();
		}
	}
}
