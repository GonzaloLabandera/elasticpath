/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.importexport.common.adapters.catalogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.importexport.common.caching.CachingService;
import com.elasticpath.importexport.common.dto.catalogs.AttributeDTO;
import com.elasticpath.importexport.common.dto.catalogs.AttributeMultiValueTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeUsageType;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;

/**
 * Verify that AttributeAdapter (from Catalogs) populates catalog domain object from DTO properly and vice versa. 
 * Nested adapters should be tested separately.
 */
public class AttributeAdapterTest {

	private static final String ATTRIBUTE_KEY = "AttributeKey0";

	private static final String LANGUAGE = "en";

	private static final DisplayValue DISPLAY_VALUE = new DisplayValue(LANGUAGE, "attr_name");

	private static final Locale LANGUAGE_LOCALE = LocaleUtils.toLocale(LANGUAGE);

	private static final AttributeUsageType ATTRIBUTE_USAGE = AttributeUsageType.Product;

	private static final AttributeTypeType ATTRIBUTE_TYPE = AttributeTypeType.Image;

	private static final Boolean ATTRIBUTE_LOCALE_DEPENDANT = Boolean.TRUE;

	private static final Boolean ATTRIBUTE_REQUIRED = Boolean.TRUE;

	private static final Boolean ATTRIBUTE_IS_GLOBAL = Boolean.TRUE;

	private AttributeDTO attributeDto;
	
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private BeanFactory mockBeanFactory;
	
	private Attribute mockAttributeDomain;

	private Catalog mockCatalogDomain;
	
	private CachingService mockCachingService;
	
	private AttributeAdapter attributeAdapter;

	@Before
	public void setUp() throws Exception {
		mockBeanFactory = context.mock(BeanFactory.class);
		
		attributeDto = new AttributeDTO();
		mockAttributeDomain = context.mock(Attribute.class);
		mockCatalogDomain = context.mock(Catalog.class);
		mockCachingService = context.mock(CachingService.class);
		
		attributeAdapter = new AttributeAdapter();
		attributeAdapter.setBeanFactory(mockBeanFactory);
		attributeAdapter.setCachingService(mockCachingService);

		context.checking(new Expectations() {
			{
				allowing(mockBeanFactory).getPrototypeBean(ContextIdNames.ATTRIBUTE, Attribute.class);
				will(returnValue(new AttributeImpl()));
			}
		});
	}
	
	private void setUpDefaultDto() {
		attributeDto.setKey(ATTRIBUTE_KEY);
		attributeDto.setNameValues(Arrays.asList(DISPLAY_VALUE));
		attributeDto.setUsage(ATTRIBUTE_USAGE);
		attributeDto.setType(ATTRIBUTE_TYPE);
		attributeDto.setMultiLanguage(ATTRIBUTE_LOCALE_DEPENDANT);
		attributeDto.setRequired(ATTRIBUTE_REQUIRED);
		attributeDto.setMultivalue(AttributeMultiValueTypeType.FALSE);
		attributeDto.setGlobal(ATTRIBUTE_IS_GLOBAL);
	}

