/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product.option.impl;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * Tests for {@link SkuOptionRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionEntityRepositoryImplTest {

	private static final String CANNOT_FIND_OPTION_VALUE_MESSAGE = "Cannot find option value.";
	private static final String SKU_OPTION_NAME_KEY = "SKU_OPTION_NAME_KEY";
	private static final String SKU_OPTION_VALUE_KEY = "SKU_OPTION_VALUE_KEY";

	@Mock
	private SkuOptionService skuOptionService;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	private SkuOptionRepositoryImpl skuOptionRepository;

	@Mock
	SkuOptionValue skuOptionValue;

	@Before
	public void initialize() {
		skuOptionRepository = new SkuOptionRepositoryImpl(skuOptionService, reactiveAdapter);
	}

	/**
	 * Test find sku option by value key with a successful result.
	 */
	@Test
	public void testFindSkuOptionByValueKey() {
		when(skuOptionService.findOptionValueByOptionAndValueKeys(SKU_OPTION_NAME_KEY, SKU_OPTION_VALUE_KEY)).thenReturn(skuOptionValue);

		skuOptionRepository.findSkuOptionValueByKey(SKU_OPTION_NAME_KEY, SKU_OPTION_VALUE_KEY)
				.test()
				.assertNoErrors()
				.assertValue(skuOptionValue);
	}

	/**
	 * Test find sku option by value key when no sku option value found.
	 */
	@Test
	public void testFindSkuOptionByValueKeyWhenNoSkuOptionValueFound() {
		when(skuOptionService.findOptionValueByOptionAndValueKeys(SKU_OPTION_NAME_KEY, SKU_OPTION_VALUE_KEY)).thenReturn(null);

		skuOptionRepository.findSkuOptionValueByKey(SKU_OPTION_NAME_KEY, SKU_OPTION_VALUE_KEY)
				.test()
				.assertError(ResourceOperationFailure.notFound(CANNOT_FIND_OPTION_VALUE_MESSAGE))
				.assertNoValues();
	}
}
