/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.readers;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.service.datapolicy.DataPointLocationEnum;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class OrderDataValueReaderTest extends AbstractDataPointValueReaderTest {

	private static final String LOCATION = DataPointLocationEnum.ORDER_DATA.getName();
	private static final String DATAPOINT_1_KEY = "order.data.key.1";
	private static final String DATAPOINT_2_KEY = "order.data.key.2";
	private static final String DATAPOINT_1_DB_VALUE = "oDataDbValue1";

	@InjectMocks
	private OrderDataValueReader reader;

	@Override
	protected String getLocation() {
		return LOCATION;
	}

	@Override
	protected AbstractDataPointValueReader getReader() {
		return reader;
	}

	@Override
	protected String getExpectedReadQuery(final String... dataPointKeys) {

		return "SELECT ord.uidPk,ord.createdDate,ord.lastModifiedDate,ord.modifierFields"
			.concat(" FROM OrderImpl ord")
			.concat(" WHERE ord.customer.guid = ?1");
	}

	@Override
	protected String getDataPoint1Key() {
		return DATAPOINT_1_KEY;
	}

	@Override
	protected String getDataPoint1Value() {
		return DATAPOINT_1_DB_VALUE;
	}

	@Override
	protected String getDataPoint2Key() {
		return DATAPOINT_2_KEY;
	}

	@Override
	@Test
	public void shouldBeFalseForInvalidKey() {
		//validation always returns true
	}

	@Override
	protected List<Object[]> getRawData(final boolean shouldIncludeFieldValue) {
		ModifierFieldsMapWrapper modifierFields = new ModifierFieldsMapWrapper();
		modifierFields.put(DATAPOINT_1_KEY, DATAPOINT_1_DB_VALUE);

		Object[] rawData = new Object[]{UIDPK, CREATED_DATE, LAST_MODIFIED_DATE, modifierFields};

		return Collections.singletonList(rawData);
	}
}
