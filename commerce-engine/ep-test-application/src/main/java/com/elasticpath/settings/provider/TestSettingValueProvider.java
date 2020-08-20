/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.settings.provider;

import com.elasticpath.settings.provider.impl.SettingValueProviderImpl;

/**
 * Stubbed setting value provider.
 */
public final class TestSettingValueProvider extends SettingValueProviderImpl<Integer> {
	private int settingValue;

	/**
	 * Custom constructor for setting desired setting value.
	 *
	 * @param settingValue the desired setting value to return.
	 */
	public TestSettingValueProvider(final int settingValue) {
		this.settingValue = settingValue;
	}

	@Override
	public Integer get() {
		return settingValue;
	}

	@Override
	public Integer get(final String context) {
		return settingValue;
	}
}
