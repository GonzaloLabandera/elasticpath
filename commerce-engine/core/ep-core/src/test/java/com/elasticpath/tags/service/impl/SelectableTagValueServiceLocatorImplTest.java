/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tags.service.impl;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.service.SelectableTagValueProvider;

/**
 * 
 * Test for SelectableTagValueServiceLocatorImpl.
 *
 */
public class SelectableTagValueServiceLocatorImplTest  {
	
	
	private TagValueType exampleTagValueType;
	
	private TagValueType simpleTagValueType;
	
	private SelectableTagValueProvider<?> exampleValueProvider;
	
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	
	private final SelectableTagValueServiceLocatorImpl serviceLocatorImpl = new 
		SelectableTagValueServiceLocatorImpl();
	
	/**
	 * Initializing test.
	 */
	@Before	
	public void setUp() {
		
		exampleTagValueType = context.mock(TagValueType.class, "exampleTagValueType");
		
		simpleTagValueType = context.mock(TagValueType.class, "simpleTagValueType");
		
		exampleValueProvider = context.mock(SelectableTagValueProvider.class, "exampleValueProvider");
		
		context.checking(new Expectations() {
			{
				allowing(exampleTagValueType).getGuid();
				will(returnValue("example"));
				
				allowing(simpleTagValueType).getGuid();
				will(returnValue("simpleText"));
				
			}
			
		});
		
		serviceLocatorImpl.getValueProviders().put("example", exampleValueProvider);
		
	}
	
	/**
	 * Test, that localor can find value provider service for configured tag value type guid. 
	 */
	@Test
	public void testLocateService() {
		
		SelectableTagValueProvider<?> provider = serviceLocatorImpl.getSelectableTagValueProvider(exampleTagValueType);
		
		assertNotNull(provider);
		
	}
	
	/**
	 * Test, that localor can find value provider service will return null for not nullable and 
	 * not configured tag value type. 
	 */
	@Test
	public void testLocateServiceNotFound() {
		
		SelectableTagValueProvider<?> provider = serviceLocatorImpl.getSelectableTagValueProvider(simpleTagValueType);
		
		assertNull(provider);
		
	}
	
	/**
	 * Test, that localor can find value provider service for configured tag value type guid. 
	 */
	@Test
	public void testLocateServiceNotFoundForNullType() {
		
		SelectableTagValueProvider<?> provider = serviceLocatorImpl.getSelectableTagValueProvider(null);
		
		assertNull(provider);
		
	}
	
	
	

}
