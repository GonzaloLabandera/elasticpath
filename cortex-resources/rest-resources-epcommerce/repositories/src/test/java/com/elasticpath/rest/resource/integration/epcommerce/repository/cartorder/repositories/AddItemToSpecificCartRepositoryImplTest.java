/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SKU_CODE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.definition.carts.LineItemConfigurationEntity;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ModifiersRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link AddItemToSpecificCartRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemToSpecificCartRepositoryImplTest {
	@InjectMocks
	private AddItemToSpecificCartRepositoryImpl<LineItemEntity, AddToSpecificCartFormIdentifier> repository;

	@Mock
	private ModifiersRepository modifiersRepository;

	@Test
	public void testFindOneWithIdentifierData() {


		AddToSpecificCartFormIdentifier identifier = mock(AddToSpecificCartFormIdentifier.class);
		Map<String, String> map = new HashMap<>();
		map.put(ItemRepository.SKU_CODE_KEY, SKU_CODE);
		ItemIdentifier itemIdentifier = ItemIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART)
				.withItemId(CompositeIdentifier.of(map)).build();
		when(identifier.getItem()).thenReturn(itemIdentifier);

		LineItemConfigurationEntity configuration = LineItemConfigurationEntity.builder()
				.addingProperty("a", "b")

				.build();
		when(modifiersRepository.getConfiguration(SKU_CODE)).thenReturn(Single.just(configuration));

		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(entity -> entity.getQuantity().equals(1))
				.assertValue(entity -> entity.getItemId().equals(SKU_CODE));

	}


}