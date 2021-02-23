/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.store.CreditCardType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.modifier.ModifierService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.store.WarehouseService;
import com.elasticpath.service.tax.TaxCodeService;
import com.elasticpath.service.tax.TaxJurisdictionService;

/**
 * Assembler for {@link Store} domain object and {@link StoreDTO}.

 * @deprecated
 * Now using commerce-engine\importexport\ep-importexport\src\main\java\com\elasticpath\importexport\common\assembler\store\StoreDtoAssembler
 */
@SuppressWarnings({"PMD.GodClass"})
@Deprecated
public class StoreDtoAssembler extends AbstractDtoAssembler<StoreDTO, Store> {

	private BeanFactory beanFactory;

	private CatalogService catalogService;

	private WarehouseService warehouseService;

	private TaxCodeService taxCodeService;

	private TaxJurisdictionService taxJurisdictionService;

	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	private StoreService storeService;
	private ModifierService modifierService;

	@Override
	public Store getDomainInstance() {
		return beanFactory.getPrototypeBean(ContextIdNames.STORE, Store.class);
	}

	@Override
	public StoreDTO getDtoInstance() {
		return new StoreDTO();
	}

	/**
	 * Factory method for {@link StoreGlobalizationDTO}.
	 *
	 * @return a new, uninitialized {@link StoreGlobalizationDTO}.
	 */
	protected StoreGlobalizationDTO storeGlobalizationDtoFactory() {
		return new StoreGlobalizationDTO();
	}

	/**
	 * Factory method for {@link creditCardType}.
	 *
	 * @return a new, uninitialized {@link creditCardType}.
	 */
	protected CreditCardType creditCardTypeDomainFactory() {
		return beanFactory.getPrototypeBean(ContextIdNames.CREDIT_CARD_TYPE, CreditCardType.class);
	}

	/**
	 * Factory method for {@link CartType}.
	 * @return cart type.
	 */
	protected CartType cartTypeDomainFactory() {
		return beanFactory.getPrototypeBean(ContextIdNames.CART_TYPE, CartType.class);
	}

	@Override
	public void assembleDto(final Store source, final StoreDTO target) {
		target.setCode(source.getCode());
		target.setEnabled(source.isEnabled());

		StoreGlobalizationDTO globalization = storeGlobalizationDtoFactory();

		globalization.setContentEncoding(source.getContentEncoding());
		globalization.setCountry(source.getCountry());
		globalization.setDefaultCurrency(source.getDefaultCurrency());
		globalization.setDefaultLocale(source.getDefaultLocale());
		globalization.setSubCountry(source.getSubCountry());
		globalization.setTimeZone(source.getTimeZone());

		target.setGlobalization(globalization);

		target.setUrl(source.getUrl());
		target.setName(source.getName());
		target.setStoreState(source.getStoreState().getValue());
		target.setStoreType(source.getStoreType());

		target.setDescription(source.getDescription());
		if (source.getCatalog() != null) {
			target.setCatalogCode(source.getCatalog().getCode());
		}

		target.setDisplayOutOfStock(source.isDisplayOutOfStock());

		target.setEmailSenderName(source.getEmailSenderName());
		target.setEmailSenderAddress(source.getEmailSenderAddress());
		target.setStoreAdminEmail(source.getStoreAdminEmailAddress());

		target.setCvv2Enabled(source.isCreditCardCvv2Enabled());
		target.setStoreFullCreditCards(source.isStoreFullCreditCardsEnabled());

		target.getSupportedLocales().addAll(source.getSupportedLocales());
		Collections.sort(target.getSupportedLocales(), (object1, object2) -> object1.toString().compareTo(object2.toString()));

		target.getSupportedCurrencies().addAll(source.getSupportedCurrencies());
		Collections.sort(target.getSupportedCurrencies(), (object1, object2) -> object1.getCurrencyCode().compareTo(object2.getCurrencyCode()));

		for (Warehouse warehouse : source.getWarehouses()) {
			target.getWarehouses().add(warehouse.getCode());
		}
		Collections.sort(target.getWarehouses());

		for (TaxCode taxCode : source.getTaxCodes()) {
			target.getTaxCodeGuids().add(taxCode.getGuid());
		}
		Collections.sort(target.getTaxCodeGuids());

		for (TaxJurisdiction taxJurisdiction : source.getTaxJurisdictions()) {
			target.getTaxJurisdictions().add(taxJurisdiction.getGuid());
		}
		Collections.sort(target.getTaxJurisdictions());

		for (StorePaymentProviderConfig paymentProviderConfig : storePaymentProviderConfigService.findByStore(source)) {
			target.getPaymentProviderPluginConfigGuids().add(paymentProviderConfig.getPaymentProviderConfigGuid());
		}
		Collections.sort(target.getPaymentProviderPluginConfigGuids());

		for (CreditCardType creditCardType : source.getCreditCardTypes()) {
			target.getCreditCardTypes().add(creditCardType.getCreditCardType());
		}
		Collections.sort(target.getCreditCardTypes());

		for (CartType cartType : source.getShoppingCartTypes()) {
			target.getShoppingCartTypes().add(getCartTypeDTO(cartType));
		}
	}

