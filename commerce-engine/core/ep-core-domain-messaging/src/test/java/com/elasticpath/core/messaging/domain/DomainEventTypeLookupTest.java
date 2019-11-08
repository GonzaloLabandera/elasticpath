/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.core.messaging.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

import com.elasticpath.core.messaging.domain.DomainEventType.DomainEventTypeLookup;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;

/**
 * Test class for {@link DomainEventTypeLookup}.
 */
public class DomainEventTypeLookupTest {

	private final DomainEventTypeLookup lookup = new DomainEventTypeLookup();

	@Test
	public void verifyLookupFindsExistingValue() throws Exception {
		assertThat(DomainEventType.SKU_OPTION_CREATED).isEqualTo(lookup.lookup(DomainEventType.SKU_OPTION_CREATED.getName()));
	}

	@Test
	public void verifyLookupThrowsExceptionWhenNoSuchValue() throws Exception {
		assertThatExceptionOfType(NoSuchEventTypeException.class).isThrownBy(() -> lookup.lookup("noSuchName"))
				.withMessageContaining("No such enum value: class com.elasticpath.core.messaging.domain.DomainEventType.noSuchName");
	}
}