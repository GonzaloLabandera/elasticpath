/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.helpers.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;


import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.store.CreditCardType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.TaxJurisdiction;

/**
 * Represents the model of store to be populated in <code>StoreEditor</code>.
 */
@SuppressWarnings({ "PMD.GodClass" })
public class StoreEditorModel {

	private Store store;

	private Set<StoreEditorModel> sharedStoreEditorModels;

	private Set<StoreEditorModel> persistedSharedStoreEditorModels;

	private List<SettingModel> marketingSettings;

	private boolean availableToMarketingOnly;

	private List<SettingModel> systemSettings;

	private List<StoreCustomerAttributeModel> storeCustomerAttributes;

	private List<StorePaymentConfigurationModel> storePaymentConfigurations;

	private String theme;

	private String storeBrowsingSetting;

	private String storeAdvancedSearchSetting;

	private Boolean storeEnableDataPoliciesSetting;

	private StoreEditorModelHelper editorModelHelper;

	/**
	 * Creates the store editor model based on store object.
	 *
	 * @param store the store object
	 */
	StoreEditorModel(final Store store) {
		this.store = store;
	}

	/**
	 * Creates the store editor model based on store object.
	 * Takes into account the necessity to filter marketing settings using metadata
	 *
	 * @param store the Store
	 * @param availableToMarketingOnly true if only marketing related settings with metadata
	 * key = availableToMarketing and metadata value set to true should be loaded
	 */
	StoreEditorModel(final Store store, final boolean availableToMarketingOnly) {
		this.store = store;
		this.availableToMarketingOnly = availableToMarketingOnly;
	}

	/**
	 * Gets the store object.
	 *
	 * @return the store object.
	 */
	public Store getStore() {
		return store;
	}

	/**
	 * Sets the store.
	 *
	 * @param store the store
	 */
	void setStore(final Store store) {
		this.store = store;
		persistedSharedStoreEditorModels = null;
		sharedStoreEditorModels = null;
	}

	/**
	 * @return the Store's Code
	 */
	public String getStoreCode() {
		return getStore().getCode();
	}

	/**
	 * Gets the added shared store models.
	 *
	 * @return the added models
	 */
	Set<StoreEditorModel> getAddedModels() {
		final Set<StoreEditorModel> addedModels = new HashSet<StoreEditorModel>();
		for (StoreEditorModel addedModel : getSharedLoginStoreEntries()) {
			if (!persistedSharedStoreEditorModels.contains(addedModel)) {
				addedModels.add(addedModel);
			}
		}
		return addedModels;
	}

	/**
	 * Gets the removed shared store models.
	 *
	 * @return the removed models
	 */
	Set<StoreEditorModel> getRemovedModels() {
		final Set<StoreEditorModel> removedModels = new HashSet<StoreEditorModel>();
		for (StoreEditorModel removedModel : persistedSharedStoreEditorModels) {
			if (!sharedStoreEditorModels.contains(removedModel)) {
				removedModels.add(removedModel);
			}
		}
		return removedModels;
	}

	/**
	 * Gets set of entries corresponding to shared login stores.
	 *
	 * @return the set of <code>StoreEditorModel</code> entities
	 */
	public Set<StoreEditorModel> getSharedLoginStoreEntries() {
		if (persistedSharedStoreEditorModels == null) {
			sharedStoreEditorModels = new HashSet<StoreEditorModel>();
			Collection<Store> sharedLoginStores = getEditorModelHelper().loadSharedStores(store);
			for (Store sharedLoginStore : sharedLoginStores) {
				sharedStoreEditorModels.add(new StoreEditorModel(sharedLoginStore));
			}
			persistedSharedStoreEditorModels = new HashSet<StoreEditorModel>(sharedStoreEditorModels);
		}
		return sharedStoreEditorModels;
	}

	/**
	 * Gets the marketing settings.
	 *
	 * @return the list of marketing settings
	 */
	public List<SettingModel> getMarketingSettings() {
		return marketingSettings;
	}

	/**
	 * Sets the marketing settings.
	 *
	 * @param marketingSettings the marketing settings
	 */
	void setMarketingSettings(final List<SettingModel> marketingSettings) {
		this.marketingSettings = marketingSettings;
	}

