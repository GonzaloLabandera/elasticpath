/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.impl;

import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.rest.identity.attribute.CurrencySubjectAttribute;
import com.elasticpath.rest.identity.attribute.KeyValueSubjectAttribute;
import com.elasticpath.rest.identity.attribute.LocaleSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.attribute.UserTraitSubjectAttribute;
import com.elasticpath.rest.relos.rs.subject.attribute.SubjectAttributeProvider;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.util.math.NumberUtil;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;
import com.elasticpath.xpf.context.builders.HttpTagSetContextBuilder;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;

/**
 * Loads subject attributes.
 */
@Component
public class SubjectAttributeProviderImpl implements SubjectAttributeProvider {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectAttributeProviderImpl.class);

	@Reference
	private XPFExtensionLookup extensionLookup;

	@Reference
	private HttpTagSetContextBuilder httpTagSetContextBuilder;

	@Override
	public Collection<SubjectAttribute> getSubjectAttributes(final HttpServletRequest request) {

		Map<String, SubjectAttribute> attributes = new HashMap<>();
		final XPFHttpTagSetContext httpTagSetContext = httpTagSetContextBuilder.build(request);

		for (HttpRequestTagSetPopulator populator : extensionLookup.getMultipleExtensions(HttpRequestTagSetPopulator.class,
				XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR, new XPFExtensionSelectorAny())) {
			final Map<String, String> result = populator.collectTagValues(httpTagSetContext);

			for (Map.Entry<String, String> tagValue : result.entrySet()) {

				// This logic, combined with the service ranking for
				// SubjectAttributeLookupStrategies in
				// the SubjectAttributeLookupRegistry, enforces the business rules
				// around precedence of SubjectAttributes.
				//
				// Since SubjectAttributes are unique by key and the load order has precedence,
				// we filter by key here so that we only have the highest precedence attributes
				// in the collection.
				SubjectAttribute added = attributes.putIfAbsent(tagValue.getKey(), createAttribute(tagValue));

				if (LOG.isDebugEnabled() && added != null) {
					LOG.debug("Adding SubjectAttribute {} from {}", tagValue, populator);
				}
			}
		}

		return attributes.values();
	}

	private SubjectAttribute createAttribute(final Map.Entry<String, String> tagSet) {
		final SubjectAttribute attribute;
		switch (tagSet.getKey()) {
			case "CURRENCY":
				attribute = new CurrencySubjectAttribute(tagSet.getKey(), Currency.getInstance(tagSet.getValue()));
				break;
			case "LOCALE":
				attribute = new LocaleSubjectAttribute(tagSet.getKey(), Locale.forLanguageTag(tagSet.getValue()));
				break;
			case "DATA_POLICY_SEGMENTS":
				attribute = new KeyValueSubjectAttribute(tagSet.getKey(), tagSet.getValue(), tagSet.getKey());
				break;
			default:
				attribute = new UserTraitSubjectAttribute(tagSet.getKey(), tagSet.getValue());
				break;
		}
		return attribute;
	}

	/**
	 * Construct a map of traits from the request headers.
	 *
	 * @param request request to find headers in
	 * @return traits map
	 */
	Map<String, String> getUserTraitsFromRequest(final HttpServletRequest request) {
		//Create a map of the existing traits
		Collection<String> traitsFromRequest = SubjectHeadersUtil.getUserTraitsFromRequest(request);
		Map<String, String> traits = new HashMap<>(traitsFromRequest.size());

		for (String trait : traitsFromRequest) {
			addAttribute(traits, trait);
		}

		return traits;
	}

	private void addAttribute(final Map<String, String> traits, final String trait) {
		int delimPos = trait.indexOf(KeyValueSubjectAttribute.KEY_VALUE_DELIM);

		if (NumberUtil.isPositive(delimPos)) {
			String key = trait.substring(0, delimPos).trim();
			String value = trait.substring(delimPos + 1).trim();

			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				//Don't replace a previously-defined attribute
				traits.putIfAbsent(key, value);
			}
		}
	}

	public void setExtensionLookup(final XPFExtensionLookup extensionLookup) {
		this.extensionLookup = extensionLookup;
	}

	public void setHttpTagSetContextBuilder(final HttpTagSetContextBuilder httpTagSetContextBuilder) {
		this.httpTagSetContextBuilder = httpTagSetContextBuilder;
	}
}
