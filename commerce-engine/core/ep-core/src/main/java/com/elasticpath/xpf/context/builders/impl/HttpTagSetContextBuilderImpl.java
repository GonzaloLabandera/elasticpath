/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.context.builders.HttpTagSetContextBuilder;
import com.elasticpath.xpf.converters.StoreConverter;
import com.elasticpath.xpf.converters.XPFConverterUtil;

/**
 * Implementation of {@code com.elasticpath.xpf.context.builders.HttpTagSetContextBuilder}.
 */
public class HttpTagSetContextBuilderImpl implements HttpTagSetContextBuilder {

	private static final char KEY_VALUE_DELIM = '=';

	private StoreConverter storeConverter;
	private StoreService storeService;

	@Override
	public XPFHttpTagSetContext build(final HttpServletRequest servletRequest) {

		final XPFStore xpfStore = Optional.ofNullable(findUserScope(servletRequest))
				.map(storeService::findStoreWithCode)
				.map(storeConverter::convert)
				.orElse(null);

		return new XPFHttpTagSetContext(servletRequest, xpfStore, getUserTraitValues(servletRequest));
	}

	private String findUserScope(final HttpServletRequest httpRequest) {
		final Collection<String> scopes = XPFConverterUtil.getUserScopesFromRequest(httpRequest);

		return scopes.stream().findFirst().orElse(null);
	}

	private Map<String, String> getUserTraitValues(final HttpServletRequest request) {
		Collection<String> traitsFromRequest = XPFConverterUtil.getUserTraitsFromRequest(request);
		Map<String, String> traits = new HashMap<>(traitsFromRequest.size());

		for (String trait : traitsFromRequest) {
			addAttribute(traits, trait);
		}

		return traits;
	}

	private void addAttribute(final Map<String, String> traits, final String trait) {
		int delimPos = trait.indexOf(KEY_VALUE_DELIM);

		if (delimPos > 0) {
			String key = trait.substring(0, delimPos).trim();
			String value = trait.substring(delimPos + 1).trim();

			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				//Don't replace a previously-defined attribute
				traits.putIfAbsent(key, value);
			}
		}
	}

	public void setStoreConverter(final StoreConverter storeConverter) {
		this.storeConverter = storeConverter;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}
}
