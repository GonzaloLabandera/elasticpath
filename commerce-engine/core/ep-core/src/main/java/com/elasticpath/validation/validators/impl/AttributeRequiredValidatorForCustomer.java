/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.validation.validators.impl;

import java.util.Collection;
import javax.validation.ConstraintValidatorContext;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.customer.Customer;

/**
 * Validator for {@link com.elasticpath.validation.constraints.AttributeRequired AttributeRequired} when dealing with {@link Customer}s.
 */
public class AttributeRequiredValidatorForCustomer extends AbstractAttributeRequiredValidator<Customer> {

	@SuppressWarnings("fallthrough")
	@Override
	public boolean isAttributeValid(final Attribute attribute, final Customer customer, final ConstraintValidatorContext context) {
		Object profileValue = customer.getCustomerProfile().getProfileValue(attribute.getKey());
		boolean result;
		switch (attribute.getAttributeType().getTypeId()) {
			case AttributeType.SHORT_TEXT_TYPE_ID:
			case AttributeType.LONG_TEXT_TYPE_ID:
				result = profileValue != null && profileValue.toString().trim().length() > 0;
				break;
			default:
				result = profileValue != null;
				break;
		}
		return result;
	}

	@Override
	protected void buildConstraintViolation(final ConstraintValidatorContext context, final Attribute attribute) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addNode("customerProfile").addNode(String.format("profileValueMap[%s]", attribute.getKey())).addConstraintViolation();
	}

	@Override
	protected Collection<Attribute> getAttributesToValidate(final Customer value) {
		return value.getCustomerProfile().getProfileAttributes(value.getCustomerType());
	}
}