	private CartTypeDTO getCartTypeDTO(final CartType cartType) {
		CartTypeDTO cartTypeDTO = new CartTypeDTO();
		cartTypeDTO.setName(cartType.getName());
		cartTypeDTO.setGuid(cartType.getGuid());

		List<ModifierGroup> modifiers = cartType.getModifiers();
		List<String> modifierGroupCodes = new ArrayList<>(modifiers.size());
		for (ModifierGroup modifierGroup: modifiers) {
			modifierGroupCodes.add(modifierGroup.getCode());
		}
		cartTypeDTO.setModifierGroups(modifierGroupCodes);
		return cartTypeDTO;

	}

	@Override
	public void assembleDomain(final StoreDTO source, final Store target) {
		target.setCode(source.getCode());
		target.setEnabled(source.getEnabled());

		target.setTimeZone(source.getGlobalization().getTimeZone());
		target.setCountry(source.getGlobalization().getCountry());
		target.setSubCountry(source.getGlobalization().getSubCountry());
		target.setContentEncoding(source.getGlobalization().getContentEncoding());
		target.setDefaultCurrency(source.getGlobalization().getDefaultCurrency());
		target.setDefaultLocale(source.getGlobalization().getDefaultLocale());
		target.setUrl(source.getUrl());
		target.setName(source.getName());
		target.setStoreState(StoreState.valueOf(source.getStoreState()));
		target.setStoreType(source.getStoreType());
		target.setDescription(source.getDescription());
		target.setB2CAuthenticatedRole(source.getAuthenticatedB2CRole());
		target.setB2CSingleSessionRole(source.getSingleSessionB2CRole());

		Catalog catalog = catalogService.findByCode(source.getCatalogCode());

		target.setCatalog(catalog);

		target.setDisplayOutOfStock(source.getDisplayOutOfStock());
		target.setEmailSenderName(source.getEmailSenderName());
		target.setEmailSenderAddress(source.getEmailSenderAddress());
		target.setStoreAdminEmailAddress(source.getStoreAdminEmail());

		target.setCreditCardCvv2Enabled(source.getCvv2Enabled());
		target.setStoreFullCreditCardsEnabled(source.getStoreFullCreditCards());

		populateSupportedLocales(source, target);
		populateSupportedCurrencies(source, target);

		populateWarehousesForDomain(source, target);
		populateTaxCodesForDomain(source, target);
		populateTaxJurisdictionsForDomain(source, target);
		populateCreditCardTypesForDomain(source, target);

		populateCartTypesForDomain(source, target);
	}

	private void populateCartTypesForDomain(final StoreDTO source, final Store target) {
		List<CartTypeDTO> shoppingCartTypes = source.getShoppingCartTypes();
		Set<CartType> cartTypes = new HashSet<>(shoppingCartTypes.size());

		for (CartTypeDTO cartTypeDto : shoppingCartTypes) {

			CartType cartType = cartTypeDomainFactory();
			cartType.setName(cartTypeDto.getName());
			cartType.setGuid(cartTypeDto.getGuid());
			cartType.setModifiers(getModifierGroups(cartTypeDto.getModifierGroups()));
			cartTypes.add(cartType);
		}
		// As this is a set, existing cart types are not replaced.
		cartTypes.addAll(target.getShoppingCartTypes());

		target.setShoppingCartTypes(cartTypes);
	}

	private List<ModifierGroup> getModifierGroups(final List<String> modifierGroupCodes) {
		List<ModifierGroup> modifierGroups = new ArrayList<>();
		modifierGroupCodes.forEach(guid -> modifierGroups.add(modifierService.findModifierGroupByCode(guid)));
		return modifierGroups;

	}

