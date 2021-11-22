/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerIdentifierStrategy;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Test for {@link CustomerIdentifierServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerIdentifierServiceImplTest {

	private static final String GUID_ISSUER = "guid_issuer";
	private static final String SHARED_ID_ISSUER = "shared_id_issuer";
	private static final String ATTRIBUTE_VALUE_ISSUER = "attribute_key_issuer";
	private static final String INVALID_ISSUER = "invalid_issuer";
	private static final String INVALID_TEXT = "invalid_text";
	private static final String GUID_STR = "GUID";
	private static final String USER_ID_STR = "SHARED_ID";
	private static final String ATTRIBUTE_VALUE_STR = "ATTRIBUTE_VALUE";
	private static final String ATTRIBUTE_VALUE_SETTING = "ATTRIBUTE_VALUE:Punchout";
	private static final String TEST_USER_ID = "testUser";
	private static final String TEST_GUID = "testGuid";
	private static final String TEST_ATTRIBUTE_VALUE = "Test";

	@Mock
	private SettingValue guidSettingValue;

	@Mock
	private SettingValue userIdSettingValue;

	@Mock
	private SettingValue attributeKeySettingValue;


	@Mock
	private SettingsReader settingsReader;

	@Mock
	private CustomerIdentifierBySharedIdStrategyImpl customerIdentifierByUserIdStrategy;

	@Mock
	private CustomerIdentifierByGuidStrategyImpl customerIdentifierByGuidStrategy;

	@Mock
	private CustomerIdentifierByAttributeValueKeyStrategyImpl customerIdentifierByAttributeValueKeyStrategy;

	@InjectMocks
	private CustomerIdentifierServiceImpl customerIdentifierStrategyUtil;

	private static Collection<CustomerIdentifierStrategy> customerIdentifierStrategyList;

	@Before
	public void setUp() throws Exception {
		customerIdentifierStrategyList = new ArrayList<>();
		customerIdentifierStrategyList.add(customerIdentifierByGuidStrategy);
		customerIdentifierStrategyList.add(customerIdentifierByUserIdStrategy);
		customerIdentifierStrategyList.add(customerIdentifierByAttributeValueKeyStrategy);

		when(customerIdentifierByGuidStrategy.getCustomerIdentificationKeyField()).thenReturn(GUID_STR);
		when(customerIdentifierByUserIdStrategy.getCustomerIdentificationKeyField()).thenReturn(USER_ID_STR);
		when(customerIdentifierByAttributeValueKeyStrategy.getCustomerIdentificationKeyField()).thenReturn(ATTRIBUTE_VALUE_STR);

		FieldSetter.setField(
				customerIdentifierStrategyUtil,
				customerIdentifierStrategyUtil.getClass().getDeclaredField("customerIdentifierStrategyList"),
				customerIdentifierStrategyList);

		when(guidSettingValue.getValue()).thenReturn(GUID_STR);
		when(userIdSettingValue.getValue()).thenReturn(USER_ID_STR);
		when(attributeKeySettingValue.getValue()).thenReturn(ATTRIBUTE_VALUE_SETTING);

		when(settingsReader.getSettingValue(CustomerIdentifierServiceImpl.CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH, GUID_ISSUER))
				.thenReturn(guidSettingValue);
		when(settingsReader.getSettingValue(CustomerIdentifierServiceImpl.CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH, SHARED_ID_ISSUER))
				.thenReturn(userIdSettingValue);
		when(settingsReader.getSettingValue(CustomerIdentifierServiceImpl.CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH, ATTRIBUTE_VALUE_ISSUER))
				.thenReturn(attributeKeySettingValue);
		when(settingsReader.getSettingValue(CustomerIdentifierServiceImpl.CUSTOMER_IDENTIFIER_FIELD_SETTING_PATH, INVALID_ISSUER))
				.thenReturn(guidSettingValue);
	}

	/**
	 * Test getCustomerIdentifierKey. Checks GUID default value is returned if issuer is invalid.
	 */
	@Test
	public void testGetCustomerIdentifierKey() {
		assertEquals(GUID_STR, customerIdentifierStrategyUtil.getCustomerIdentifierKey(GUID_ISSUER));
		assertEquals(USER_ID_STR, customerIdentifierStrategyUtil.getCustomerIdentifierKey(SHARED_ID_ISSUER));
		assertEquals(ATTRIBUTE_VALUE_SETTING, customerIdentifierStrategyUtil.getCustomerIdentifierKey(ATTRIBUTE_VALUE_ISSUER));

		// Assert, for an invalid or non existent issuer, GUID is returned by default.
		assertEquals(GUID_STR, customerIdentifierStrategyUtil.getCustomerIdentifierKey(INVALID_ISSUER));
	}

	/**
	 * Test getCustomerIdentifierStrategyByIssuer for all issuer strategies.
	 */
	@Test
	public void testGetCustomerIdentifierStrategyByIssuer() {
		assertEquals(customerIdentifierByGuidStrategy, customerIdentifierStrategyUtil.getCustomerIdentifierStrategy(GUID_ISSUER).getData());

		assertEquals(customerIdentifierByUserIdStrategy,
				customerIdentifierStrategyUtil.getCustomerIdentifierStrategy(SHARED_ID_ISSUER).getData());

		assertEquals(customerIdentifierByAttributeValueKeyStrategy,
				customerIdentifierStrategyUtil.getCustomerIdentifierStrategy(ATTRIBUTE_VALUE_ISSUER).getData());
	}

	/**
	 * Test isCustomerExists with valid input.
	 */
	@Test
	public void testIsCustomerExists() {
		when(customerIdentifierByGuidStrategy.isCustomerExists(TEST_GUID, CustomerType.REGISTERED_USER, GUID_STR))
				.thenReturn(ExecutionResultFactory.createReadOK(null));
		when(customerIdentifierByUserIdStrategy.isCustomerExists(TEST_USER_ID, CustomerType.REGISTERED_USER, USER_ID_STR))
				.thenReturn(ExecutionResultFactory.createReadOK(null));
		when(customerIdentifierByAttributeValueKeyStrategy
				.isCustomerExists(TEST_ATTRIBUTE_VALUE, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_SETTING))
				.thenReturn(ExecutionResultFactory.createReadOK(null));

		assertTrue(customerIdentifierStrategyUtil.isCustomerExists(TEST_GUID, CustomerType.REGISTERED_USER, GUID_ISSUER).isSuccessful());
		assertTrue(customerIdentifierStrategyUtil.isCustomerExists(TEST_USER_ID, CustomerType.REGISTERED_USER, SHARED_ID_ISSUER).isSuccessful());
		assertTrue(customerIdentifierStrategyUtil
				.isCustomerExists(TEST_ATTRIBUTE_VALUE, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_ISSUER).isSuccessful());
	}

	/**
	 * Test isCustomerExists with invalid input.
	 */
	@Test
	public void testInvalidIsCustomerExists() {
		when(customerIdentifierByGuidStrategy.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, GUID_STR))
				.thenReturn(ExecutionResultFactory.createNotFound());
		when(customerIdentifierByUserIdStrategy.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, USER_ID_STR))
				.thenReturn(ExecutionResultFactory.createNotFound());
		when(customerIdentifierByAttributeValueKeyStrategy.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_SETTING))
				.thenReturn(ExecutionResultFactory.createNotFound());

		assertFalse(customerIdentifierStrategyUtil.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, GUID_ISSUER).isSuccessful());
		assertFalse(customerIdentifierStrategyUtil.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, SHARED_ID_ISSUER).isSuccessful());
		assertFalse(customerIdentifierStrategyUtil
				.isCustomerExists(INVALID_TEXT, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_ISSUER).isSuccessful());
	}

	/**
	 * Test deriveCustomerGuid with valid input.
	 */
	@Test
	public void testDeriveCustomerGuid() {
		when(customerIdentifierByGuidStrategy.deriveCustomerGuid(TEST_GUID, CustomerType.REGISTERED_USER, GUID_STR))
				.thenReturn(ExecutionResultFactory.createNotImplemented());
		when(customerIdentifierByUserIdStrategy.deriveCustomerGuid(TEST_USER_ID, CustomerType.REGISTERED_USER, USER_ID_STR))
				.thenReturn(ExecutionResultFactory.createReadOK(TEST_GUID));
		when(customerIdentifierByAttributeValueKeyStrategy
				.deriveCustomerGuid(TEST_ATTRIBUTE_VALUE, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_SETTING))
				.thenReturn(ExecutionResultFactory.createReadOK(TEST_GUID));

		assertEquals(ExecutionResultFactory.createNotFound(), customerIdentifierStrategyUtil
				.deriveCustomerGuid(TEST_GUID, CustomerType.REGISTERED_USER, GUID_ISSUER));
		assertEquals(TEST_GUID, customerIdentifierStrategyUtil.deriveCustomerGuid(TEST_USER_ID, CustomerType.REGISTERED_USER, SHARED_ID_ISSUER)
				.getData());
		assertEquals(TEST_GUID,
				customerIdentifierStrategyUtil.deriveCustomerGuid(TEST_ATTRIBUTE_VALUE, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_ISSUER)
						.getData());
	}

	/**
	 * Test deriveCustomerGuid with invalid input.
	 */
	@Test
	public void testDeriveInvalidCustomerGuid() {
		when(customerIdentifierByGuidStrategy.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, GUID_STR))
				.thenReturn(ExecutionResultFactory.createNotFound());
		when(customerIdentifierByUserIdStrategy.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, USER_ID_STR))
				.thenReturn(ExecutionResultFactory.createNotFound());
		when(customerIdentifierByAttributeValueKeyStrategy.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_SETTING))
				.thenReturn(ExecutionResultFactory.createNotFound());

		assertFalse(customerIdentifierStrategyUtil.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, GUID_ISSUER).isSuccessful());
		assertFalse(customerIdentifierStrategyUtil.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, SHARED_ID_ISSUER).isSuccessful());
		assertFalse(customerIdentifierStrategyUtil
				.deriveCustomerGuid(INVALID_TEXT, CustomerType.REGISTERED_USER, ATTRIBUTE_VALUE_ISSUER).isSuccessful());
	}
}