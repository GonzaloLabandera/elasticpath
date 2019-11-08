/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * Test for {@link CategoryCatalogCapabilityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryCatalogCapabilityRepositoryImplTest {
	private static final String CODE = "code";
	@Mock
	private CatalogService catalogService;

	@Test
	public void testThatCatalogCapabilitySaveOrUpdateCategoryAndReturnsTrue() {
		final CategoryWriterRepository repository = new CategoryCatalogCapabilityRepositoryImpl(catalogService);
		final Category createdProjection = mock(Category.class);
		when(catalogService.saveOrUpdate(createdProjection)).thenReturn(true);

		final boolean isSaved = repository.write(createdProjection);

		verify(catalogService).saveOrUpdate(createdProjection);
		assertThat(isSaved).isTrue();
	}

	@Test
	public void testThatCatalogCapabilityDeleteProjection() {
		final CategoryWriterRepository repository = new CategoryCatalogCapabilityRepositoryImpl(catalogService);
		doNothing().when(catalogService).delete(CATEGORY_IDENTITY_TYPE, CODE);

		repository.delete(CODE);

		verify(catalogService).delete(CATEGORY_IDENTITY_TYPE, CODE);
	}

	@Test
	public void testThatCatalogCapabilityWriteAllProjections() {
		final CategoryWriterRepository repository = new CategoryCatalogCapabilityRepositoryImpl(catalogService);
		List<Category> projections = Arrays.asList(mock(Category.class), mock(Category.class));
		when(catalogService.saveOrUpdateAll(projections)).thenReturn(projections);

		repository.writeAll(projections);

		verify(catalogService).saveOrUpdateAll(projections);
	}
}
