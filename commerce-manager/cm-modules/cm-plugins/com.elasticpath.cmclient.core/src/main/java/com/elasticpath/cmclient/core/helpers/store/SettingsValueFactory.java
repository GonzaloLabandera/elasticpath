/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.helpers.store;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.settings.SettingsService;
import com.elasticpath.settings.domain.SettingDefinition;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Implementation of <code>SettingsFactory</code> for settings value.
 */
public class SettingsValueFactory implements SettingsFactory {
	
	private final SettingsService settingsService;
	
	private final StoreEditorModel editorModel;
	
	/**
	 * Creates the settings value factory.
	 * 
	 * @param editorModel the editor model
	 */
	public SettingsValueFactory(final StoreEditorModel editorModel) {
		this.editorModel = editorModel;
		settingsService = BeanLocator.getSingletonBean(ContextIdNames.SETTINGS_SERVICE, SettingsService.class);
	}

	@Override
	public SettingModel createSetting(final String path, final String storeCode) {
		final SettingValue settingValue = settingsService.getSettingValue(path, storeCode);
		final SettingDefinition settingDefinition = settingsService.getSettingDefinition(path);
		return new SettingValueModel(editorModel, settingValue, settingDefinition);
	}
}
