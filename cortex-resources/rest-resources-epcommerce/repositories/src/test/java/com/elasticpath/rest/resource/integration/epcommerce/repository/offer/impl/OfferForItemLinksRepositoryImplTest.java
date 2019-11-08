package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class OfferForItemLinksRepositoryImplTest {

	private static final String PRODUCT_GUID = "testProductGuid";
	private static final String ITEM_ID = "testItemId";
	private static final String SCOPE = "testScope";

	@InjectMocks
	private OfferForItemLinksRepositoryImpl<ItemIdentifier, OfferIdentifier> target;

	@Mock
	private ItemRepository itemRepository;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ItemIdentifier itemIdentifier;

	@Mock
	private ProductSku productSku;

	@Mock
	private Product product;

	@Before
	public void setUp() {

		given(product.getGuid()).willReturn(PRODUCT_GUID);
		given(productSku.getProduct()).willReturn(product);
		given(itemIdentifier.getScope()).willReturn(StringIdentifier.of(SCOPE));
		given(itemIdentifier.getItemId()).willReturn(CompositeIdentifier.of(ItemRepository.SKU_CODE_KEY, ITEM_ID));

		given(itemRepository.getSkuForItemId(itemIdentifier.getItemId().getValue())).willReturn(Single.just(productSku));

	}

	@Test
	public void testOnLinkTo() {

		target.getElements(itemIdentifier).test()
				.assertNoErrors()
				.assertValue(offerIdentifier -> offerIdentifier.getOfferId().getValue().equals(ImmutableMap.of(PRODUCT_GUID_KEY, PRODUCT_GUID)))
				.assertValue(offerIdentifier -> offerIdentifier.getScope().getValue().equals(SCOPE));

	}

}