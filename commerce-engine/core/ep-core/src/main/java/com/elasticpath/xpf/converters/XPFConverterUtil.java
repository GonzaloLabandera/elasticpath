/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.misc.LocalizedAttributeKeyUtils;
import com.elasticpath.domain.misc.SupportedLocale;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.context.builders.impl.ProductSkuValidationContextBuilderImpl;

/**
 * Util methods used by XPF converters.
 */
public class XPFConverterUtil {
	/**
	 * User trait header. User traits are things like ip address, browser type, and gender.
	 */
	public static final String USER_TRAIT = "x-ep-user-trait";

	/**
	 * User traits header. User traits are things like ip address, browser type, and gender.
	 */
	public static final String USER_TRAITS = "x-ep-user-traits";

	/**
	 * USER SCOPE.
	 */
	public static final String USER_SCOPE = "x-ep-user-scope";

	/**
	 * USER SCOPES.
	 */
	public static final String USER_SCOPES = "x-ep-user-scopes";


	private AttributeValueConverter xpfAttributeValueConverter;

	private ProductSkuValidationContextBuilderImpl xpfProductSkuValidationContextBuilder;

	private StoreService storeService;

	/**
	 * Util method to convert a collection of attribute values to
	 * a map of locales to a map of attribute keys to XPFAttributeValue objects.
	 *
	 * @param attributeValues attribute value collection to be converted
	 * @param store the store
	 * @return map of locales to map of attribute key to attribute value
	 */
	public Map<Locale, Map<String, XPFAttributeValue>> convertToXpfAttributeValues(final Collection<AttributeValue> attributeValues,
																				   final Optional<Store> store) {
		if (attributeValues == null || attributeValues.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Pair<AttributeValue, Locale>> attributeValueLocalePairs = attributeValues.stream()
				.map(attributeValue -> new Pair<>(attributeValue,
						LocalizedAttributeKeyUtils.getLocaleFromLocalizedKeyName(attributeValue.getLocalizedAttributeKey())))
				.collect(Collectors.toList());

		Map<Locale, Map<String, XPFAttributeValue>> localizedAttributeValueMap = attributeValueLocalePairs.stream()
				.filter(pair -> pair.getSecond() != null)
				.collect(Collectors.groupingBy(Pair::getSecond,
						Collectors.toMap(pair -> pair.getFirst().getAttribute().getKey().toUpperCase(pair.getSecond()),
								pair -> xpfAttributeValueConverter.convert(new StoreDomainContext<AttributeValue>(pair.getFirst(), store)))));

		Map<String, XPFAttributeValue> nonLocalizedAttributeValueMap = attributeValueLocalePairs.stream()
				.filter(pair -> pair.getSecond() == null)
				.map(Pair::getFirst)
				.collect(Collectors.toMap(
						attributeValue -> attributeValue.getAttribute().getKey().toUpperCase(),
						attributeValue -> xpfAttributeValueConverter.convert(new StoreDomainContext<AttributeValue>(attributeValue, store))
				));

		if (!nonLocalizedAttributeValueMap.isEmpty()) {
			localizedAttributeValueMap.put(null, nonLocalizedAttributeValueMap);
		}

		return localizedAttributeValueMap;
	}

	/**
	 * Util method to convert attribute values that match the current locale or are locale independent to XPFAttributeValue.
	 *
	 * @param attributeValueMap attribute value map
	 * @param store the store
	 * @return corresponding set containing XPFAttributeValue
	 */
	public Map<Locale, Map<String, XPFAttributeValue>> convertToXpfAttributeValues(final Map<String, AttributeValue> attributeValueMap,
																				   final Optional<Store> store) {
		return convertToXpfAttributeValues(attributeValueMap == null ? null : attributeValueMap.values(), store);
	}

	/**
	 * Util method to convert customer profile map values that match the current locale or are locale independent to XPFAttributeValue.
	 *
	 * @param customerProfileValueMap customerProfileValue map whose value collection to be converted
	 * @param store the store
	 * @return corresponding set containing XPFAttributeValue
	 */
	public Map<Locale, Map<String, XPFAttributeValue>> convertCustomerProfilesToXpfAttributeValues(
			final Map<String, CustomerProfileValue> customerProfileValueMap, final Optional<Store> store) {
		if (customerProfileValueMap == null) {
			return Collections.emptyMap();
		}

		// Upcast CustomerProfileValue to AttributeValue and convert
		return convertToXpfAttributeValues(new ArrayList<>(customerProfileValueMap.values()), store);
	}

	/**
	 * Get Locales for Store.
	 *
	 * @param storeOptional the store optional
	 * @return the locales
	 */
	public Set<Locale> getLocalesForStore(final Optional<Store> storeOptional) {
		return storeOptional.map(store -> store.getSupportedLocales().stream().collect(Collectors.toSet()))
				.orElseGet(this::findAllEnabledStoreLocales);
	}

	/**
	 * Returns a set of unique locales across all enabled, open stores.
	 *
	 * @return set of unique locales across all enabled, open stores
	 */
	private Set<Locale> findAllEnabledStoreLocales() {
		return storeService.findAllEnabledStoreLocales().stream()
				.map(SupportedLocale::getLocale)
				.collect(Collectors.toSet());
	}

	/**
	 * Convert children of domain ProductSkuValidationContext into XPFProductSkuValidationContext type.
	 *
	 * @param productSku the product sku associated with domain ProductSkuValidationContext.
	 * @param shopper the shopper associated with domain ProductSkuValidationContext.
	 * @param store the store associated with domain ProductSkuValidationContext.
	 *
	 * @return Children of domain ProductSkuValidationContext converted to XPFProductSkuValidationContext type.
	 */
	public List<XPFProductSkuValidationContext> getProductConstituentsAsValidationContexts(
			final ProductSku productSku,
			final Shopper shopper,
			final Store store) {
		return getFilteredConstituents(productSku.getProduct()).stream()
				.map(item -> xpfProductSkuValidationContextBuilder.build(item.getProductSku(), productSku, shopper, store))
				.collect(Collectors.toList());
	}

	private Collection<ConstituentItem> getFilteredConstituents(final Product product) {
		if (product instanceof ProductBundle) {
			ProductBundle bundle = (ProductBundle) product;

			return bundle.getConstituents().stream()
					.filter(bundle::isConstituentAutoSelectable)
					.map(BundleConstituent::getConstituent)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public void setXpfAttributeValueConverter(final AttributeValueConverter xpfAttributeValueConverter) {
		this.xpfAttributeValueConverter = xpfAttributeValueConverter;
	}

	public void setXpfProductSkuValidationContextBuilder(final ProductSkuValidationContextBuilderImpl xpfProductSkuValidationContextBuilder) {
		this.xpfProductSkuValidationContextBuilder = xpfProductSkuValidationContextBuilder;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	/**
	 * Parse the {@link XPFConverterUtil#USER_TRAIT} and {@link XPFConverterUtil#USER_TRAITS} parameters.
	 * The parameter will be decoded. TRAIT will be parsed before TRAITS.
	 *
	 * @param request The request.
	 * @return The parsed parameters, or null if no user SCOPES header was supplied.
	 */
	public static Collection<String> getUserTraitsFromRequest(final HttpServletRequest request) {
		return mergeRequestHeaders(request, USER_TRAIT, USER_TRAITS);
	}

	/**
	 * Parse the {@link XPFConverterUtil#USER_SCOPE} and {@link XPFConverterUtil#USER_SCOPES} parameters.
	 * The parameter will be decoded. SCOPE will be parsed before SCOPES.
	 *
	 * @param request The request.
	 * @return The parsed parameters.
	 */
	public static Collection<String> getUserScopesFromRequest(final HttpServletRequest request) {
		return mergeRequestHeaders(request, USER_SCOPE, USER_SCOPES);
	}

	private static Collection<String> mergeRequestHeaders(final HttpServletRequest request, final String firstHeader, final String secondHeader) {
		return ImmutableList.copyOf(
				Iterables.concat(
						getHeadersFromRequest(request, firstHeader),
						getHeadersFromRequest(request, secondHeader)));
	}

	/**
	 * Retrieve all header values for a given name from a request. This method considers comma-separated values
	 * as separate values. It also understands that headers can be declared multiple times in
	 * a request and knows how to enumerate over them. Values can be quoted with the &quot; character.
	 * <p/>
	 * If no headers are found, an empty iterable is returned. Header values are trimmed such than empty values are omitted
	 * from the result.
	 *
	 * @param request the <code>HttpServletRequest</code> to get headers from
	 * @param header  the name of header
	 * @return a collection of header values.
	 */
	public static Iterable<String> getHeadersFromRequest(final HttpServletRequest request, final String header) {
		Enumeration<String> headersEnum = request.getHeaders(header);
		if (headersEnum == null) {
			return ImmutableList.of();
		}

		List<String> values = ImmutableList.copyOf(Iterators.forEnumeration(headersEnum));

		//short-circuit common cases
		if (values.isEmpty()) {
			return values;
		}

		if (values.size() == 1) {
			return tokenize(values.get(0));
		}

		return values.stream()
				.flatMap((String value) -> tokenize(value).stream())
				.collect(ImmutableList.toImmutableList());
	}


	private static List<String> tokenize(final String value) {
		if (value.indexOf(',') < 0) {
			//trim off quote chars, whitespace as the CSV parser does
			String trimmed = StringUtils.strip(value, " \t\"");
			return StringUtils.isEmpty(trimmed)
					? ImmutableList.of()
					: ImmutableList.of(trimmed);
		} else {
			return tokensFor(value);
		}
	}

	private static List<String> tokensFor(final String value) {
		return StringTokenizer
				.getCSVInstance(value)
				.setIgnoreEmptyTokens(true)
				.getTokenList();
	}
}
