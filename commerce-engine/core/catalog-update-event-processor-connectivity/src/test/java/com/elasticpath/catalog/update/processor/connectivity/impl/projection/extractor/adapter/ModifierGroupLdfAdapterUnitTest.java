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

import com.elasticpath.domain.modifier.ModifierGroupLdf;

/**
 * Test for {@link ModifierGroupLdfAdapter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupLdfAdapterUnitTest {

	/**
	 * Adapter get ModifierGroupLdf with en and fr values, and return map, that contain Locales en, fr as key,
	 * and en_value, fr_value as value.
	 */
	@Test
	public void ensureThatAdapterReturnAppropriateLocaleByValueMap() {
		final Set<ModifierGroupLdf> set = new HashSet<>();
		final ModifierGroupLdf cartModifierGroupEn = mock(ModifierGroupLdf.class);
		when(cartModifierGroupEn.getLocale()).thenReturn(Locale.ENGLISH.toString());
		when(cartModifierGroupEn.getDisplayName()).thenReturn("Color");

		final ModifierGroupLdf cartModifiedGroupFr = mock(ModifierGroupLdf.class);
		when(cartModifiedGroupFr.getLocale()).thenReturn(Locale.FRANCE.toString());
		when(cartModifiedGroupFr.getDisplayName()).thenReturn("Colour");

		set.add(cartModifierGroupEn);
		set.add(cartModifiedGroupFr);

		final ModifierGroupLdfAdapter adapter = new ModifierGroupLdfAdapter(Locale.getDefault(), set);

		final Map<Locale, String> resultMap = adapter.getCatalogLocaleByValue();

		assertThat(resultMap).contains(entry(Locale.FRANCE, "Colour"));
	}
}
