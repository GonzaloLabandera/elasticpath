/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

/**
 * The order data value remover.
 */
public class OrderDataValueRemover extends AbstractJsonDataPointValueRemover {
	/** Query for fetching order modifier fields. */
	protected static final String SELECT_MODIFIER_FIELDS_JPQL = "SELECT ord.uidPk, ord.modifierFields FROM OrderImpl ord WHERE ord.uidPk IN (:list)";
	/** The statement for updating order modifier fields. */
	protected static final String UPDATE_MODIFIER_FIELDS_JPQL = "UPDATE OrderImpl ord SET ord.modifierFields=?1 WHERE ord.uidPk=?2";

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.ORDER_DATA.getName();
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
