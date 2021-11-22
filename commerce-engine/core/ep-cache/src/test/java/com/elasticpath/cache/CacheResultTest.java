/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.elasticpath.base.cache.CacheResult;

public class CacheResultTest {
	@Test
	public void testThatIsPresentReturnsTrueForNonNullObject() {
		assertTrue(CacheResult.create("some object").isPresent());
	}

	@Test
	public void testThatIsPresentReturnsTrueForNullObject() {
		assertTrue(CacheResult.create(null).isPresent());
	}

	@Test
	public void testThatIsPresentReturnsFalseForEmptyObject() {
		assertFalse(CacheResult.notPresent().isPresent());
	}

	@Test(expected = NoSuchElementException.class)
	public void testThatGetThrowExceptionWhenTryGetEmptyObject() {
		CacheResult.notPresent().get();
	}

	@Test
	public void testThatGetReturnsTheSameObjectWithPassedToCreateMethod() {
		final String cacheValue = "some object";
		assertEquals(cacheValue, CacheResult.create(cacheValue).get());
	}
}