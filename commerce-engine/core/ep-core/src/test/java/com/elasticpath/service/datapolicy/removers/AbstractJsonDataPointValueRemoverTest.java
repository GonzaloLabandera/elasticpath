/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.removers;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

/**
 * Common test class for all data point value removers.
 */
public abstract class AbstractJsonDataPointValueRemoverTest {
	private static final String DPV_1_KEY = "GDPR_KEY1";
	private static final String DPV_2_KEY = "GDPR_KEY2";

	private static final Long DPV_1_UIDPK = 1L;
	private static final Long DPV_2_UIDPK = 2L;

	@Mock
	private PersistenceEngine persistenceEngine;

	private DataPointValue dpv1;
	private DataPointValue dpv2;

	private Set<Long> uidPks1;
	private Set<Long> uidPks2;

	@Before
	public void init() {
		dpv1 = createDataPointValue(DPV_1_UIDPK, DPV_1_KEY);
		dpv2 = createDataPointValue(DPV_2_UIDPK, DPV_2_KEY);

		uidPks1 = Sets.newHashSet(DPV_1_UIDPK);
		uidPks2 = Sets.newHashSet(DPV_1_UIDPK, DPV_2_UIDPK);
	}

	@Test
	public void shouldRemoveOneDataPointValue() {

		List<Object[]> rows = new ArrayList<>();
		ModifierFieldsMapWrapper modifierFields = new ModifierFieldsMapWrapper();
		modifierFields.put(DPV_1_KEY, "GDPR_VAL1");

		rows.add(new Object[]{DPV_1_UIDPK, modifierFields});

		doReturn(rows).when(persistenceEngine)
				.retrieveWithList(getSelectModifierFieldsJPQL(), LIST_PARAMETER_NAME, uidPks1, null, 0, 1);

		List<DataPointValue> dataPointValues = Collections.singletonList(dpv1);

		int numOfDeletedRecords = getRemover().removeValues(dataPointValues);

		assertThat(numOfDeletedRecords)
				.as("One record should be deleted")
				.isEqualTo(1);

		verify(persistenceEngine).executeQuery(getUpdateModifierFieldsJPQL(), null, DPV_1_UIDPK);
	}

	@Test
	public void shouldBeApplicableForLocation() {
		assertThat(getRemover().isApplicableTo(getLocation()))
				.as("Should be applicable for", getLocation())
				.isTrue();
	}

	@Test
	public void shouldRemoveTwoDataPointValues() {
		List<Object[]> rows = new ArrayList<>();
		ModifierFieldsMapWrapper modifierFields1 = new ModifierFieldsMapWrapper();
		modifierFields1.put(DPV_1_KEY, "GDPR_VAL1");

		ModifierFieldsMapWrapper modifierFields2 = new ModifierFieldsMapWrapper();
		modifierFields2.put(DPV_2_KEY, "GDPR_VAL2");
		modifierFields2.put("ANY_DATA_KEY", "ANY_DATA_VALUE");

		rows.add(new Object[]{DPV_1_UIDPK, modifierFields1});
		rows.add(new Object[]{DPV_2_UIDPK, modifierFields2});

		doReturn(rows).when(persistenceEngine)
				.retrieveWithList(getSelectModifierFieldsJPQL(), LIST_PARAMETER_NAME, uidPks2, null, 0, 2);

		List<DataPointValue> dataPointValues = Arrays.asList(dpv1, dpv2);

		int numOfDeletedRecords = getRemover().removeValues(dataPointValues);

		assertThat(numOfDeletedRecords)
				.as("Two records should be deleted")
				.isEqualTo(2);

		verify(persistenceEngine).executeQuery(getUpdateModifierFieldsJPQL(), null, DPV_1_UIDPK);
		verify(persistenceEngine).executeQuery(getUpdateModifierFieldsJPQL(), "{\"ANY_DATA_KEY\":\"ANY_DATA_VALUE\"}", DPV_2_UIDPK);
	}

	private DataPointValue createDataPointValue(final Long uidPk, final String dataPointKey) {
		DataPointValue dpv = new DataPointValue();
		dpv.setUidPk(uidPk);
		dpv.setLocation(getLocation());
		dpv.setKey(dataPointKey);

		return dpv;
	}

	/**
	 * The implementation of {@link com.elasticpath.service.datapolicy.DataPointValueRemover}.
	 * @return the remover
	 */
	protected abstract AbstractDataPointValueRemover getRemover();

	/**
	 * The data point value location.
	 * @return the location
	 */
	protected abstract String getLocation();

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
