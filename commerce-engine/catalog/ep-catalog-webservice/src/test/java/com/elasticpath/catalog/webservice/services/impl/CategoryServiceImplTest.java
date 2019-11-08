/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.webservice.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.CatalogReaderCapability;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link CategoryServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceImplTest {

	private static final String STORE_CODE = "store";
	private static final String CODE = "CODE";
	private static final String START_AFTER_STRING = "startAfterString";
	private static final String CHILD_CODE = "CHILD_CODE";

	@Mock
	private CategoryReaderCapability reader;

	@Mock
	private TimeService timeService;

	private CategoryServiceImpl categoryService;

	@Before
	public void setUp() {
		final CatalogProjectionPluginProvider catalogProjectionPluginProvider = mock(CatalogProjectionPluginProvider.class,
				Mockito.RETURNS_DEEP_STUBS);
		final Optional<CatalogReaderCapability> categoryReaderCapability = Optional.of(reader);

		when(catalogProjectionPluginProvider.getCatalogProjectionPlugin().getReaderCapability(any())).thenReturn(categoryReaderCapability);

		categoryService = new CategoryServiceImpl(catalogProjectionPluginProvider, timeService);
	}

	@Test
	public void testThatCategoryReaderCapabilityIsCalledInGetMethod() {
		when(reader.get(STORE_CODE, CODE)).thenReturn(Optional.empty());

		categoryService.get(STORE_CODE, CODE);

		verify(reader).get(STORE_CODE, CODE);
	}

	@Test
	public void testThatCategoryReaderCapabilityCallFindAllMethodIfLimitIsCorrect() {
		final int testLimit = 2;
		final FindAllResponse<Category> testResponse = new FindAllResponseImpl<>(
				new PaginationResponseImpl(testLimit, START_AFTER_STRING, false), ZonedDateTime.now(), Collections.emptyList());

		when(reader.findAll(any(), any(), any())).thenReturn(testResponse);

		categoryService.getAllCategories(STORE_CODE, String.valueOf(testLimit), START_AFTER_STRING, null, null);

		verify(reader).findAll(any(), any(), any());
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatCategoryServiceThrowExceptionIfParameterIsLessThanZero() {
		final String invalidLimit = "-1";

		categoryService.getAllCategories(STORE_CODE, invalidLimit, START_AFTER_STRING, null, null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatCategoryServiceThrowExceptionIfParameterIsLetter() {
		final String invalidLimit = "x";

		categoryService.getAllCategories(STORE_CODE, invalidLimit, START_AFTER_STRING, null, null);
	}

	@Test
	public void testThatCategoryServiceConvertsValidDate() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
		categoryService.getAllCategories(STORE_CODE, "2", START_AFTER_STRING, "2018-01-01T14:47:00+00:00", null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatCategoryServiceThrowExceptionIfInvalidDate() {
		categoryService.getAllCategories(STORE_CODE, "2", START_AFTER_STRING, "A2018-01-01T14:47:00+00:00", null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatModifiedSinceOffsetCannotBeSpecifiedIfModifiedSinceNotPresent() {
		categoryService.getAllCategories(STORE_CODE, "2", START_AFTER_STRING, null, "5");
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatModifiedSinceMustBeInThePast() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
		categoryService.getAllCategories(STORE_CODE, "2", START_AFTER_STRING, "3018-01-01T14:47:00+00:00", "5");
	}

	@Test
	public void getChildrenShouldReturnsChildrenListFoundByCodes() {
		final Category child = mockCategory(CHILD_CODE);
		final Category category = mockCategoryWithChildren(Collections.singletonList(CHILD_CODE), CODE);

		when(reader.get(STORE_CODE, CODE)).thenReturn(Optional.of(category));
		when(reader.findAllWithCodes(anyString(), anyList())).thenReturn(Collections.singletonList(child));

		final List<Category> children = categoryService.getChildren(STORE_CODE, CODE);

		assertThat(children).containsOnly(child);
	}

	@Test
	public void getChildrenShouldReturnsChildrenWithCorrectOrder() {
		final Category child = mockCategory("2");
		final Category child1 = mockCategory("1");
		final Category category = mockCategoryWithChildren(Arrays.asList(child.getIdentity().getCode(),
				child1.getIdentity().getCode()), CODE);

		when(reader.get(STORE_CODE, CODE)).thenReturn(Optional.of(category));
		when(reader.findAllWithCodes(anyString(), anyList())).thenReturn(Arrays.asList(child, child1));

		final List<Category> children = categoryService.getChildren(STORE_CODE, CODE);

		assertThat(children.get(0)).isEqualTo(child);
		assertThat(children.get(1)).isEqualTo(child1);
	}

	private Category mockCategory(final String code) {
		return mockCategoryWithChildren(Collections.emptyList(), code);
	}

	private Category mockCategoryWithChildren(final List<String> children, final String code) {
		final ProjectionProperties projectionProperties = new ProjectionProperties(code, STORE_CODE, ZonedDateTime.now(), false);
		final List<Property> categorySpecificProperties = Collections.emptyList();

		final CategoryProperties categoryProperties = new CategoryProperties(projectionProperties, categorySpecificProperties);
		final Object extensions = new Object();
		final List<CategoryTranslation> categoryTranslations = Collections.emptyList();
		final AvailabilityRules availabilityRules = new AvailabilityRules(null, null);
		final List<String> path = Collections.emptyList();
		final String parent = null;

		return new Category(categoryProperties, extensions, categoryTranslations, children, availabilityRules, path, parent);
	}

}