	private void populateSupportedCurrencies(final StoreDTO source, final Store target) {

		Set<Currency> currencies = new HashSet<>(source.getSupportedCurrencies());

		// As this is a set, existing currencies are not replaced.
		currencies.addAll(target.getSupportedCurrencies());

		try {
			target.setSupportedCurrencies(currencies);
		} catch (DefaultValueRemovalForbiddenException e) {
			throw new EpSystemException("While setting supported currencies on store " + source.getCode(), e);
		}
	}

	private void populateSupportedLocales(final StoreDTO source, final Store target) {
		Set<Locale> locales = new HashSet<>(source.getSupportedLocales());

		// As this is a set, existing locales are not replaced.
		locales.addAll(target.getSupportedLocales());

		try {
			target.setSupportedLocales(locales);
		} catch (DefaultValueRemovalForbiddenException e) {
			throw new EpSystemException("While setting supported locales on store " + source.getCode(), e);
		}
	}

	private void populateCreditCardTypesForDomain(final StoreDTO source, final Store target) {

		Set<CreditCardType> cards = new HashSet<>();

		for (String cardTypeName : source.getCreditCardTypes()) {

			CreditCardType cardType = creditCardTypeDomainFactory();
			cardType.setCreditCardType(cardTypeName);
			cards.add(cardType);
		}

		// As this is a set, existing card types are not replaced.
		cards.addAll(target.getCreditCardTypes());

		target.setCreditCardTypes(cards);
	}

	private void populateTaxJurisdictionsForDomain(final StoreDTO source, final Store target) {

		for (String guid : source.getTaxJurisdictions()) {

			TaxJurisdiction jurisdiction = taxJurisdictionService.findByGuid(guid);

			if (jurisdiction == null) {
				throw new EpSystemException("Store " + source.getCode() + " references tax jurisdiction guid " + guid
						+ " which is not in the target system. Maybe run an export/import on tax jurisdictions first.");
			}

			// Jurisdiction comparison done with .equals which uses guid
			target.getTaxJurisdictions().remove(jurisdiction);
			target.getTaxJurisdictions().add(jurisdiction);
		}
	}

	private void populateTaxCodesForDomain(final StoreDTO source, final Store target) {
		for (String guid : source.getTaxCodeGuids()) {

			TaxCode taxCode = taxCodeService.findByGuid(guid);

			if (taxCode == null) {
				throw new EpSystemException("Store " + source.getCode() + " references tax code guid " + guid
						+ " which is not in the target system. Maybe run an export/import on tax code (and tax jurisdictions) first.");
			}

			// tax code comparison done with .equals which uses guid
			target.getTaxCodes().remove(taxCode);
			target.getTaxCodes().add(taxCode);
		}
	}

	private void populateWarehousesForDomain(final StoreDTO source, final Store target) {

		for (String code : source.getWarehouses()) {

			Warehouse warehouse = warehouseService.findByCode(code);

			if (warehouse == null) {
				throw new EpSystemException("Store " + source.getCode() + " references warehouse " + code
						+ " which is not in the target system. Maybe run an export/import on warehouses first.");
			}

			// Warehouse comparison done with .equals which uses code
			target.getWarehouses().remove(warehouse);
			target.getWarehouses().add(warehouse);
		}
	}

	/**
	 * @param beanFactory the factory used for creating beans.
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setCatalogService(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	public void setWarehouseService(final WarehouseService warehouseService) {
		this.warehouseService = warehouseService;
	}

	public void setTaxCodeService(final TaxCodeService taxCodeService) {
		this.taxCodeService = taxCodeService;
	}

	public void setTaxJurisdictionService(final TaxJurisdictionService taxJurisdictionService) {
		this.taxJurisdictionService = taxJurisdictionService;
	}

	public void setStorePaymentProviderConfigService(final StorePaymentProviderConfigService storePaymentProviderConfigService) {
		this.storePaymentProviderConfigService = storePaymentProviderConfigService;
	}

	/**
	 * @return the storeService
	 */
	public StoreService getStoreService() {
		return storeService;
	}

	/**
	 * @param storeService the storeService to set
	 */
	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setModifierService(final ModifierService modifierService) {
		this.modifierService = modifierService;
	}

	protected ModifierService getModifierService() {
		return modifierService;
	}
}
