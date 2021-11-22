/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.service.datapolicy.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.datapolicy.DataPoint;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.service.datapolicy.DataPointLocationEnum;
import com.elasticpath.service.datapolicy.impl.DataPointValue;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
@RunWith(MockitoJUnitRunner.class)
public class CartGiftCertificateFieldReaderTest extends AbstractDataPointValueReaderTest {

	private static final String LOCATION = DataPointLocationEnum.CART_GIFT_CERTIFICATE.getName();

	private static final String DATAPOINT_1_KEY = "giftCertificate.recipientName";
	private static final String DATAPOINT_1_DB_VALUE = "Recipient";

	private static final String DATAPOINT_2_KEY = "giftCertificate.recipientEmail";
	private static final String DATAPOINT_2_DB_VALUE = "Recipient@Email";

	@InjectMocks
	private CartGiftCertificateFieldReader reader;

	@Override
	protected String getLocation() {
		return LOCATION;
	}

	@Override
	protected AbstractDataPointValueReader getReader() {
		return reader;
	}

	@Override
	protected String getDataPoint1Key() {
		return DATAPOINT_1_KEY;
	}

	@Override
	protected String getDataPoint2Key() {
		return DATAPOINT_2_KEY;
	}

	@Override
	protected String getDataPoint1Value() {
		return DATAPOINT_1_DB_VALUE;
	}

	@Override
	protected void mockDataServiceIfRequired(final String dataPointKey, final boolean returnValue) {
		when(dataPointValueService.validateKey(dataPointKey, reader.getNamedValidationQuery()))
		.thenReturn(returnValue);
	}

	@Override
	protected String getExpectedReadQuery(final String... dataPointKeys) {

		return "SELECT items.uidPk,items.creationDate,items.lastModifiedDate,items.modifierFields"
			.concat(" FROM ShoppingCartMementoImpl cart, ShopperMementoImpl shopper")
			.concat(" INNER JOIN cart.allItems items")
			.concat(" WHERE shopper.uidPk = cart.shopperUid")
			.concat(" AND shopper.customer.guid = ?1");
	}

	@Test
	public void shouldConvertRawData() {
		long uidPk1 = 1L;
		long uidPk2 = 2L;

		Date createdDate = new Date();
		Date lastModifiedDate = new Date();

		String dataPoint1Name = "Cart GC Recipient Name";
		String dataPoint2Name = "Cart GC Recipient Email";

		String dataPoint1Description = "Description 1";
		String dataPoint2Description = "Description 2";

		ModifierFieldsMapWrapper modifierFields1 = new ModifierFieldsMapWrapper();
		modifierFields1.put(DATAPOINT_1_KEY, DATAPOINT_1_DB_VALUE);

		Object[] row1 = new Object[]{uidPk1, createdDate, lastModifiedDate, modifierFields1};

		ModifierFieldsMapWrapper modifierFields2 = new ModifierFieldsMapWrapper();
		modifierFields2.put(DATAPOINT_2_KEY, DATAPOINT_2_KEY);

		Object[] row2 = new Object[]{uidPk2, createdDate, lastModifiedDate, modifierFields2};

		List<Object[]> rawData = Arrays.asList(row1, row2);

		DataPoint cartGCDRecipientNameDataPoint = createDataPoint(dataPoint1Name, DATAPOINT_1_KEY, dataPoint1Description);
		DataPoint cartGCDRecipientEmailDataPoint = createDataPoint(dataPoint2Name, DATAPOINT_2_KEY, dataPoint2Description);


		DataPointValue expectedDataPointValue1 =  createDataPointValue(uidPk1, createdDate, lastModifiedDate, dataPoint1Name, DATAPOINT_1_KEY,
			DATAPOINT_1_DB_VALUE);

		DataPointValue expectedDataPointValue2 =  createDataPointValue(uidPk2, createdDate, lastModifiedDate, dataPoint2Name, DATAPOINT_2_KEY,
			DATAPOINT_2_DB_VALUE);

		List<DataPointValue> dataPointValues = reader.convertRawData(rawData, CUSTOMER_GUID, Arrays.asList(cartGCDRecipientNameDataPoint,
			cartGCDRecipientEmailDataPoint));

		assertThat(dataPointValues)
			.containsExactly(expectedDataPointValue1, expectedDataPointValue2);
	}

	@Override
	protected List<Object[]> getRawData(final boolean shouldIncludeFieldValue) {
		ModifierFieldsMapWrapper modifierFields = new ModifierFieldsMapWrapper();
		modifierFields.put(DATAPOINT_1_KEY, DATAPOINT_1_DB_VALUE);

		Object[] rawData = new Object[]{UIDPK, CREATED_DATE, LAST_MODIFIED_DATE, modifierFields};

		return Collections.singletonList(rawData);
	}

}
