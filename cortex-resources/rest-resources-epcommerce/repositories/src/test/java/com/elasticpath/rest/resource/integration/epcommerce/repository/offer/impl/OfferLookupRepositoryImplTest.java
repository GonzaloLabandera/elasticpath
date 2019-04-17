/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.offers.CodeEntity;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;

/**
 * Test for {@link OfferLookupRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferLookupRepositoryImplTest {

	private static final String VALID_CODE = "validCode";

	private static final String GUID = "guid";

	private static final String INVALID_CODE = "invalidCode";

	private static final String NOT_FOUND = "notFound";

	private static final String SCOPE = "scope";

	private final IdentifierPart<String> scope = StringIdentifier.of(SCOPE);

	@InjectMocks
	private OfferLookupRepositoryImpl<CodeEntity, OfferIdentifier> offerLookupRepository;

	@Mock
	private StoreProductRepository storeProductRepository;

	@Mock
	private StoreProduct product;

	@Test
	public void verifySubmitReturnsOfferIdentifier() {
		CodeEntity codeEntity = CodeEntity.builder()
				.withCode(VALID_CODE)
				.build();

		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, VALID_CODE)).thenReturn(Single.just(product));
		when(product.getGuid()).thenReturn(GUID);

		offerLookupRepository.submit(codeEntity, scope)
				.test()
				.assertNoErrors()
				.assertValue(identifier -> identifier.getIdentifier().getOfferId().getValue().get(PRODUCT_GUID_KEY).equals(GUID));
	}

	@Test
	public void verifySubmitReturnsNotFoundWhenProductDoesNotExist() {
		CodeEntity codeEntity = CodeEntity.builder()
				.withCode(INVALID_CODE)
				.build();

		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, INVALID_CODE))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		offerLookupRepository.submit(codeEntity, scope)
				.test()
				.assertError(ErrorCheckPredicate.createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));

	}
}
