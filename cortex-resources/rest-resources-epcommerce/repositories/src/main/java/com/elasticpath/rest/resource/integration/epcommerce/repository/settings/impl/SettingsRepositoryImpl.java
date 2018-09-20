/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.settings.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.settings.SettingsRepository;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;


/**
 * Implementation of SettingsRepository.
 */
@Singleton
@Named("settingsRepository")
public class SettingsRepositoryImpl implements SettingsRepository {

	private final SettingsReader settingsReader;

	/**
	 * Instantiates a new settings repository.
	 *
	 * @param settingsReader the settings lookup
	 */
	@Inject
	public SettingsRepositoryImpl(
			@Named("cachedSettingsReader")
			final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}


	@Override
	@CacheResult
	public Single<String> getStringSettingValue(final String path, final String context) {
		return getSettingValue(path, context)
				.map(SettingValue::getValue);
	}

	@SuppressWarnings("PMD.AvoidCatchingThrowable")
	private Single<SettingValue> getSettingValue(final String path, final String context) {
		try {
			SettingValue settingValue = settingsReader.getSettingValue(path, context);
			if (settingValue == null) {
				return Single.error(ResourceOperationFailure.notFound(
						"Setting value for path [" + path + "] and context [" + context + "] is not " + "found"));
			} else {
				return Single.just(settingValue);
			}
		} catch (Exception e) {
			return Single.error(ResourceOperationFailure.serverError("Unable to resolve setting value"));
		}
	}
}