	/**
	 * Check that all required fields for Dto object are being set during domain population.
	 */
	@Test
	public void testPopulateDTO() {
		context.checking(new Expectations() {
			{

				atLeast(2).of(mockAttributeDomain).getCatalog();
				will(returnValue(mockCatalogDomain));
				allowing(mockCatalogDomain).getSupportedLocales();
				will(returnValue(Arrays.asList(LANGUAGE_LOCALE)));
				oneOf(mockAttributeDomain).getDisplayName(LANGUAGE_LOCALE);
				will(returnValue(DISPLAY_VALUE.getValue()));

				oneOf(mockAttributeDomain).getKey();
				will(returnValue(ATTRIBUTE_KEY));
				oneOf(mockAttributeDomain).getAttributeUsage();
				will(returnValue(ATTRIBUTE_USAGE.usage()));
				oneOf(mockAttributeDomain).getAttributeType();
				will(returnValue(ATTRIBUTE_TYPE.type()));
				oneOf(mockAttributeDomain).isLocaleDependant();
				will(returnValue(ATTRIBUTE_LOCALE_DEPENDANT));
				oneOf(mockAttributeDomain).isRequired();
				will(returnValue(ATTRIBUTE_REQUIRED));
				oneOf(mockAttributeDomain).getMultiValueType();
				will(returnValue(AttributeMultiValueType.SINGLE_VALUE));
				oneOf(mockAttributeDomain).isGlobal();
				will(returnValue(ATTRIBUTE_IS_GLOBAL));
			}
		});

		AttributeDTO dto = attributeAdapter.createDtoObject();
		
		attributeAdapter.populateDTO(mockAttributeDomain, dto);
		
		assertEquals(ATTRIBUTE_KEY, dto.getKey());
		assertEquals(1, dto.getNameValues().size());
		assertEquals(DISPLAY_VALUE.getValue(), dto.getNameValues().get(0).getValue());
		assertEquals(ATTRIBUTE_USAGE, dto.getUsage());
		assertEquals(ATTRIBUTE_TYPE, dto.getType());
		assertEquals(ATTRIBUTE_LOCALE_DEPENDANT, dto.getMultiLanguage());
		assertEquals(ATTRIBUTE_REQUIRED, dto.getRequired());
		assertEquals(AttributeMultiValueTypeType.FALSE, dto.getMultivalue());
		assertEquals(ATTRIBUTE_IS_GLOBAL, dto.getGlobal());
	}

	/**
	 * Check that all required fields for domain object are being set during domain population.
	 */
	@Test
	public void testPopulateDomain() {
		setUpDefaultDto();

		context.checking(new Expectations() {
			{
				allowing(mockCatalogDomain).getSupportedLocales();
				will(returnValue(Arrays.asList(LANGUAGE_LOCALE)));
				allowing(mockAttributeDomain).getCatalog();
				will(returnValue(mockCatalogDomain));
				allowing(mockCatalogDomain).getSupportedLocales();
				will(returnValue(Arrays.asList(LANGUAGE_LOCALE)));

				oneOf(mockAttributeDomain).setKey(ATTRIBUTE_KEY);
				oneOf(mockAttributeDomain).setDisplayName(DISPLAY_VALUE.getValue(), LANGUAGE_LOCALE);
				oneOf(mockAttributeDomain).setAttributeUsage(ATTRIBUTE_USAGE.usage());
				oneOf(mockAttributeDomain).setAttributeType(ATTRIBUTE_TYPE.type());
				oneOf(mockAttributeDomain).setMultiValueType(AttributeMultiValueType.SINGLE_VALUE);
				oneOf(mockAttributeDomain).setLocaleDependant(ATTRIBUTE_LOCALE_DEPENDANT);
				oneOf(mockAttributeDomain).setRequired(ATTRIBUTE_REQUIRED);
				oneOf(mockAttributeDomain).setGlobal(ATTRIBUTE_IS_GLOBAL);
			}
		});

		attributeAdapter.populateDomain(attributeDto, mockAttributeDomain);
	}	
	
	/**
	 * Check that all required fields for domain object are being set during domain population (unsuccessful situation).
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testPopulateDomainThrowsOnMultilanguageCheck() {
		setUpDefaultDto();

		attributeDto.setType(AttributeTypeType.Integer);
		attributeDto.setMultiLanguage(true);
		attributeAdapter.populateDomain(attributeDto, mockAttributeDomain);
	}
	
	/**
	 * Check that all required fields for domain object are being set during domain population (unsuccessful situation).
	 */
	@Test(expected = PopulationRollbackException.class)
	public void testPopulateDomainThrowsOnMultivalueCheck() {
		setUpDefaultDto();

		attributeDto.setMultiLanguage(false);
		attributeDto.setMultivalue(AttributeMultiValueTypeType.TRUE);
		
		attributeAdapter.populateDomain(attributeDto, mockAttributeDomain);
	}

	/**
	 * Check that CreateDtoObject works. 
	 */
	@Test
	public void testCreateDomainObject() {
		assertNotNull(attributeAdapter.createDomainObject());
	}

	/**
	 * Check that createDomainObject works.
	 */
	@Test
	public void testCreateDtoObject() {
		assertNotNull(attributeAdapter.createDtoObject());
	}

}
