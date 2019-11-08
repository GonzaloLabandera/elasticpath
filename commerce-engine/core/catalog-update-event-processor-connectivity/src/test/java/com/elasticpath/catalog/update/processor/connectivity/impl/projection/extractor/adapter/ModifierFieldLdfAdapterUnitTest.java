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

import com.elasticpath.domain.modifier.ModifierFieldLdf;


/**
 * Test for {@link ModifierFieldLdfAdapter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierFieldLdfAdapterUnitTest {

	/**
	 * Adapter get ModifierFieldLdf with en and fr values, and return map, that contain Locales en, fr as key,
	 * and en_value, fr_value as value.
	 */
	@Test
	public void ensureThatAdapterReturnAppropriateLocaleByValueMap() {
		final Set<ModifierFieldLdf> set = new HashSet<>();
		final ModifierFieldLdf cartModifierFieldEn = mock(ModifierFieldLdf.class);
		when(cartModifierFieldEn.getLocale()).thenReturn(Locale.ENGLISH.toString());
		when(cartModifierFieldEn.getDisplayName()).thenReturn("Color");

		final ModifierFieldLdf cartModifiedFieldFr = mock(ModifierFieldLdf.class);
		when(cartModifiedFieldFr.getLocale()).thenReturn(Locale.FRANCE.toString());
		when(cartModifiedFieldFr.getDisplayName()).thenReturn("Colour");

		set.add(cartModifierFieldEn);
		set.add(cartModifiedFieldFr);

		final ModifierFieldLdfAdapter adapter = new ModifierFieldLdfAdapter(Locale.getDefault(), set);

		final Map<Locale, String> resultMap = adapter.getCatalogLocaleByValue();

		assertThat(resultMap).contains(entry(Locale.FRANCE, "Colour"));
	}
}
