/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.factory.TestPaymentGatewayPluginFactoryImpl;
import com.elasticpath.domain.payment.PaymentGateway;
import com.elasticpath.domain.payment.PaymentGatewayFactory;
import com.elasticpath.domain.payment.PaymentGatewayProperty;
import com.elasticpath.plugin.payment.PaymentGatewayPlugin;
import com.elasticpath.plugin.payment.PaymentGatewayType;
import com.elasticpath.plugin.payment.capabilities.CreditCardCapability;
import com.elasticpath.plugin.payment.capabilities.PaymentGatewayCapability;
import com.elasticpath.plugin.payment.dto.OrderPaymentDto;
import com.elasticpath.plugin.payment.dto.PayerAuthenticationEnrollmentResultDto;
import com.elasticpath.plugin.payment.dto.ShoppingCartDto;
import com.elasticpath.service.payment.PaymentGatewayService;
import com.elasticpath.service.payment.impl.PaymentGatewayServiceImpl;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

/**
 * Integration test for {@link PaymentGatewayServiceImpl}.
 */
@DirtiesContext
public class PaymentGatewayServiceImplTest extends DbTestCase {
	private static final String NON_EXISTENT_GATEWAY_NAME = "nonExistentPaymentGateway";

	private static final String GATEWAY_TYPE = "testPaymentGatewayType";

	private static String gatewayName;

	/** The main object under test. */
	@Autowired
	private PaymentGatewayService paymentGatewayService;

	@Autowired
	private TestPaymentGatewayPluginFactoryImpl paymentGatewayPluginFactory;

	private final TestPaymentGatewayPlugin testPaymentGatewayPlugin = new TestPaymentGatewayPlugin();

	@Before
	public void setUp() {
		gatewayName = Utils.uniqueCode("testPaymentGateway");
	}

	@After
	public void tearDown() {
		paymentGatewayPluginFactory.getPaymentGatewayPlugins().remove(testPaymentGatewayPlugin);
	}

	/**
	 * Ensure added gateway is correctly formed.
	 */
	@Test
	public void ensureAddedGatewayIsCorrectlyFormed() {
		Properties properties = getTestProperties();
		PaymentGateway addedGateway = paymentGatewayService.addPaymentGateway(gatewayName, GATEWAY_TYPE, properties);
		assertNotNull("Persisted gateway should not be null", addedGateway);
		assertGatewayHasName(addedGateway, gatewayName);
		assertGatewayHasType(addedGateway, GATEWAY_TYPE);
		assertGatewayHasProperties(addedGateway, getTestProperties());
	}

	/**
	 * Ensure added gateway is persisted.
	 */
	@Test
	public void ensureAddedGatewayIsPersisted() {
		PaymentGateway addedGateway = createUniquePaymentGateway(getEmptyProperties(), GATEWAY_TYPE, gatewayName);
		PaymentGateway loadedGateway = paymentGatewayService.getGatewayByName(gatewayName);
		assertGatewaysMatch(addedGateway, loadedGateway);
	}

	/**
	 * Ensure new gateway is persisted.
	 */
	@Test
	public void ensureNewGatewayIsPersisted() {
		paymentGatewayService.saveOrUpdate(getTestGateway());
		PaymentGateway loadedGateway = paymentGatewayService.getGatewayByName(gatewayName);
		assertGatewaysMatch(getTestGateway(), loadedGateway);
	}

	@Test
	public void ensureGatewayWithNoPluginImplementationHasUnresolvedPlugin() {
		paymentGatewayService.saveOrUpdate(getTestGateway());
		PaymentGateway loadedGateway = paymentGatewayService.getGatewayByName(gatewayName);
		assertFalse("Gateways without plugin implementations should not have an installed plugin. ", loadedGateway.isPaymentGatewayPluginInstalled());
	}

	/**
	 * Ensure retrieval of non existent gateway returns null.
	 */
	@Test
	public void ensureRetrievalOfNonExistentGatewayReturnsNull() {
		assertNull(paymentGatewayService.getGatewayByName(NON_EXISTENT_GATEWAY_NAME));
	}

	/**
	 * Ensure removed gateway is deleted.
	 */
	@Test
	public void ensureRemovedGatewayIsDeleted() {
		PaymentGateway savedGateway = paymentGatewayService.saveOrUpdate(getTestGateway());
		PaymentGateway gatewayLoadedBeforeRemoval = paymentGatewayService.getGatewayByName(gatewayName);
		assertGatewaysMatch(savedGateway, gatewayLoadedBeforeRemoval);

		paymentGatewayService.remove(savedGateway);
		assertNull(paymentGatewayService.getGatewayByName(gatewayName));
	}

	/**
	 * Ensure supported credit card types are loaded.
	 */
	@Test
	public void ensureSupportedCreditCardTypesAreLoaded() {
		addTestPluginToGatewayFactory();
		Set<String> supportedCreditCardTypes = paymentGatewayService.getSupportedCreditCardTypes();
		assertTrue("Test gateway credit card type is missing", supportedCreditCardTypes.contains(TestPaymentGatewayPlugin.TEST_CARD_TYPE));
	}