	/**
	 * Gets the store customer attributes.
	 *
	 * @return the list of store customer attributes
	 */
	public List<StoreCustomerAttributeModel> getStoreCustomerAttributes() {
		return storeCustomerAttributes;
	}

	/**
	 * Sets the store customer attributes.
	 *
	 * @param storeCustomerAttributes the list of store customer attributes
	 */
	void setStoreCustomerAttributes(final List<StoreCustomerAttributeModel> storeCustomerAttributes) {
		this.storeCustomerAttributes = storeCustomerAttributes;
	}

	/**
	 * Gets the store payment configurations.
	 *
	 * @return the list of store payment configurations.
	 */
	public List<StorePaymentConfigurationModel> getStorePaymentConfigurations() {
		return storePaymentConfigurations;
	}

	/**
	 * Sets the store payment configurations.
	 *
	 * @param storePaymentConfigurations the list of store payment configurations.
	 */
	public void setStorePaymentConfigurations(final List<StorePaymentConfigurationModel> storePaymentConfigurations) {
		this.storePaymentConfigurations = storePaymentConfigurations;
	}

	/**
	 * Gets the system setting values.
	 *
	 * @return the list of system setting values
	 */
	public List<SettingModel> getSystemSettings() {
		return systemSettings;
	}

	/**
	 * Gets the system settings.
	 *
	 * @param systemSettings the system settings
	 */
	void setSystemSettings(final List<SettingModel> systemSettings) {
		this.systemSettings = systemSettings;
	}

	/**
	 * Sets setting value for store theme setting.
	 *
	 * @param storeThemeSettingValue value to set
	 */
	public void setStoreThemeSetting(final String storeThemeSettingValue) {
		this.theme = storeThemeSettingValue;
	}

	/**
	 * Gets setting value for store theme setting.
	 *
	 * @return store theme setting value
	 */
	public String getStoreThemeSetting() {
		return theme;
	}

	/**
	 * Sets setting value for store browsing setting.
	 *
	 * @param storeBrowsingSetting value to set
	 */
	public void setStoreBrowsingSetting(final String storeBrowsingSetting) {
		this.storeBrowsingSetting = storeBrowsingSetting;
	}

	/**
	 * Gets setting value for store browsing setting.
	 *
	 * @return store browsing setting value
	 */
	public String getStoreBrowsingSetting() {
		return storeBrowsingSetting;
	}

	/**
	 * Sets the value for the advanced search setting.
	 *
	 * @param storeAdvancedSearchSetting value to set
	 */
	public void setStoreAdvancedSearchSetting(final String storeAdvancedSearchSetting) {
		this.storeAdvancedSearchSetting = storeAdvancedSearchSetting;
	}

	/**
	 * Gets the setting value for store advanced search.
	 *
	 * @return setting value for store advanced search
	 */
	public String getStoreAdvancedSearchSetting() {
		return storeAdvancedSearchSetting;
	}

	/**
	 * Sets the value for the store enable data policies setting.
	 *
	 * @param storeEnableDataPoliciesSetting value to set
	 */
	public void setStoreEnableDataPoliciesSettingEnabled(final Boolean storeEnableDataPoliciesSetting) {
		this.storeEnableDataPoliciesSetting = storeEnableDataPoliciesSetting;
	}

	/**
	 * Gets the setting value for store enable data policies.
	 *
	 * @return setting value for store enable data policies.
	 */
	public Boolean isStoreEnableDataPoliciesSettingEnabled() {
		return storeEnableDataPoliciesSetting;
	}


	/**
	 * Sets default locale.
	 *
	 * @param defaultLocale default locale
	 */
	public void setDefaultLocale(final Locale defaultLocale) {
		store.setDefaultLocale(defaultLocale);
	}

	/**
	 * Gets store's default Locale.
	 *
	 * @return default locale of the store
	 */
	public Locale getDefaultLocale() {
		return store.getDefaultLocale();
	}

	/**
	 * Gets UID of store.
	 *
	 * @return store UID
	 */
	public long getUidPk() {
		return store.getUidPk();
	}

