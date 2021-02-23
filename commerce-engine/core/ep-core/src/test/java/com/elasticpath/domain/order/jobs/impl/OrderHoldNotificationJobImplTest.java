/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.order.jobs.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.jobs.OrderHoldNotificationJob;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Unit tests for {@link OrderHoldNotificationJobImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderHoldNotificationJobImplTest {

	private static final String ENABLED_STORE_CODE = "ENABLED_STORE";
	private static final String DISABLED_STORE_CODE = "DISABLED_STORE";

	@Mock
	private OrderService orderService;
	
	@Mock
	private SettingsReader settingsReader;

	@InjectMocks
	private OrderHoldNotificationJobImpl orderHoldNotificationJob;

	@Mock
	private SettingValue settingValueHoldNotificationEnabled, settingValueHoldNotificationDisabled;

	/**
	 * Tests that {@link OrderService} sendOrderHoldNotificationEvent is invoked for an enabled store.
	 */
	@Test
	public void testNotificationEventForEnabledStore() {

		when(settingsReader.getSettingValues(OrderHoldNotificationJob.COMMERCE_STORE_ENABLE_DATA_HOLD_NOTIFICATION))
				.thenReturn(Collections.singleton(settingValueHoldNotificationEnabled));
		when(settingValueHoldNotificationEnabled.getBooleanValue()).thenReturn(Boolean.TRUE);
		when(settingValueHoldNotificationEnabled.getContext()).thenReturn(ENABLED_STORE_CODE);

		orderHoldNotificationJob.publishHoldNotificationEvent();

		verify(orderService).sendOrderHoldNotificationEvent(ENABLED_STORE_CODE);
		verify(orderService).sendOrderHoldNotificationEvent(any());
	}

	/**
	 * Tests that {@link OrderService} sendOrderHoldNotificationEvent is not invoked for a disabled store.
	 */
	@Test
	public void testNotificationEventForDisabledStore() {

		when(settingsReader.getSettingValues(OrderHoldNotificationJob.COMMERCE_STORE_ENABLE_DATA_HOLD_NOTIFICATION))
				.thenReturn(Collections.singleton(settingValueHoldNotificationDisabled));
		when(settingValueHoldNotificationDisabled.getBooleanValue()).thenReturn(Boolean.FALSE);

		orderHoldNotificationJob.publishHoldNotificationEvent();

		verify(orderService, never()).sendOrderHoldNotificationEvent(any());
		verify(settingValueHoldNotificationDisabled, never()).getContext();
	}

	@Test
	public void testNotificationEventForMultipleStores() {

		when(settingsReader.getSettingValues(OrderHoldNotificationJob.COMMERCE_STORE_ENABLE_DATA_HOLD_NOTIFICATION))
				.thenReturn(ImmutableSet.of(settingValueHoldNotificationEnabled, settingValueHoldNotificationDisabled));
		when(settingValueHoldNotificationEnabled.getBooleanValue()).thenReturn(Boolean.TRUE);
		when(settingValueHoldNotificationEnabled.getContext()).thenReturn(ENABLED_STORE_CODE);
		when(settingValueHoldNotificationDisabled.getBooleanValue()).thenReturn(Boolean.FALSE);

		orderHoldNotificationJob.publishHoldNotificationEvent();

		verify(orderService).sendOrderHoldNotificationEvent(ENABLED_STORE_CODE);
		verify(orderService, never()).sendOrderHoldNotificationEvent(DISABLED_STORE_CODE);
		verify(settingValueHoldNotificationDisabled, never()).getContext();
	}
}
