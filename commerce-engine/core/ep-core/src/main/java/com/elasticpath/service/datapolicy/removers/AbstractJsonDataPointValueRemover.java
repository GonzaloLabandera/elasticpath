/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import static com.elasticpath.persistence.openjpa.util.ModifierFieldsMapper.toJSON;
import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

/**
 * The abstract JSON data point value remover.
 */
public abstract class AbstractJsonDataPointValueRemover extends AbstractDataPointValueRemover {

	private PersistenceEngine persistenceEngine;
	private int removedDPVsCounter;

	/**
	 * The Cart(Item) and Order(Item) modifier fields are stored as JSON.
	 * The modifier fields may be different across the items, thus it is not possible to create a single DELETE/UPDATE statement
	 * like for other removers.
	 *
	 * Instead, the modifier fields must be retrieved first, converted from JSON to a map, data point removed from the map,
	 * modifier fields map serialized and finally, and the owner entity updated in db.
	 *
	 * This is not performant, but with current db and OpenJPA versions this is the best we can get.
	 * Newer versions of MySQL (>= 5.7) provide JSON support and should be possible to remove JSON field using an UPDATE statement.
	 * However, it is questionable whether OpenJPA will provide similar support.
	 *
	 * @param dataPointValues the collection of {@link DataPointValue} instances to remove.
	 * @return the number of removed data point values
	 */
	@Override
	public int removeValues(final Collection<DataPointValue> dataPointValues) {
		removedDPVsCounter = 0;

		Map<Long, String> uidPkToKeyMap = dataPointValues.stream()
				.collect(Collectors.toMap(DataPointValue::getUidPk, DataPointValue::getKey));

		List<Object[]> rows = persistenceEngine
				.retrieveWithList(getSelectModifierFieldsJPQL(), LIST_PARAMETER_NAME,
						uidPkToKeyMap.keySet(), null, 0, uidPkToKeyMap.size());

		rows.forEach(row -> {
			Long uidPk = (Long) row[0];

			ModifierFieldsMapWrapper modifierFields = (ModifierFieldsMapWrapper) row[1];
			modifierFields.remove(uidPkToKeyMap.get(uidPk));

			String orderDataJson = toJSON(modifierFields);

			persistenceEngine.executeQuery(getUpdateModifierFieldsJPQL(), orderDataJson, uidPk);

			removedDPVsCounter++;
		});

		return removedDPVsCounter;
	}

	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * JPQL query for retrieving modifier fields.
	 *
	 * @return the JPQL query
	 */
	protected abstract String getSelectModifierFieldsJPQL();

	/**
	 * JPQL statement for updating modifier fields.
	 *
	 * @return the JPQL update statement
	 */
	protected abstract String getUpdateModifierFieldsJPQL();
}