	/**
	 * Sets store name.
	 *
	 * @param name store name
	 */
	public void setName(final String name) {
		store.setName(name);
	}

	/**
	 * Gets the name of store.
	 *
	 * @return the name of store
	 */
	public String getName() {
		return store.getName();
	}

	/**
	 * Sets store code.
	 *
	 * @param code store code
	 */
	public void setCode(final String code) {
		store.setCode(code);
	}

	/**
	 * Gets the code of store.
	 *
	 * @return store code
	 */
	public String getCode() {
		return store.getCode();
	}

	/**
	 * Sets store URL.
	 *
	 * @param url store URL
	 */
	public void setUrl(final String url) {
		store.setUrl(url);
	}

	/**
	 * Gets URL of store.
	 *
	 * @return store URL
	 */
	public String getUrl() {
		return store.getUrl();
	}

	/**
	 * Gets store's catalog.
	 *
	 * @return catalog
	 */
	public Catalog getCatalog() {
		return store.getCatalog();
	}

	/**
	 * Sets catalog to the store.
	 *
	 * @param catalog catalog
	 */
	public void setCatalog(final Catalog catalog) {
		store.setCatalog(catalog);
	}

	/**
	 * Sets types of credit cards supported by store.
	 *
	 * @param creditCardTypes set of credit card types
	 */
	public void setCreditCardTypes(final Set<CreditCardType> creditCardTypes) {
		store.setCreditCardTypes(creditCardTypes);
	}

	/**
	 * Gets types of credit cards supported by store.
	 *
	 * @return set of credit card types
	 */
	public Set<CreditCardType> getCreditCardTypes() {
		return store.getCreditCardTypes();
	}

	/**
	 * Checks whether store persistent or not.
	 *
	 * @return true if store is persistent, false otherwise
	 */
	public boolean isPersistent() {
		return store.isPersisted();
	}

	/**
	 * Check if the selected payment configuration can be saved to the store.
	 * @return true if the store payment configuration can be saved, false otherwise.
	 */
	public boolean isStorePaymentConfigurationSavable() {
		return getStore().getStoreState().equals(StoreState.UNDER_CONSTRUCTION) || isPaymentConfigurationSelected();
	}

	/**
	 * Check if the store has any payment configuration selected.
	 * @return true if any payment configuration is selected, false otherwise.
	 */
	public boolean isPaymentConfigurationSelected() {
		return 	getStorePaymentConfigurations().stream().anyMatch(StorePaymentConfigurationModel::isSelected);
	}

	/**
	 * Verifies whether credit card is Cvv2 enabled or not.
	 *
	 * @return true if credit card is Cvv2 enabled, false otherwise
	 */
	public boolean isCreditCardCvv2Enabled() {
		return store.isCreditCardCvv2Enabled();
	}

	/**
	 * Sets whether store has CVV2 verification enabled.
	 *
	 * @param creditCardCvv2Enabled verification flag
	 */
	public void setCreditCardCvv2Enabled(final boolean creditCardCvv2Enabled) {
		store.setCreditCardCvv2Enabled(creditCardCvv2Enabled);
	}

	/**
	 * Checks whether store full credit card enabled.
	 *
	 * @return true if store full credit card enabled, false otherwise
	 */
	public boolean isStoreFullCreditCardsEnabled() {
		return store.isStoreFullCreditCardsEnabled();
	}

	/**
	 * Sets whether the store saves full/unmasked credit card numbers on orders placed.
	 *
	 * @param savingCreditCardWithOrdersEnabled saving credit card policy flag
	 */
	public void setStoreFullCreditCardsEnabled(final boolean savingCreditCardWithOrdersEnabled) {
		store.setStoreFullCreditCardsEnabled(savingCreditCardWithOrdersEnabled);
	}

	/**
	 * Gets warehouse.
	 *
	 * @return warehouse
	 */
	public Warehouse getWarehouse() {
		return store.getWarehouse();
	}

	/**
	 * Sets warehouses.
	 *
	 * @param warehouses warehouses to set
	 */
	public void setWarehouses(final List<Warehouse> warehouses) {
		store.setWarehouses(warehouses);
	}

