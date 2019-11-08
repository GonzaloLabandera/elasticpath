/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.extractor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.elasticpath.catalog.entity.translation.Translation;

/**
 * Extract list of {@link Translation} from properties of domain object.
 */

public interface CatalogTranslationExtractor {

	/**
	 * Processes domain objects properties to list of Translation.
	 *
	 * @param defaultStoreLocale        default store Locale.
	 * @param supportedByStoreLocales          list of supported store Locales.
	 * @param catalogLocaleExtractor class {@link ProjectionLocaleAdapter}.
	 * @return list of {@link Translation}.
	 */
	List<Translation> getProjectionTranslations(Locale defaultStoreLocale,
												Collection<Locale> supportedByStoreLocales,
												ProjectionLocaleAdapter catalogLocaleExtractor);

}
