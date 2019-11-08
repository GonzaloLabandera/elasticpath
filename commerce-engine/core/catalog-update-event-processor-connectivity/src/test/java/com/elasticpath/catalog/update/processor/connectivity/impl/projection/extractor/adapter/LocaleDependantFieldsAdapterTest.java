/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;

import org.junit.Test;

import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.ObjectWithLocaleDependantFields;

/**
 * Test for {@link LocaleDependantFieldsAdapter}.
 */
public class LocaleDependantFieldsAdapterTest {

	/**
	 * Adapter get LocaleDependantFieldsAdapter with en value, and return map, that contain Locale en,
	 * and en_value.
	 */
	@Test
	public void ensureThatAdapterReturnAppropriateLocaleByValueMap() {
		final String displayName = "displayName";
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		when(localeDependantFields.getDisplayName()).thenReturn(displayName);
		final ObjectWithLocaleDependantFields fields = mock(ObjectWithLocaleDependantFields.class);
		when(fields.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);

		final LocaleDependantFieldsAdapter localeDependantFieldsAdapter = new LocaleDependantFieldsAdapter(Locale.ENGLISH, fields,
				Collections.singleton(Locale.ENGLISH));

		assertThat(localeDependantFieldsAdapter.getCatalogLocaleByValue()).contains(entry(Locale.ENGLISH, displayName));
	}

	@Test
	public void ensureThatAdapterDoesNotThrowExceptionWhenLocaleIsAbsent() {
		final String displayName = "displayName";
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		when(localeDependantFields.getDisplayName()).thenReturn(displayName);
		final ObjectWithLocaleDependantFields fields = mock(ObjectWithLocaleDependantFields.class);
		when(fields.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);

		assertThatCode(() -> new LocaleDependantFieldsAdapter(Locale.ENGLISH, fields, Collections.singleton(Locale.CHINESE)))
				.doesNotThrowAnyException();
	}
}