	/**
	 * Sets store country.
	 *
	 * @param country country
	 */
	public void setCountry(final String country) {
		store.setCountry(country);
	}

	/**
	 * Gets store country.
	 *
	 * @return country
	 */
	public String getCountry() {
		return store.getCountry();
	}

	/**
	 * Sets store sub country.
	 *
	 * @param subCountry sub country
	 */
	public void setSubCountry(final String subCountry) {
		store.setSubCountry(subCountry);
	}

	/**
	 * Gets store sub country.
	 *
	 * @return sub country
	 */
	public String getSubCountry() {
		return store.getSubCountry();
	}

	/**
	 * Sets store time zone.
	 *
	 * @param timeZone time zone
	 */
	public void setTimeZone(final TimeZone timeZone) {
		store.setTimeZone(timeZone);
	}

	/**
	 * Gets store time zone.
	 *
	 * @return time zone
	 */
	public TimeZone getTimeZone() {
		return store.getTimeZone();
	}

	/**
	 * Sets store description.
	 *
	 * @param description store description
	 */

	public void setDescription(final String description) {
		store.setDescription(description);
	}

	/**
	 * Gets store description.
	 *
	 * @return store description
	 */
	public String getDescription() {
		return store.getDescription();
	}

	/**
	 * Sets store tax codes.
	 *
	 * @param taxCodes tax codes
	 */
	public void setTaxCodes(final Set<TaxCode> taxCodes) {
		store.setTaxCodes(taxCodes);
	}

	/**
	 * Sets tax jurisdictions.
	 *
	 * @param taxJurisdictions tax jurisdictions
	 */
	public void setTaxJurisdictions(final Set<TaxJurisdiction> taxJurisdictions) {
		store.setTaxJurisdictions(taxJurisdictions);
	}

	/**
	 * Sets tax jurisdictions.
	 *
	 * @return tax jurisdictions
	 */
	public Set<TaxJurisdiction> getTaxJurisdictions() {
		return store.getTaxJurisdictions();
	}

	/**
	 * Gets store tax codes.
	 *
	 * @return tax codes
	 */
	public Set<TaxCode> getTaxCodes() {
		return store.getTaxCodes();
	}

	/**
	 * Sets default currency.
	 *
	 * @param defaultCurrency default currency
	 */
	public void setDefaultCurrency(final Currency defaultCurrency) {
		store.setDefaultCurrency(defaultCurrency);
	}

	/**
	 * Gets default currency.
	 *
	 * @return default currency
	 */
	public Currency getDefaultCurrency() {
		return store.getDefaultCurrency();
	}

	/**
	 * Sets supported currencies.
	 *
	 * @param supportedCurrencies supported currencies
	 * @throws DefaultValueRemovalForbiddenException if supported currencies do not contain the default
	 */
	public void setSupportedCurrencies(final Collection<Currency> supportedCurrencies) throws DefaultValueRemovalForbiddenException {
		store.setSupportedCurrencies(supportedCurrencies);
	}

	/**
	 * Gets the list of supported currencies.
	 *
	 * @return supported currencies
	 */
	public List<Currency> getSupportedCurrencies() {
		return new ArrayList<Currency>(store.getSupportedCurrencies());
	}

	/**
	 * Sets supported locales.
	 *
	 * @param supportedLocales suppored locales
	 * @throws DefaultValueRemovalForbiddenException if the new locales do not contain the default
	 */
	public void setSupportedLocales(final Collection<Locale> supportedLocales) throws DefaultValueRemovalForbiddenException {
		store.setSupportedLocales(supportedLocales);
	}

	/**
	 * Gets supported locales.
	 *
	 * @return supported locales
	 */
	public Collection<Locale> getSupportedLocales() {
		return store.getSupportedLocales();
	}

	/**
	 * Gets the email sender name.
	 * 
	 * @return email sender name
	 */
	public String getEmailSenderName() {
		return store.getEmailSenderName();
	}

	/**
	 * Sets the email sender name.
	 * 
	 * @param assignedValue the assigned value
	 */
	public void setEmailSenderName(final String assignedValue) {
		store.setEmailSenderName(assignedValue);
	}

