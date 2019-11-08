/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.core.messaging.catalog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

import com.elasticpath.core.messaging.catalog.CatalogEventType.CatalogEventTypeLookup;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;

/**
 * Test class for {@link CatalogEventTypeLookup}.
 */
public class CatalogEventTypeLookupTest {

	private final CatalogEventTypeLookup lookup = new CatalogEventTypeLookup();

	@Test
	public void verifyLookupFindsExistingValue() throws Exception {
		assertThat(CatalogEventType.OPTIONS_UPDATED).isEqualTo(lookup.lookup(CatalogEventType.OPTIONS_UPDATED.getName()));
	}

	@Test
	public void verifyLookupThrowsExceptionWhenNoSuchValue() throws Exception {
		assertThatExceptionOfType(NoSuchEventTypeException.class).isThrownBy(() -> lookup.lookup("noSuchName"))
				.withMessageContaining("No such enum value: class com.elasticpath.core.messaging.catalog.CatalogEventType.noSuchName");
	}
}