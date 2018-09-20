/**
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.DefaultCartIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories.DefaultCartIdentifierRepositoryImpl;

/**
 * Test for {@link DefaultCartIdentifierRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class DefaultCartIdentifierRepositoryImplTest {

    private static final String SCOPE = "SCOPE";
    private static final String GUID = "GUID";

    @Mock
    private ShoppingCartRepositoryImpl shoppingCartRepository;

    @Mock
    private ShoppingCart shoppingCart;

    @InjectMocks
    private DefaultCartIdentifierRepositoryImpl<DefaultCartIdentifier, CartIdentifier> defaultCartRepository;

    @Before
    public void initialize() {
        when(shoppingCart.getGuid()).thenReturn(GUID);
        when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
        defaultCartRepository.setShoppingCartRepository(shoppingCartRepository);
    }

    @Test
    public void resolveProducesCartIdentifier() {

        DefaultCartIdentifier defaultCartIdentifier = DefaultCartIdentifier.builder()
                .withScope(StringIdentifier.of(SCOPE))
                .build();

        defaultCartRepository.resolve(defaultCartIdentifier)
                .test()
                .assertNoErrors()
                .assertValue(cartIdentifier -> cartIdentifier.getCartId().getValue().equals(GUID))
                .assertValue(cartIdentifier -> cartIdentifier.getScope().getValue().equals(SCOPE));
    }

}
