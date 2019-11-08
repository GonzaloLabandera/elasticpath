/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;


/**
 * Test for {@link ModifierFieldOptionLdfAdapter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierFieldOptionLdfAdapterUnitTest {

	/**
	 * Adapter get ModifierFieldOptionLdf with en and fr values, and return map, that contain Locales en, fr as key,
	 * and en_value, fr_value as value.
	 */
	@Test
	public void ensureThatAdapterReturnAppropriateLocaleByValueMap() {
		final Set<ModifierFieldOptionLdf> set = new HashSet<>();
		final ModifierFieldOptionLdf cartModifierFieldOptionEn = mock(ModifierFieldOptionLdf.class);
		when(cartModifierFieldOptionEn.getLocale()).thenReturn(Locale.ENGLISH.toString());
		when(cartModifierFieldOptionEn.getDisplayName()).thenReturn("Color");

		final ModifierFieldOptionLdf cartModifiedFieldOptionFr = mock(ModifierFieldOptionLdf.class);
		when(cartModifiedFieldOptionFr.getLocale()).thenReturn(Locale.FRANCE.toString());
		when(cartModifiedFieldOptionFr.getDisplayName()).thenReturn("Colour");

		set.add(cartModifierFieldOptionEn);
		set.add(cartModifiedFieldOptionFr);

		final ModifierFieldOptionLdfAdapter adapter = new ModifierFieldOptionLdfAdapter(Locale.getDefault(), set);

		final Map<Locale, String> resultMap = adapter.getCatalogLocaleByValue();

		assertThat(resultMap).contains(entry(Locale.FRANCE, "Colour"));
	}
}
