/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import com.elasticpath.service.datapolicy.DataPointLocationEnum;

/**
 * The order GC value remover.
 */
public class OrderGiftCertificateFieldRemover extends AbstractJsonDataPointValueRemover {

	/** Query for fetching order modifier fields. */
	protected static final String SELECT_MODIFIER_FIELDS_JPQL =
			"SELECT ordSku.uidPk, ordSku.modifierFields FROM OrderSkuImpl ordSku WHERE ordSku.uidPk IN (:list)";
	/** The statement for updating order modifier fields. */
	protected static final String UPDATE_MODIFIER_FIELDS_JPQL =
			"UPDATE OrderSkuImpl ordSku SET ordSku.modifierFields=?1 WHERE ordSku.uidPk=?2";

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.ORDER_GIFT_CERTIFICATE.getName();
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
