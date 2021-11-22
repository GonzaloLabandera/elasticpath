/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.readers;

import static com.elasticpath.persistence.openjpa.util.ModifierFieldsMapper.toJSON;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.service.datapolicy.DataPointLocationEnum;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

/**
 * A data point value reader for any cart gift certificate field.
 *
 * There are 2 situations when GCs are created:
 *
 * 1. when added to the cart, but not purchased (this reader)
 * 2. when purchased {@link OrderGiftCertificateFieldReader
 */
public class CartGiftCertificateFieldReader extends AbstractDataPointValueReader {

	private static final Pair<String, String> CART_CLASS_ALIAS_PAIR = Pair.of("ShoppingCartMementoImpl", "cart");
	private static final Pair<String, String> SHOPPER_CLASS_ALIAS_PAIR = Pair.of("ShopperMementoImpl", "shopper");

	private static final String NAMED_VALIDATION_QUERY = "VERIFY_EXISTS_GIFT_CERTIFICATE_ATTRIBUTE";

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.CART_GIFT_CERTIFICATE.getName();
	}

	@Override
	public String getReadQuery(final Collection<String> dataPointKeys) {

		String query = "SELECT items.uidPk,items.creationDate,items.lastModifiedDate,items.modifierFields"
			.concat(" FROM #TABLE#")
			.concat(" INNER JOIN #cartAlias#.allItems items")
			.concat(" WHERE #shopperAlias#.uidPk = #cartAlias#.shopperUid")
			.concat(" AND #shopperAlias#.customer.guid = ?1");

		return finalizedJPQLQuerys(query);
	}

	@Override
	protected String[] getJPQLSearchList() {
		return new String[]{"#TABLE#", "#cartAlias#", "#shopperAlias#"};
	}

	@Override
	protected String[] getJPQLReplacementList() {
		List<Pair<String, String>> entityClassAndAliasPairs = getEntityClassAndAliasPairs();
		Pair<String, String> cartClassAndAliasPair = entityClassAndAliasPairs.get(0);
		Pair<String, String> shopperClassAndAliasPair = entityClassAndAliasPairs.get(1);

		String cartAlias = cartClassAndAliasPair.getSecond();
		String shopperAlias = shopperClassAndAliasPair.getSecond();

		return new String[]{getCSVFromTables(entityClassAndAliasPairs), cartAlias, shopperAlias};
	}

	@Override
	public boolean validateKey(final String dataKey) {
		return getDataPointValueService().validateKey(dataKey, getNamedValidationQuery());
	}

	@Override
	public Map<String, String> getSupportedFields() {
		return DataPointLocationEnum.CART_GIFT_CERTIFICATE.getSupportedFields();
	}

	/**
	 * Get named validation query.
	 *
	 * @return the named validation query.
	 */
	protected String getNamedValidationQuery() {
		return NAMED_VALIDATION_QUERY;
	}

	@Override
	public List<Pair<String, String>> getEntityClassAndAliasPairs() {
		return Arrays.asList(CART_CLASS_ALIAS_PAIR, SHOPPER_CLASS_ALIAS_PAIR);
	}

	@Override
	protected void setValue(final DataPointValue dpv, final Object[] row, final int attributeValueIndex) {
		ModifierFieldsMapWrapper modifierFields = (ModifierFieldsMapWrapper) row[attributeValueIndex];

		if (modifierFields != null) {
			dpv.setValue(modifierFields.get(dpv.getKey()));
		}
	}

	@Override
	protected String rawDataKeyToString(final Object rawDataKey) {
		return toJSON((ModifierFieldsMapWrapper) rawDataKey);
	}
}