	/**
	 * Ensure default gateway properties are loaded.
	 */
	@Test
	public void ensureDefaultGatewayPropertiesAreLoaded() {
		addTestPluginToGatewayFactory();
		Properties defaultProperties = paymentGatewayService.getPaymentGatewayDefaultProperties(GATEWAY_TYPE);
		assertTrue("Test gateway default properties are missing",
				defaultProperties.containsKey(TestPaymentGatewayPlugin.TEST_CONFIGURATION_PARAMETER));
	}

	private void addTestPluginToGatewayFactory() {
		paymentGatewayPluginFactory.getPaymentGatewayPlugins().add(testPaymentGatewayPlugin);
	}

	private void assertGatewaysMatch(final PaymentGateway expected, final PaymentGateway actual) {
		assertNotNull(actual);
		assertEquals("Gateway names do not match", expected.getName(), actual.getName());
		assertEquals("Gateway types do not match", expected.getType(), actual.getType());
		assertPropertiesMatch(expected.getPropertiesMap(), actual.getPropertiesMap());
	}

	private void assertGatewayHasName(final PaymentGateway gateway, final String name) {
		assertEquals("Persisted gateway name is incorrect. ", name, gateway.getName());
	}

	private void assertGatewayHasType(final PaymentGateway gateway, final String type) {
		assertEquals("Persisted gateway type is incorrect. ", type, gateway.getType());
	}

	private void assertGatewayHasProperties(final PaymentGateway gateway, final Properties properties) {
		Map<String, PaymentGatewayProperty> gatewayProperties = gateway.getPropertiesMap();

		for (final Map.Entry<String, PaymentGatewayProperty> gatewayEntry : gatewayProperties.entrySet()) {
			assertEquals("Gateway property for key " + gatewayEntry.getKey() + " does not match ", properties.get(gatewayEntry.getKey()), gatewayEntry.getValue().getValue());
		}
		assertEquals("Properties have a different size", properties.size(), gatewayProperties.size());

	}

	private void assertPropertiesMatch(final Map<String, PaymentGatewayProperty> expectedMap, final Map<String, PaymentGatewayProperty> actualMap) {
		for (final Map.Entry<String, PaymentGatewayProperty> expectedEntry : expectedMap.entrySet()) {
			assertEquals("Gateway property for key " + expectedEntry.getKey() + " does not match ", expectedEntry.getValue().getValue(), actualMap.get(expectedEntry.getKey()).getValue());
		}
		assertEquals("Properties have a different size", expectedMap.size(), actualMap.size());
	}

	private PaymentGateway getTestGateway() {
		PaymentGateway newGateway = paymentGatewayPluginFactory.getPaymentGateway(GATEWAY_TYPE);
		newGateway.setName(gatewayName);
		Properties testProperties = getTestProperties();
		newGateway.setProperties(testProperties);
		return newGateway;
	}

	private Properties getTestProperties() {
		Properties testProperties = getEmptyProperties();
		testProperties.put("testKey", "testValue");
		return testProperties;
	}

	private Properties getEmptyProperties() {
		Properties emptyProperties = new Properties();
		return emptyProperties;
	}

	private PaymentGateway createUniquePaymentGateway(final Properties properties, final String gatewayType, final String gatewayName) {
		return paymentGatewayService.addPaymentGateway(gatewayName, gatewayType, properties);
	}

	/**
	 * Test Payment Gateway Plugin used to verify {@link PaymentGatewayFactory} operations.
	 */
	public static class TestPaymentGatewayPlugin implements PaymentGatewayPlugin, CreditCardCapability {

		/** The Constant TEST_CARD_TYPE. */
		public static final String TEST_CARD_TYPE = "testCardType";

		/** The Constant TEST_CONFIGURATION_PARAMETER. */
		public static final String TEST_CONFIGURATION_PARAMETER = "testConfigurationParameter";

		private static final String NOT_IMPLEMENTED_ERROR_MESSAGE = "Test payment gateway: Not implemented";

		@Override
		public <T extends PaymentGatewayCapability> T getCapability(final Class<T> capability) {
			if (capability.isAssignableFrom(this.getClass())) {
				return capability.cast(this);
			}
			return null;
		}

		@Override
		public String getPluginType() {
			return GATEWAY_TYPE;
		}

		@Override
		public PaymentGatewayType getPaymentGatewayType() {
			return PaymentGatewayType.CREDITCARD;
		}

		@Override
		public boolean isResolved() {
			return true;
		}

		@Override
		public void setConfigurationValues(final Map<String, String> configurations) {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public Collection<String> getConfigurationParameters() {
			return Collections.singletonList(TEST_CONFIGURATION_PARAMETER);
		}

		@Override
		public void setCertificatePathPrefix(final String certificatePathPrefix) {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public boolean isCvv2ValidationEnabled() {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public void setValidateCvv2(final boolean validate) {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public PayerAuthenticationEnrollmentResultDto checkEnrollment(final ShoppingCartDto shoppingCart, final OrderPaymentDto payment) {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public boolean validateAuthentication(final OrderPaymentDto payment, final String paRes) {
			throw new EpServiceException(NOT_IMPLEMENTED_ERROR_MESSAGE);
		}

		@Override
		public List<String> getSupportedCardTypes() {
			return Collections.singletonList(TEST_CARD_TYPE);
		}
	}

}
