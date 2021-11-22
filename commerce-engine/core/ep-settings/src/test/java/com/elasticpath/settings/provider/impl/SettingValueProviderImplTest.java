/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.settings.provider.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.settings.MalformedSettingValueException;
import com.elasticpath.settings.provider.converter.SettingValueTypeConverter;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.connectivity.context.XPFSettingValueRetrievalContext;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;
import com.elasticpath.xpf.connectivity.extensionpoint.SettingValueRetrievalStrategy;

/**
 * Test class for {@link com.elasticpath.settings.provider.SettingValueProvider}.
 */
public class SettingValueProviderImplTest {

	private static final String PATH = "COMMERCE/ITEST/SETTINGS/mockSetting";
	private static final String CONTEXT = "CONTEXT";

	private SettingValueProviderImpl<?> settingValueProvider;

	private final XPFExtensionLookup extensionLookup = mock(XPFExtensionLookup.class);

	private final XPFSettingValue settingValue = mock(XPFSettingValue.class);

	private final SettingValueRetrievalStrategy settingValueRetrievalStrategy = mock(SettingValueRetrievalStrategy.class);

	@SuppressWarnings("unchecked")
	private final SettingValueTypeConverter typeConverter = mock(SettingValueTypeConverter.class);

	@Before
	public void setUp() {
		settingValueProvider = new SettingValueProviderImpl<>();
		settingValueProvider.setXpfExtensionLookup(extensionLookup);

		settingValueProvider.setPath(PATH);


		when(extensionLookup.getMultipleExtensions(any(), any(), any())).thenReturn(Collections.singletonList(settingValueRetrievalStrategy));
	}

	@Test
	public void verifyGetWithNoSettingsThrowsEpServiceException() {
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(CONTEXT);
		when(settingValueRetrievalStrategy.getSettingValue(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> settingValueProvider.get(CONTEXT))
				.isInstanceOf(EpServiceException.class)
		.hasMessage("No setting value retrieval strategies were unable to find a value for path and context: 'COMMERCE/ITEST/SETTINGS/mockSetting',"
				+ " '" + CONTEXT + "'");
	}

	@Test
	public void verifyGetWithNoSettingsReaderThrowsIllegalStateException() {
		settingValueProvider.setXpfExtensionLookup(null);

		assertThatThrownBy(() -> settingValueProvider.get())
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void verifyGetWithNoSettingValueTypeConverterThrowsIllegalStateException() {
		settingValueProvider.setSettingValueTypeConverter(null);

		assertThatThrownBy(() -> settingValueProvider.get())
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void verifyGetWithNoPathThrowsIllegalStateException() {
		settingValueProvider.setPath(null);

		assertThatThrownBy(() -> settingValueProvider.get())
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void verifyGetWithPathAndNoContextCallsSettingsReader() {
		final String expected = UUID.randomUUID().toString();
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(CONTEXT);
		when(settingValueRetrievalStrategy.getSettingValue(any())).thenReturn(Optional.of(settingValue));
		when(typeConverter.convert(settingValue)).thenReturn(expected);

		assertThat(settingValueProvider.get())
				.as("Expected the SettingValue's value to be returned by the provider")
				.isEqualTo(expected);
	}

	@Test
	public void verifyGetWithPathAndContextCallsSettingsReader() {
		final String expected = UUID.randomUUID().toString();
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(CONTEXT);

		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, CONTEXT))).thenReturn(Optional.of(settingValue));
		when(typeConverter.convert(settingValue)).thenReturn(expected);

		assertThat(settingValueProvider.get())
				.as("Expected the SettingValue's value to be returned by the provider")
				.isEqualTo(expected);
	}

	@Test
	public void verifySettingValueConverterReturnsCorrectValueType() {
		final BigDecimal expected = new BigDecimal("123.4");
		final XPFSettingValue bigDecimalSettingValue = mock(XPFSettingValue.class, "bigDecimalTypeSettingValue");

		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(CONTEXT);
		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, CONTEXT)))
				.thenReturn(Optional.of(bigDecimalSettingValue));
		when(typeConverter.convert(bigDecimalSettingValue)).thenReturn(expected);

		assertThat(settingValueProvider.get())
				.as("Expected the correct value type  to be returned by the provider")
				.isEqualTo(expected);
	}

	@Test
	public void verifyGetPropagatesUnexpectedSettingValueTypeExceptionsFromConverter() {
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(CONTEXT);
		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, CONTEXT))).thenReturn(Optional.of(settingValue));
		doThrow(MalformedSettingValueException.class).when(typeConverter).convert(settingValue);

		assertThatThrownBy(() -> settingValueProvider.get())
				.isInstanceOf(MalformedSettingValueException.class);
	}

	@Test
	public void verifyGetWithContextUsedWhenContextNotConfigured() {
		final String expected = UUID.randomUUID().toString();
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		settingValueProvider.setContext(null);
		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, CONTEXT))).thenReturn(Optional.of(settingValue));
		when(typeConverter.convert(settingValue)).thenReturn(expected);

		assertThat(settingValueProvider.get(CONTEXT))
				.as("Expected the context value input to be used to determine the setting value")
				.isEqualTo(expected);
	}

	@Test
	public void verifyGetWithContextOverridesSetContext() {
		final String overrideContext = "CONTEXT2";
		final String expected = UUID.randomUUID().toString();

		settingValueProvider.setContext(CONTEXT);
		settingValueProvider.setSettingValueTypeConverter(typeConverter);
		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, overrideContext)))
				.thenReturn(Optional.of(settingValue));
		when(typeConverter.convert(settingValue)).thenReturn(expected);

		assertThat(settingValueProvider.get(overrideContext))
				.as("Expected the context value input to be used to determine the setting value")
				.isEqualTo(expected);
	}

	@Test
	public void verifySystemPropertyOverrideValueIsApplied() {
		final String overrideKey = "overrideKey";
		final String overrideValue = "overrideValue";

		System.setProperty(overrideKey, overrideValue);
		settingValueProvider.setSystemPropertyOverrideKey(overrideKey);
		settingValueProvider.setSettingValueTypeConverter(typeConverter);

		when(settingValueRetrievalStrategy.getSettingValue(new XPFSettingValueRetrievalContext(PATH, CONTEXT))).thenReturn(Optional.of(settingValue));
		XPFSettingValue overrideSettingValue = new XPFSettingValue(overrideValue, settingValue.getValueType());
		when(typeConverter.convert(overrideSettingValue)).thenReturn(overrideValue);

		assertThat(settingValueProvider.get(CONTEXT))
				.as("Expected the system override value to be returned")
				.isEqualTo(overrideValue);
	}
}
