/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.domain;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderImpl;

@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderTest {

	private static final String TEST_KEY = "testKey";
	private static final String TEST_DATA = "testData";
	private static final Map<String, String> PLUGIN_CONFIG_DATA = ImmutableMap.of(TEST_KEY, TEST_DATA);

	private PaymentProvider testee;

	@Mock
	private PaymentProviderPlugin plugin;

	@Mock
	private PaymentProviderConfiguration configuration;

	@Mock
	private PaymentProviderConfigurationData paymentProviderConfigurationData;


	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		when(paymentProviderConfigurationData.getKey()).thenReturn(TEST_KEY);
		when(paymentProviderConfigurationData.getData()).thenReturn(TEST_DATA);

		when(configuration.getPaymentConfigurationData()).thenReturn(
				Stream.of(paymentProviderConfigurationData).collect(Collectors.toSet()));

		when(plugin.getCapability(any())).then((Answer<Optional<Capability>>)
				invocationOnMock -> Optional.of(mock((Class<Capability>) invocationOnMock.getArguments()[0])));
		when(plugin.getClassLoader()).thenReturn(getClass().getClassLoader());

		testee = new PaymentProviderImpl(plugin, configuration);
	}

	@Test
	public void testModifyCapability() throws PaymentCapabilityRequestFailedException {
		ModifyCapabilityRequest request = mock(ModifyCapabilityRequest.class);
		Optional<ModifyCapability> capability = testee.getCapability(ModifyCapability.class);

		assertTrue(capability.isPresent());

		capability.get().modify(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testCreditCapability() throws PaymentCapabilityRequestFailedException {
		CreditCapabilityRequest request = mock(CreditCapabilityRequest.class);
		Optional<CreditCapability> capability = testee.getCapability(CreditCapability.class);

		assertTrue(capability.isPresent());

		capability.get().credit(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testCancelCapability() throws PaymentCapabilityRequestFailedException {
		CancelCapabilityRequest request = mock(CancelCapabilityRequest.class);
		Optional<CancelCapability> capability = testee.getCapability(CancelCapability.class);

		assertTrue(capability.isPresent());

		capability.get().cancel(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testChargeCapability() throws PaymentCapabilityRequestFailedException {
		ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
		Optional<ChargeCapability> capability = testee.getCapability(ChargeCapability.class);

		assertTrue(capability.isPresent());

		capability.get().charge(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testReserveCapability() throws PaymentCapabilityRequestFailedException {
		ReserveCapabilityRequest request = mock(ReserveCapabilityRequest.class);
		Optional<ReserveCapability> capability = testee.getCapability(ReserveCapability.class);

		assertTrue(capability.isPresent());

		capability.get().reserve(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testReverseChargeCapability() throws PaymentCapabilityRequestFailedException {
		ReverseChargeCapabilityRequest request = mock(ReverseChargeCapabilityRequest.class);
		Optional<ReverseChargeCapability> capability = testee.getCapability(ReverseChargeCapability.class);

		assertTrue(capability.isPresent());

		capability.get().reverseCharge(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testPICClientInteractionRequestCapabilityInstructions() throws PaymentInstrumentCreationFailedException {
		PICInstructionsRequest request = mock(PICInstructionsRequest.class);
		Optional<PICClientInteractionRequestCapability> capability = testee.getCapability(PICClientInteractionRequestCapability.class);

		assertTrue(capability.isPresent());

		capability.get().getPaymentInstrumentCreationInstructions(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testPICClientInteractionRequestCapabilityInstructionsFields() throws PaymentInstrumentCreationFailedException {
		PICFieldsRequestContextDTO request = mock(PICFieldsRequestContextDTO.class);
		Optional<PICClientInteractionRequestCapability> capability = testee.getCapability(PICClientInteractionRequestCapability.class);

		assertTrue(capability.isPresent());

		capability.get().getPaymentInstrumentCreationInstructionsFields(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}


	@Test
	public void testPICCapabilityInstructions() throws PaymentInstrumentCreationFailedException {
		PaymentInstrumentCreationRequest request = mock(PaymentInstrumentCreationRequest.class);
		Optional<PICCapability> capability = testee.getCapability(PICCapability.class);

		assertTrue(capability.isPresent());

		capability.get().createPaymentInstrument(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}

	@Test
	public void testPICCapabilityInstructionsFields() throws PaymentInstrumentCreationFailedException {
		PICFieldsRequestContextDTO request = mock(PICFieldsRequestContextDTO.class);
		Optional<PICCapability> capability = testee.getCapability(PICCapability.class);

		assertTrue(capability.isPresent());

		capability.get().getPaymentInstrumentCreationFields(request);

		verify(request).setPluginConfigData(PLUGIN_CONFIG_DATA);
	}
}
