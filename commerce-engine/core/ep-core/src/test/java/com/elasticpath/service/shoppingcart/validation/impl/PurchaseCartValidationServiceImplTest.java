package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.context.builders.ShoppingCartValidationContextBuilder;
import com.elasticpath.xpf.context.builders.ShoppingItemValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

@RunWith(MockitoJUnitRunner.class)
public class PurchaseCartValidationServiceImplTest {

	private static final String ERROR_ID = "ERROR_ID";

	private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	private static final String STORE_CODE_KEY = "STORE_CODE_KEY";

	private static final String STORE_CODE = "STORE_CODE";

	private static final String STORE_CODE_2 = "STORE_CODE_2";

	@InjectMocks
	private PurchaseCartValidationServiceImpl validator;

	@Mock
	private XPFExtensionLookup extensionLookup;

	@Mock
	private XPFShoppingCartValidationContext shoppingCartValidationContext;

	@Mock
	private XPFShoppingCart xpfShoppingCart;

	@Mock
	private XPFShoppingItemValidationContext shoppingItemValidationContext;

	@Mock
	private Shopper shopper;

	@Mock
	private Store store;

	@Mock
	private ShoppingItem shoppingItem;

	@Mock
	private ShoppingItem parentShoppingItem;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private ShoppingCartValidationContextBuilder shoppingCartValidationContextBuilder;

	@Mock
	private ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder;

	@Mock
	private BundleMinSelectionRulesShoppingItemValidatorImpl bundleMinSelectionRulesShoppingItemValidator;

	@Mock
	private StructuredErrorMessageConverter structuredErrorMessageConverter;

	@Before
	public void setUp() {
		given(shoppingCartValidationContextBuilder.build(shoppingCart)).willReturn(shoppingCartValidationContext);
		given(shoppingCartValidationContext.getShoppingCart()).willReturn(xpfShoppingCart);
		given(shoppingCart.getRootShoppingItems()).willReturn(Collections.singletonList(shoppingItem));
		given(shoppingItem.getParentItemUid()).willReturn(1L);
		given(shoppingCart.getCartItemById(1L)).willReturn(parentShoppingItem);
		given(shoppingItemValidationContextBuilder.build(xpfShoppingCart, shoppingItem, parentShoppingItem, XPFOperationEnum.NOOP, shopper, store))
				.willReturn(shoppingItemValidationContext);
		given(shoppingItemValidationContextBuilder.getAllContextsStream(shoppingItemValidationContext))
				.willReturn(Stream.of(shoppingItemValidationContext));
	}

	@Test
	public void unsuccessfulValidationTest() {
		// Given
		XPFStructuredErrorMessage xpfErrorMessage1 = new XPFStructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
				STORE_CODE_KEY, STORE_CODE));

		XPFStructuredErrorMessage xpfErrorMessage2 = new XPFStructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
				STORE_CODE_KEY, STORE_CODE_2));

		StructuredErrorMessage errorMessage1 = new StructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
				STORE_CODE_KEY, STORE_CODE));

		StructuredErrorMessage errorMessage2 = new StructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
				STORE_CODE_KEY, STORE_CODE_2));

		given(extensionLookup.getMultipleExtensions(eq(ShoppingCartValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.singletonList(strategy -> ImmutableList.of(xpfErrorMessage1)));

		given(extensionLookup.getMultipleExtensions(eq(ShoppingItemValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.singletonList(bundleMinSelectionRulesShoppingItemValidator));

		given(bundleMinSelectionRulesShoppingItemValidator.validate(shoppingItemValidationContext))
				.willReturn(ImmutableList.of(xpfErrorMessage2));

		given(structuredErrorMessageConverter.convert(xpfErrorMessage1)).willReturn(errorMessage1);
		given(structuredErrorMessageConverter.convert(xpfErrorMessage2)).willReturn(errorMessage2);


		// When
		Collection<StructuredErrorMessage> errorMessages = validator.validate(shoppingCart, shopper, store);

		// Then
		assertThat(errorMessages).containsSequence(errorMessage1, errorMessage2);
	}

	@Test
	public void successfulValidationTest() {
		// Given
		given(extensionLookup.getMultipleExtensions(eq(ShoppingCartValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.singletonList(strategy -> Collections.emptyList()));

		given(extensionLookup.getMultipleExtensions(eq(ShoppingItemValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_CHECKOUT),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.singletonList(bundleMinSelectionRulesShoppingItemValidator));

		given(bundleMinSelectionRulesShoppingItemValidator.validate(shoppingItemValidationContext))
				.willReturn(Collections.emptyList());

		// When
		Collection<StructuredErrorMessage> errorMessages = validator.validate(shoppingCart, shopper, store);

		// Then
		assertThat(errorMessages).isEmpty();
	}
}
