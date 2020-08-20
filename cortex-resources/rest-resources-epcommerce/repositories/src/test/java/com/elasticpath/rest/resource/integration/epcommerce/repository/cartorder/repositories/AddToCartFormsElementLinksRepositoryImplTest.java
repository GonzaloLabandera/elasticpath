/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.ACCOUNT_SHARED_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.AccountSharedIdSubjectAttribute;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Test for {@link AddToCartFormsElementLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddToCartFormsElementLinksRepositoryImplTest {

	private static final String TEST_VALUE = "test";

	@InjectMocks
	private AddToCartFormsElementLinksRepositoryImpl<AddToCartFormsIdentifier, AddToSpecificCartFormIdentifier> repository;
	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;


	@Before
	public void setUp() {
		repository.setResourceOperationContext(resourceOperationContext);
		repository.setShoppingCartRepository(shoppingCartRepository);
	}

	@Test
	public void testFindElements() {

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		Subject subject = mock(Subject.class);
		when(subject.getAttributes()).thenReturn(Collections.singletonList(new AccountSharedIdSubjectAttribute("key", ACCOUNT_SHARED_ID)));
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(shoppingCartRepository.findAllCarts(USER_ID, ACCOUNT_SHARED_ID, SCOPE)).thenReturn(Observable.just(CART_GUID));

		Observable<AddToSpecificCartFormIdentifier> result = repository.getElements(AddToCartFormsIdentifier.builder()
				.withItem(ItemIdentifier.builder()
						.withScope(SCOPE_IDENTIFIER_PART)
						.withItemId(CompositeIdentifier.of(TEST_VALUE, TEST_VALUE))
						.build())
				.withCarts(CartsIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART).build()).build());

		result.test()
				.assertNoErrors()
				.assertValueCount(1);
	}

}