/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.changeset.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.pricing.BaseAmountDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.domain.objectgroup.impl.BusinessObjectDescriptorImpl;
import com.elasticpath.service.changeset.ObjectGuidResolver;

/**
 * Tests for {@link com.elasticpath.service.changeset.impl.BusinessObjectResolverImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectResolverImplTest {

	private static final String OBJECT_TYPE_CATEGORY = "Category";
	private static final String OBJECT_TYPE_PRODUCT = "Product";
	private static final String OBJECT_TYPE_CATALOG = "Catalog";
	private static final String OBJECT_TYPE_BASE_AMOUNT = "Base Amount";
	private static final String OBJECT_TYPE_PRODUCT_BUNDLE = "Product Bundle";

	@Mock
	private BeanFactory beanFactory;

	/**
	 * A map of interface to object type mappings.
	 */
	private Map<Class <?>, String> objectTypes;

	@Mock
	private ObjectGuidResolver defaultObjectGuidResolver;

	@Mock
	private Map<String, ObjectGuidResolver> objectGuidResolvers;

	@Mock
	private ObjectGuidResolver categoryGuidResolver;

	private BusinessObjectResolverImpl businessObjectResolver;

	/**
	 *
	 * @throws java.lang.Exception on error
	 */
	@Before
	public void setUp() throws Exception {

		objectTypes = new LinkedHashMap<>();
		objectTypes.put(Product.class, OBJECT_TYPE_PRODUCT);
		objectTypes.put(Catalog.class, OBJECT_TYPE_CATALOG);
		objectTypes.put(Category.class, OBJECT_TYPE_CATEGORY);
		objectTypes.put(ProductBundle.class, OBJECT_TYPE_PRODUCT_BUNDLE);

		Map<Class<?>, String> secondaryObjectTypes = new HashMap<>();
		secondaryObjectTypes.put(BaseAmountDTO.class, OBJECT_TYPE_BASE_AMOUNT);

		objectGuidResolvers = new HashMap<>();
		objectGuidResolvers.put(Category.class.getName(), categoryGuidResolver);
		objectGuidResolvers.put(BaseAmountDTO.class.getName(), new BaseAmountDTOGuidResolver());

		businessObjectResolver = new BusinessObjectResolverImpl();
		businessObjectResolver.setObjectTypes(objectTypes);
		businessObjectResolver.setSecondaryObjectTypes(secondaryObjectTypes);
		businessObjectResolver.setBeanFactory(beanFactory);
		businessObjectResolver.setDefaultObjectGuidResolver(defaultObjectGuidResolver);
		businessObjectResolver.setObjectGuidResolvers(objectGuidResolvers);
	}

	/**
	 * Tests that the policy is able to resolve entities to object type by using the object type (interface) map.
	 *
	 * <p>For example, com.elasticpath.domain.catalog.Product resolves to object type "product".
	 * <br>and com.elasticpath.domain.catalog.Catalog resolves to "catalog".
	 */
	@Test
	public void testResolveObjectDescriptor() {

		final BusinessObjectDescriptor objectDescriptor = new BusinessObjectDescriptorImpl();

		final Product product1 = new ProductImpl();
		product1.setGuid("123");

		final Catalog catalog1 = new CatalogImpl();
		catalog1.setGuid("456");

		final ProductBundle productBundle1 = new ProductBundleImpl();
		productBundle1.setGuid("789");

		final int expectTime = 3;
		when(beanFactory.getBean(ContextIdNames.BUSINESS_OBJECT_DESCRIPTOR)).thenReturn(objectDescriptor);
		when(defaultObjectGuidResolver.resolveGuid(product1)).thenReturn(product1.getGuid());
		when(defaultObjectGuidResolver.resolveGuid(catalog1)).thenReturn(catalog1.getGuid());
		when(defaultObjectGuidResolver.resolveGuid(productBundle1)).thenReturn(productBundle1.getGuid());

		// test product resolution
		BusinessObjectDescriptor result = businessObjectResolver.resolveObjectDescriptor(product1);
		String objectType = result.getObjectType();
		String objectId = result.getObjectIdentifier();

		assertThat(objectType)
			.as("Incorrect object type resolved.")
			.isEqualTo(OBJECT_TYPE_PRODUCT);
		assertThat(objectId)
			.as("Incorrect object ID resolved.")
			.isEqualTo(product1.getGuid());

		// test catalog resolution
		result = businessObjectResolver.resolveObjectDescriptor(catalog1);
		objectType = result.getObjectType();
		objectId = result.getObjectIdentifier();

		assertThat(objectType)
			.as("Incorrect object type resolved.")
			.isEqualTo(OBJECT_TYPE_CATALOG);
		assertThat(objectId)
			.as("Incorrect object ID resolved.")
			.isEqualTo(catalog1.getGuid());

		// test product bundle resolution
		result = businessObjectResolver.resolveObjectDescriptor(productBundle1);
		objectType = result.getObjectType();
		objectId = result.getObjectIdentifier();

		assertThat(objectType)
			.as("Incorrect object type resolved.")
			.isEqualTo(OBJECT_TYPE_PRODUCT_BUNDLE);
		assertThat(objectId)
			.as("Incorrect object ID resolved.")
			.isEqualTo(productBundle1.getGuid());

		verify(beanFactory, times(expectTime)).getBean(ContextIdNames.BUSINESS_OBJECT_DESCRIPTOR);
		verify(defaultObjectGuidResolver).resolveGuid(product1);
		verify(defaultObjectGuidResolver).resolveGuid(catalog1);
		verify(defaultObjectGuidResolver).resolveGuid(productBundle1);
	}

	/**
	 * Test the function to get sub class first set.
	 */
	@Test
	public void testGetSubClassFirstSet() {
		LinkedHashSet<Class<?>> subClassLastSet = new LinkedHashSet<>();

		subClassLastSet.add(Product.class);
		subClassLastSet.add(ProductBundle.class);

		SortedSet<Class<?>> subClassFirstSet = businessObjectResolver.getSubClassFirstSet(subClassLastSet);

		assertThat(subClassFirstSet)
			.as("sub class should appear first")
			.startsWith(ProductBundle.class);
	}

	/**
	 * Test that we can resolve an object descriptor for a dto.
	 */
	@Test
	public void testResolveObjectDescriptorDto() {
		String guid = "BA123";
		final BaseAmountDTO baDTO = new BaseAmountDTO();
		baDTO.setGuid(guid);
		final BusinessObjectDescriptor objectDescriptor = new BusinessObjectDescriptorImpl();

		when(beanFactory.getBean(ContextIdNames.BUSINESS_OBJECT_DESCRIPTOR)).thenReturn(objectDescriptor);

		BusinessObjectDescriptor bod = businessObjectResolver.resolveObjectDescriptor(baDTO);

		verify(beanFactory).getBean(ContextIdNames.BUSINESS_OBJECT_DESCRIPTOR);
		assertThat(bod.getObjectIdentifier())
			.as("Expected guid to be same")
			.isEqualTo(guid);
		assertThat(bod.getObjectType())
			.as("Expected type for Base Amount")
			.isEqualTo(OBJECT_TYPE_BASE_AMOUNT);
	}

	/**
	 * Tests that if an object type can't be resolved, a null BusinessObjectDescriptor is returned.
	 */
	@Test
	public void testResolveObjectDescriptorNoMapping() {

		// remove all object type mappings so nothing resolves correctly
		businessObjectResolver.getObjectTypes().clear();
		businessObjectResolver.getSecondaryObjectTypes().clear();

		final Product product1 = new ProductImpl();
		product1.setGuid("123");

		// test product resolution
		BusinessObjectDescriptor result = businessObjectResolver.resolveObjectDescriptor(product1);
		assertThat(result)
			.as("The resolved BusinessObjectDescriptor should be null.")
			.isNull();
	}

	/**
	 * Only objects of type Entity can be resolved, since the GUID is required.
	 */
	@Test
	public void testResolveObjectDescriptorMustBeEntity() {

		// try to resolve an object that is not of type Entity and expect a null
		BusinessObjectDescriptor result = businessObjectResolver.resolveObjectDescriptor(Integer.valueOf(1));
		assertThat(result)
			.as("Business object must be of type com.elasticpath.domain.Entity and should not be resolvable.")
			.isNull();
	}
	/**
	 * Tests to resolve object guid. 
	 */
	@Test
	public void testResolveObjectGuid() {
		final CategoryImpl category = new CategoryImpl();
		category.setGuid("category1");

		when(categoryGuidResolver.isSupportedObject(category)).thenReturn(true);
		when(categoryGuidResolver.resolveGuid(category)).thenReturn("categoryGuid|catalogGuid");

		assertThat(businessObjectResolver.resolveObjectGuid(category)).isNotNull();
		verify(categoryGuidResolver).isSupportedObject(category);
		verify(categoryGuidResolver).resolveGuid(category);
	}

	/**
	 * Test that we can resolve the guid of a dto.
	 */
	@Test
	public void testResolveObjectGuidForDTO() {
		String guid = "BA123";
		final BaseAmountDTO baDTO = new BaseAmountDTO();
		baDTO.setGuid(guid);

		String resolvedGuid = businessObjectResolver.resolveObjectGuid(baDTO);
		assertThat(resolvedGuid).isEqualTo(guid);
	}

	/**
	 * Tests to resolve object guid without guid resolver explicitly defined. 
	 */
	@Test
	public void testResolveObjectGuidWithoutGuidResolverExplicitly() {
		final ProductImpl product = new ProductImpl();
		final String productGuid = "product1";
		product.setGuid(productGuid);

		when(defaultObjectGuidResolver.resolveGuid(product)).thenReturn(productGuid);

		assertThat(businessObjectResolver.resolveObjectGuid(product)).isNotNull();
		verify(defaultObjectGuidResolver).resolveGuid(product);
	}

	/**
	 * Tests to resolve object guid without any guid resolver explicitly defined. 
	 */
	@Test
	public void testResolveObjectGuidWithoutAnyResolverDefined() {
		businessObjectResolver.setObjectGuidResolvers(null);
		final ProductImpl product = new ProductImpl();
		final String productGuid = "product1";
		product.setGuid(productGuid);

		when(defaultObjectGuidResolver.resolveGuid(product)).thenReturn(productGuid);

		assertThat(businessObjectResolver.resolveObjectGuid(product)).isNotNull();
		verify(defaultObjectGuidResolver).resolveGuid(product);
	}

	/**
	 * Expected result for a null parameter value is null.
	 */
	@Test
	public void testGetClassForObjectWithNullArgument() {
		assertThat(businessObjectResolver.getObjectClass(null))
			.as("Expected result for a null parameter value is null")
			.isNull();
	}

	/**
	 * Expecting that asking for an null type returns null value.
	 */
	@Test
	public void testGetClassForNullType() {
		BusinessObjectDescriptor objectDescriptor = new BusinessObjectDescriptorImpl();
		assertThat(businessObjectResolver.getObjectClass(objectDescriptor))
			.as("expected result is null class")
			.isNull();
	}

	/**
	 * Expecting that asking for an unknown type returns null value.
	 */
	@Test
	public void testGetClassForUnknownType() {
		BusinessObjectDescriptor objectDescriptor = new BusinessObjectDescriptorImpl();
		objectDescriptor.setObjectType("unknownType");
		assertThat(businessObjectResolver.getObjectClass(objectDescriptor))
			.as("expected result is null class")
			.isNull();
	}

	/**
	 * Expecting that asking for an expected class returns the class.
	 */
	@Test
	public void testGetClassForKnownType() {
		BusinessObjectDescriptor objectDescriptor = new BusinessObjectDescriptorImpl();
		objectDescriptor.setObjectType(OBJECT_TYPE_PRODUCT);
		Class<?> clazz = businessObjectResolver.getObjectClass(objectDescriptor);
		assertThat(clazz).as("class should not be null").isNotNull();
		assertThat(clazz).as("Product class should have been instantiated").isEqualTo(Product.class);
	}

	/**
	 * Test resolve object descriptor for a collection of objects.
	 */
	@Test
	public void testResolveObjectDescriptorForCollection() {
		final Product product1 = new ProductImpl();
		product1.setGuid("123");

		final Catalog catalog1 = new CatalogImpl();
		catalog1.setGuid("456");

		final ProductBundle productBundle1 = new ProductBundleImpl();
		productBundle1.setGuid("789");

		Set<Object> objects = new LinkedHashSet<>();
		objects.add(product1);
		objects.add(catalog1);
		objects.add(productBundle1);

		when(beanFactory.getBean(ContextIdNames.BUSINESS_OBJECT_DESCRIPTOR)).thenAnswer(invocation -> new BusinessObjectDescriptorImpl());
		when(defaultObjectGuidResolver.resolveGuid(product1)).thenReturn(product1.getGuid());
		when(defaultObjectGuidResolver.resolveGuid(catalog1)).thenReturn(catalog1.getGuid());
		when(defaultObjectGuidResolver.resolveGuid(productBundle1)).thenReturn(productBundle1.getGuid());

		// test product resolution
		Set<BusinessObjectDescriptor> results = businessObjectResolver.resolveObjectDescriptor(objects);
		BusinessObjectDescriptor result = (BusinessObjectDescriptor) CollectionUtils.get(results, 0);
		String objectType = result.getObjectType();
		String objectId = result.getObjectIdentifier();
		assertThat(objectType).isEqualTo(OBJECT_TYPE_PRODUCT);
		assertThat(objectId).isEqualTo(product1.getGuid());

		// test catalog resolution
		result = (BusinessObjectDescriptor) CollectionUtils.get(results, 1);
		objectType = result.getObjectType();
		objectId = result.getObjectIdentifier();
		assertThat(objectType).isEqualTo(OBJECT_TYPE_CATALOG);
		assertThat(objectId).isEqualTo(catalog1.getGuid());

		// test product bundle resolution
		result = (BusinessObjectDescriptor) CollectionUtils.get(results, 2);
		objectType = result.getObjectType();
		objectId = result.getObjectIdentifier();
		assertThat(objectType).isEqualTo(OBJECT_TYPE_PRODUCT_BUNDLE);
		assertThat(objectId).isEqualTo(productBundle1.getGuid());

		verify(defaultObjectGuidResolver).resolveGuid(product1);
		verify(defaultObjectGuidResolver).resolveGuid(catalog1);
		verify(defaultObjectGuidResolver).resolveGuid(productBundle1);
	}

	/**
	 * Test that passing a null to the resolver doesn't cause an exception but rather
	 * just returns a null result.
	 */
	@Test
	public void testResolveObjectWithNull() {
		Object nullObject = null;
		BusinessObjectDescriptor result = businessObjectResolver.resolveObjectDescriptor(nullObject);
		assertThat(result).as("A null object should be resolved to null").isNull();
	}
}