	/**
	 * Gets email sender address.
	 * 
	 * @return the email sender address
	 */
	public String getEmailSenderAddress() {
		return store.getEmailSenderAddress();
	}

	/**
	 * Sets the email sender address.
	 * 
	 * @param assignedValue the assigned value
	 */
	public void setEmailSenderAddress(final String assignedValue) {
		store.setEmailSenderAddress(assignedValue);
	}

	/**
	 * Gets the store admin email address.
	 * 
	 * @return the store admin email address.
	 */
	public String getStoreAdminEmailAddress() {
		return store.getStoreAdminEmailAddress();
	}

	/**
	 * Sets the store admin email address.
	 * 
	 * @param assignedValue the assigned value
	 */
	public void setStoreAdminEmailAddress(final String assignedValue) {
		store.setStoreAdminEmailAddress(assignedValue);
	}

	/**
	 * Gets the display out of stock property.
	 * 
	 * @return true if diaply products out of stock and false otherwise
	 */
	public boolean isDisplayOutOfStock() {
		return store.isDisplayOutOfStock();
	}

	/**
	 * Sets the display out of stock property.
	 * 
	 * @param outOfStock true if display products out of stock and false otherwise
	 */
	public void setDisplayOutOfStock(final Boolean outOfStock) {
		store.setDisplayOutOfStock(outOfStock);
	}

	/**
	 * Gets the content encoding.
	 * 
	 * @return the content encoding
	 */
	public String getContentEncoding() {
		return store.getContentEncoding();
	}

	/**
	 * Sets the content encoding.
	 * 
	 * @param assignedValue the assigned value
	 */
	public void setContentEncoding(final String assignedValue) {
		store.setContentEncoding(assignedValue);
	}

	/**
	 * Gets current StoreState.
	 * 
	 * @return StoreState constant instance.
	 */
	public StoreState getStoreState() {
		return store.getStoreState();
	}

	/**
	 * Sets current StoreState.
	 * 
	 * @param storeState the constant StoreState instance.
	 */
	public void setStoreState(final StoreState storeState) {
		store.setStoreState(storeState);
	}

	/**
	 * Gets the B2C authenticated role for store.
	 * @return the B2C authenticated role.
	 */
	public String getB2CAuthenticatedRole() {
		return store.getB2CAuthenticatedRole();
	}

	/**
	 * Sets the B2C authenticated role for store.
	 * @param b2CRole authenticated the B2C role.
	 */
	public void setB2CAuthenticatedRole(final String b2CRole) {
		store.setB2CAuthenticatedRole(b2CRole);
	}

	/**
	 * Gets the B2C single session role for store.
	 * @return the B2C single session role.
	 */
	public String getB2CSingleSessionRole() {
		return store.getB2CSingleSessionRole();
	}

	/**
	 * Sets the B2C single session role for store.
	 * @param b2CRole single session the B2C role.
	 */
	public void setB2CSingleSessionRole(final String b2CRole) {
		store.setB2CSingleSessionRole(b2CRole);
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StoreEditorModel)) {
			return false;
		}
		final Store store = ((StoreEditorModel) other).store;
		if (store.getCode() != null) {
			return store.getCode().equals(this.store.getCode());
		}
		return this.store.getUidPk() == store.getUidPk();
	}

	@Override
	public int hashCode() {
		if (store.getCode() != null) {
			return store.getCode().hashCode();
		}
		return new Long(store.getUidPk()).intValue();
	}

	/**
	 * Checks whether it is required to load all settings with associated availableToMarketing
	 * metadata or only settings having metadata with value equal to true (available to marketing).
	 * 
	 * @return true if only settings with metadata {availableToMarketing, true} should be loaded
	 */
	public boolean loadSettingsAvailableToMarketingOnly() {
		return availableToMarketingOnly;
	}

	/** @return the helper */
	protected StoreEditorModelHelper getEditorModelHelper() {
		if (this.editorModelHelper == null) {
			this.editorModelHelper = StoreEditorModelHelper.createStoreEditorModelHelper();
		}
		return this.editorModelHelper;
	}


}
