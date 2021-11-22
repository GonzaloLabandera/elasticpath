/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;

/**
 * Populator for locale subject attribute.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR, priority = 1020)
public class LocaleTagSetPopulator extends XPFExtensionPointImpl implements HttpRequestTagSetPopulator {
	private static final Logger LOG = LoggerFactory.getLogger(LocaleTagSetPopulator.class);
	private static final String TRAIT_HEADER = "LOCALE";
	private static final String SUBJECT_ATTRIBUTE_KEY = "LOCALE";

	private final Map<Locale, Collection<Locale>> localeLookups = new ConcurrentHashMap<>();

	@Override
	public Map<String, String> collectTagValues(final XPFHttpTagSetContext context) {
		final XPFStore store = context.getStore();
		if (store == null) {
			LOG.error("Unable to retrieve store record for {}", store);
			return Collections.emptyMap();
		}

		Locale locale;
		// NOTE: User trait key is case-sensitive
		final String localeAttribute = context.getUserTraitValues().get(TRAIT_HEADER);
		if (StringUtils.isNotEmpty(localeAttribute)) {
			locale = findBestSupportedLocale(Stream.of(Locale.forLanguageTag(localeAttribute)), store);
			// request.getLocales() always returns a value even if no header is present.
		} else if (StringUtils.isNotEmpty(context.getHttpRequest()
				.getHeader(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(Locale.getDefault())))) {
			Collection<Locale> requestLocales = Collections.list(context.getHttpRequest().getLocales());
			locale = findBestSupportedLocale(requestLocales.stream(), store);
		} else {
			locale = store.getDefaultLocale();
		}

		return Collections.singletonMap(SUBJECT_ATTRIBUTE_KEY, locale.toLanguageTag());
	}

	/**
	 * Test locales against Store support to find the best Locale to use. Defaults to store default
	 * if nothing lines up.
	 *
	 * @param locales the locales from the request
	 * @param store   the store
	 * @return an appropriate locale
	 */
	Locale findBestSupportedLocale(final Stream<Locale> locales, final XPFStore store) {
		final Collection<Locale> supportedLocales = store.getSupportedLocales();
		return locales
				.flatMap(locale -> localeLookups
						.computeIfAbsent(locale, LocaleUtils::localeLookupList)
						.stream())
				.filter(supportedLocales::contains)
				.findFirst()
				.orElse(store.getDefaultLocale());
	}

}
