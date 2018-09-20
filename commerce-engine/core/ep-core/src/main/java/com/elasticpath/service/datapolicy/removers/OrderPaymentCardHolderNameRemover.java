/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.service.datapolicy.DataPointLocationEnum;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

/**
 * The order payment card holder name remover.
 */
public class OrderPaymentCardHolderNameRemover extends AbstractDataPointValueRemover {

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.ORDER_PAYMENT_CARD_HOLDER_NAME.getName();
	}

	@Override
	public Pair<String, String> getJPQLUpdate() {
		return new Pair<>("OrderPaymentImpl", "p");
	}

	@Override
	protected String getUpdateSetForEntity(final Collection<DataPointValue> dataPointValues, final String alias) {
		String jpqlSet = "SET #alias#.lastModifiedDate=CURRENT_TIMESTAMP, #alias#.cardHolderName='" + HYPHEN + "'";

		return StringUtils.replace(jpqlSet, "#alias#", alias);
	}
}
