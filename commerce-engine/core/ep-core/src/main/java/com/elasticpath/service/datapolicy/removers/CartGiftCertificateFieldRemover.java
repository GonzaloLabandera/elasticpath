/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

/**
 * The remover for cart GC fields.
 */
public class CartGiftCertificateFieldRemover extends AbstractJsonDataPointValueRemover {

	/** Query for fetching order modifier fields. */
	protected static final String SELECT_MODIFIER_FIELDS_JPQL =
			"SELECT cartItem.uidPk, cartItem.modifierFields FROM ShoppingItemImpl cartItem WHERE cartItem.uidPk IN (:list)";
	/** The statement for updating order modifier fields. */
	protected static final String UPDATE_MODIFIER_FIELDS_JPQL =
			"UPDATE ShoppingItemImpl cartItem SET cartItem.modifierFields=?1 WHERE cartItem.uidPk=?2";



	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.CART_GIFT_CERTIFICATE.getName();
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
