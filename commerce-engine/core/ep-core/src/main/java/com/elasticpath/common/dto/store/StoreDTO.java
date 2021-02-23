/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.common.dto.store;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.elasticpath.common.dto.Dto;
import com.elasticpath.common.dto.assembler.CurrencyXmlAdapter;
import com.elasticpath.common.dto.assembler.LocaleXmlAdapter;
import com.elasticpath.domain.store.StoreType;

/**
 * Data Transfer Object for (@link Store}. 
 */
@XmlRootElement(name = StoreDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { })
@SuppressWarnings("PMD.TooManyFields")
public class StoreDTO implements Dto {

	/** Root element name for {@link StoreDTO}. */
	public static final String ROOT_ELEMENT = "store";

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "code", required = true)
	private String code;

	@XmlAttribute(required = true)
	private Boolean enabled;

	@XmlElement(name = "globalization", required = true)
	private StoreGlobalizationDTO globalization;

	@XmlElement
	private String url;

	@XmlElement(required = true)
	private String name;

	@XmlElement(name = "state", required = true)
	private int storeState;

	@XmlElement(name = "type", required = true)
	private StoreType storeType;

	@XmlElement
	private String description;

	@XmlElement(name = "catalog")
	private String catalogCode;

	@XmlElement(name = "display_out_of_stock", required = true)
	private Boolean displayOutOfStock;

	@XmlElement(name = "email_sender_name")
	private String emailSenderName;

	@XmlElement(name = "email_sender_address")
	private String emailSenderAddress;

	@XmlElement(name = "store_admin_email")
	private String storeAdminEmail;

	@XmlElement(name = "credit_card_cvv2_enabled", required = true)
	private Boolean cvv2Enabled;

	@XmlElement(name = "store_full_credit_cards", required = true)
	private Boolean storeFullCreditCards;

	@XmlElementWrapper(name = "locales")
	@XmlElement(name = "locale")
	@XmlJavaTypeAdapter(value = LocaleXmlAdapter.class)
	private List<Locale> supportedLocales = new ArrayList<>();

	@XmlElementWrapper(name = "currencies")
	@XmlElement(name = "currency")
	@XmlJavaTypeAdapter(value = CurrencyXmlAdapter.class)
	private List<Currency> supportedCurrencies = new ArrayList<>();

	@XmlElementWrapper(name = "warehouses")
	@XmlElement(name = "warehouse")
	private List<String> warehouses = new ArrayList<>();

	@XmlElementWrapper(name = "tax_codes")
	@XmlElement(name = "guid")
	private List<String> taxCodeGuids = new ArrayList<>();

	@XmlElementWrapper(name = "tax_jurisdictions")
	@XmlElement(name = "jurisdiction")
	private List<String> taxJurisdictions = new ArrayList<>();

	@XmlElementWrapper(name = "payment_provider_configurations")
	@XmlElement(name = "payment_provider_configuration_guid")
	private List<String> paymentProviderConfigGuids = new ArrayList<>();

	@XmlElementWrapper(name = "credit_card_types")
	@XmlElement(name = "type")
	private List<String> creditCardTypes = new ArrayList<>();

	@XmlElementWrapper(name = "cart_types")
	@XmlElement(name = "cart_type")
	private List<CartTypeDTO> shoppingCartTypes = new ArrayList<>();

	@XmlElement(name = "b2c_authenticated_role", required = true)
	private String authenticatedB2CRole;

	@XmlElement(name = "b2c_single_session_role", required = true)
	private String singleSessionB2CRole;

	public String getCode() {
		return code;
	}

	public void setCode(final String storeCode) {
		this.code = storeCode;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}

	public StoreGlobalizationDTO getGlobalization() {
		return globalization;
	}

	public void setGlobalization(final StoreGlobalizationDTO globalization) {
		this.globalization = globalization;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getStoreState() {
		return storeState;
	}

	public void setStoreState(final int storeState) {
		this.storeState = storeState;
	}

	public StoreType getStoreType() {
		return storeType;
	}

	public void setStoreType(final StoreType storeType) {
		this.storeType = storeType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getCatalogCode() {
		return catalogCode;
	}

	public void setCatalogCode(final String catalogCode) {
		this.catalogCode = catalogCode;
	}

	public Boolean getDisplayOutOfStock() {
		return displayOutOfStock;
	}

	public void setDisplayOutOfStock(final Boolean displayOutOfStock) {
		this.displayOutOfStock = displayOutOfStock;
	}

	public String getEmailSenderName() {
		return emailSenderName;
	}

	public void setEmailSenderName(final String emailSenderName) {
		this.emailSenderName = emailSenderName;
	}

	public String getEmailSenderAddress() {
		return emailSenderAddress;
	}

	public void setEmailSenderAddress(final String emailSenderAddress) {
		this.emailSenderAddress = emailSenderAddress;
	}

	public String getStoreAdminEmail() {
		return storeAdminEmail;
	}

	public void setStoreAdminEmail(final String storeAdminEmail) {
		this.storeAdminEmail = storeAdminEmail;
	}

	public Boolean getCvv2Enabled() {
		return cvv2Enabled;
	}

	public void setCvv2Enabled(final Boolean cvv2Enabled) {
		this.cvv2Enabled = cvv2Enabled;
	}

	public Boolean getStoreFullCreditCards() {
		return storeFullCreditCards;
	}

	public void setStoreFullCreditCards(final Boolean storeFullCreditCards) {
		this.storeFullCreditCards = storeFullCreditCards;
	}

	public List<Locale> getSupportedLocales() {
		return supportedLocales;
	}

	public void setSupportedLocales(final List<Locale> locales) {
		this.supportedLocales = locales;
	}

	public List<Currency> getSupportedCurrencies() {
		return supportedCurrencies;
	}

	public void setSupportedCurrencies(final List<Currency> currencies) {
		this.supportedCurrencies = currencies;
	}

	public List<String> getWarehouses() {
		return warehouses;
	}

	public void setWarehouses(final List<String> warehouses) {
		this.warehouses = warehouses;
	}

	public List<String> getTaxCodeGuids() {
		return taxCodeGuids;
	}

	public void setTaxCodeGuids(final List<String> taxCode) {
		this.taxCodeGuids = taxCode;
	}

	public List<String> getTaxJurisdictions() {
		return taxJurisdictions;
	}

	public void setTaxJurisdictions(final List<String> taxJurisdictions) {
		this.taxJurisdictions = taxJurisdictions;
	}

	public List<String> getPaymentProviderPluginConfigGuids() {
		return paymentProviderConfigGuids;
	}

	public void setPaymentProviderConfigGuids(final List<String> paymentProviderConfigGuids) {
		this.paymentProviderConfigGuids = paymentProviderConfigGuids;
	}

	public List<String> getCreditCardTypes() {
		return creditCardTypes;
	}

	public void setCreditCardTypes(final List<String> creditCardTypes) {
		this.creditCardTypes = creditCardTypes;
	}

	public List<CartTypeDTO> getShoppingCartTypes() {
		return shoppingCartTypes;
	}

	public void setShoppingCartTypes(final List<CartTypeDTO> shoppingCartTypes) {
		this.shoppingCartTypes = shoppingCartTypes;
	}

	public String getAuthenticatedB2CRole() {
		return authenticatedB2CRole;
	}

	public void setAuthenticatedB2CRole(final String authenticatedB2CRole) {
		this.authenticatedB2CRole = authenticatedB2CRole;
	}

	public String getSingleSessionB2CRole() {
		return singleSessionB2CRole;
	}

	public void setSingleSessionB2CRole(final String singleSessionB2CRole) {
		this.singleSessionB2CRole = singleSessionB2CRole;
	}
}
