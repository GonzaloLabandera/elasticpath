/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Unit tests for {@link AutoSelectableBundleConstituentDelegateValidatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoSelectableBundleConstituentDelegateValidatorImplTest {

	private static final String ERROR_ID = "ERROR_ID";

	private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	private static final String SKU_CODE_KEY = "SKU_CODE_KEY";

	private static final String BRANCH_SKU_CODE_1 = "BRANCH_SKU_CODE_1";

	private static final String BRANCH_SKU_CODE_2 = "BRANCH_SKU_CODE_2";

	private static final String STORE_CODE_KEY = "STORE_CODE_KEY";

	private static final String STORE_CODE = "STORE_CODE";

	@InjectMocks
	private AutoSelectableBundleConstituentDelegateValidatorImpl validator;

	@Mock
	private XPFExtensionLookup extensionLookup;

	@Mock
	private XPFProductSku rootProductSku, branchProductSku1, branchProductSku2;

	@Mock
	private XPFPrice price;

	@Mock
	private XPFStore store;

	@Mock
	private XPFShopper shopper;

	@Test
	public void testTreeWithNestedBundles() {
		// Given
		given(store.getCode()).willReturn(STORE_CODE);
		given(branchProductSku1.getCode()).willReturn(BRANCH_SKU_CODE_1);
		given(branchProductSku2.getCode()).willReturn(BRANCH_SKU_CODE_2);
		given(shopper.getStore()).willReturn(store);

		given(extensionLookup.getMultipleExtensions(eq(ProductSkuValidator.class), any(), any()))
				.willReturn(Collections.singletonList(context -> ImmutableList.of(
						new XPFStructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
								SKU_CODE_KEY, context.getProductSku().getCode(),
								STORE_CODE_KEY, STORE_CODE)))));

		/*
					rootBundle
				/				\
			branchBundle1	branchBundle2
				|				|
			leafBundle1		leafBundle2

		 */

		// When
		Collection<XPFStructuredErrorMessage> errorMessages = validator.validate(givenBundleTreeStructure());

		// Then
		assertThat(errorMessages)
				.extracting(XPFStructuredErrorMessage::getData)
				.extracting(SKU_CODE_KEY, STORE_CODE_KEY)
				.containsExactlyInAnyOrder(
						tuple(BRANCH_SKU_CODE_1, STORE_CODE),
						tuple(BRANCH_SKU_CODE_2, STORE_CODE)
				);
	}

	private XPFProductSkuValidationContext givenBundleTreeStructure() {

		final XPFProductSkuValidationContext branchBundle1 =
				new XPFProductSkuValidationContext(branchProductSku1, rootProductSku, shopper, price, true, Collections.emptyList());
		final XPFProductSkuValidationContext branchBundle2 =
				new XPFProductSkuValidationContext(branchProductSku2, rootProductSku, shopper, price, true, Collections.emptyList());

		return new XPFProductSkuValidationContext(rootProductSku, null, shopper, price, true, Arrays.asList(branchBundle1,
				branchBundle2));
	}
}
