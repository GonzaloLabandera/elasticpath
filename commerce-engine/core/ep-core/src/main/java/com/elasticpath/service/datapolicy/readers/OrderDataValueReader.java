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
 * A data point value reader for any order data value.
 */
public class OrderDataValueReader extends AbstractDataPointValueReader {

	//don't use "order" for alias - it's a db keyword
	private static final Pair<String, String> ENTITY_CLASS_ALIAS_PAIR = Pair.of("OrderImpl", "ord");

	@Override
	public String getSupportedLocation() {
		return DataPointLocationEnum.ORDER_DATA.getName();
	}

	@Override
	public String getReadQuery(final Collection<String> dataPointKeys) {

		String query =  "SELECT #alias#.uidPk,#alias#.createdDate,#alias#.lastModifiedDate,#alias#.modifierFields"
			.concat(" FROM #TABLE#")
			.concat(" WHERE #alias#.customer.guid = ?1");

		return finalizedJPQLQuerys(query);
	}

	@Override
	protected String[] getJPQLSearchList() {
		return new String[]{"#TABLE#", "#alias#"};
	}

	@Override
	protected String[] getJPQLReplacementList() {
		List<Pair<String, String>> entityClassAndAliasPairs = getEntityClassAndAliasPairs();
		String entityAlias = entityClassAndAliasPairs.get(0).getSecond();

		return new String[]{getCSVFromTables(entityClassAndAliasPairs), entityAlias};
	}

	/**
	 * It is not possible to reliably validate the key because order data
	 * can be populated on various ways, based on customer requirements.
	 *
	 * Those that populate order data and those that create data points should be in sync
	 * and agree about key names.
	 *
	 * @param dataKey the data key to be validated.
	 *
	 * @return
	 */
	@Override
	public boolean validateKey(final String dataKey) {
		return true;
	}

	@Override
	public Map<String, String> getSupportedFields() {
		return DataPointLocationEnum.ORDER_DATA.getSupportedFields();
	}

	@Override
	public List<Pair<String, String>> getEntityClassAndAliasPairs() {
		return Arrays.asList(ENTITY_CLASS_ALIAS_PAIR);
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
