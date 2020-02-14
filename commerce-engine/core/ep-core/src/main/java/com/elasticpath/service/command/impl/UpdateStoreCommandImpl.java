/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.command.impl;

import java.util.Map;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.command.CommandResult;
import com.elasticpath.service.command.UpdateStoreCommand;
import com.elasticpath.service.command.UpdateStoreCommandResult;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.settings.SettingsService;
import com.elasticpath.settings.domain.SettingValue;

/**
 * Updates store using core services.
 */
public class UpdateStoreCommandImpl implements UpdateStoreCommand {

	private static final long serialVersionUID = 1L;

	private Store store;

	private Map<String, String> settingValueMap;

	private Map<String, StoreCustomerAttribute> storeCustomerAttributes;

	private UpdateStoreCommandResult updateStoreCommandResult;

	/**
	 * Updates the store and setting values.
	 *
	 * @return command result
	 */
	@Override
	public CommandResult execute() {
		updateStoreCommandResult.setStore(getStoreService().saveOrUpdate(store));
		final String storeCode = store.getCode();
		for (final Map.Entry<String, String> settingValueEntry : settingValueMap.entrySet()) {
			final SettingValue settingValue = getSettingsService().getSettingValue(settingValueEntry.getKey(), storeCode);
			settingValue.setValue(settingValueEntry.getValue());
			getSettingsService().updateSettingValue(settingValue);
		}

		updateStoreCustomerAttributes();

		return updateStoreCommandResult;
	}

	/**
	 * Updates store customer attributes.
	 */
	protected void updateStoreCustomerAttributes() {
		getStoreCustomerAttributeService().updateAll(store.getCode(), storeCustomerAttributes);
	}

	@Override
	public void setStore(final Store store) {
		this.store = store;
	}

	@Override
	public void setSettingValues(final Map<String, String> settingValueMap) {
		this.settingValueMap = settingValueMap;
	}

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private StoreService getStoreService() {
		return ElasticPathImpl.getInstance().getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);
	}

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private SettingsService getSettingsService() {
		return ElasticPathImpl.getInstance().getSingletonBean(ContextIdNames.SETTINGS_SERVICE, SettingsService.class);
	}

	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private StoreCustomerAttributeService getStoreCustomerAttributeService() {
		return ElasticPathImpl.getInstance().getSingletonBean(ContextIdNames.STORE_CUSTOMER_ATTRIBUTE_SERVICE, StoreCustomerAttributeService.class);
	}

	public void setUpdateStoreCommandResult(final UpdateStoreCommandResult updateStoreCommandResult) {
		this.updateStoreCommandResult = updateStoreCommandResult;
	}

	@Override
	public void setStoreCustomerAttributes(final Map<String, StoreCustomerAttribute> storeCustomerAttributes) {
		this.storeCustomerAttributes = storeCustomerAttributes;
	}
}
