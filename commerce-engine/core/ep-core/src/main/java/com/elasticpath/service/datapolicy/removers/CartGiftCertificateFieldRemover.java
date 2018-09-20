/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import java.util.Collection;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.service.datapolicy.DataPointLocationEnum;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

/**
 * The remover for cart GC fields.
 */
public class CartGiftCertificateFieldRemover extends AbstractDataPointValueRemover {

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.CART_GIFT_CERTIFICATE.getName();
	}

	@Override
	public Pair<String, String> getJPQLUpdate() {
		return new Pair<>("ShoppingItemData", "data");
	}

	@Override
	protected String getRemoveQuery(final Collection<DataPointValue> dataPointValues) {
		Pair<String, String> entityAliasPair = getJPQLUpdate();

		String entity = entityAliasPair.getFirst();
		String alias = entityAliasPair.getSecond();

		String deleteQuery = "DELETE FROM " + entity + " " + alias;

		return new StringBuilder(deleteQuery).append(' ')
			.append(getWhereClauseForEntity(alias))
			.toString();
	}
}
