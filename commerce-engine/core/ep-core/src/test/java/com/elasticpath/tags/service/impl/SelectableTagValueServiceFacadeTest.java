/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tags.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.tags.domain.SelectableValue;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.domain.impl.SelectableValueImpl;
import com.elasticpath.tags.service.SelectableTagValueProvider;
import com.elasticpath.tags.service.SelectableTagValueServiceLocator;
/**
 * 
 * Test for SelectableTagValueServiceLocator.
 *
 */
public class SelectableTagValueServiceFacadeTest  {
	private final Locale locale = Locale.getDefault();

	private TagValueType exampleTagValueType;
	
	private TagValueType withoutSelectableValuesTagValueType;
	
	private SelectableTagValueServiceLocator selectableTagValueServiceLocator;

	private final SelectableTagValueFacadeImpl selectableTagValueServiceFacadeImpl = new SelectableTagValueFacadeImpl();
	
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@SuppressWarnings("unchecked")
	private final SelectableTagValueProvider<SelectableValue<String>> selectableExampleTagValueProvider =
			context.mock(SelectableTagValueProvider.class, "selectableExampleTagValueService");

	/**
	 * Initializing test.
	 */
	@Before	
	public void setUp() {
		selectableTagValueServiceLocator = context.mock(SelectableTagValueServiceLocator.class, "selectableTagValueServiceLocator");
		selectableTagValueServiceFacadeImpl.setSelectableTagValueServiceLocator(selectableTagValueServiceLocator);
	}

	/**
	 * Test, that facade return values. 
	 */
	@Test
	public void testFacadeCanRetreiveValue() {
		exampleTagValueType = context.mock(TagValueType.class, "exampleTagValueType");

		final SelectableValue<String> optionA = new SelectableValueImpl<>("A", "Option A");
		final SelectableValue<String> optionB = new SelectableValueImpl<>("B", "Option B");

		final Collection<SelectableValue<String>> options = new ArrayList<>();
		options.add(optionA);
		options.add(optionB);

		context.checking(new Expectations() {
			{
				allowing(selectableExampleTagValueProvider).getSelectableValues(locale, exampleTagValueType, null);
				will(returnValue(options));

				allowing(selectableTagValueServiceLocator).getSelectableTagValueProvider(exampleTagValueType);
				will(returnValue(selectableExampleTagValueProvider));
			}
		});

		final List<SelectableValue<String>> list = selectableTagValueServiceFacadeImpl.getSelectableValues(exampleTagValueType, locale, null);

		assertEquals("List does not contain expected selectable values", options, list);
	}

	/**
	 * Test, that facade can not retrieve value for tag value type object that was not configured. 
	 */
	@Test
	public void testFacadeCanNotRetreiveValue() {
		withoutSelectableValuesTagValueType = context.mock(TagValueType.class, "withoutSelectableValuesTagValueType");

		context.checking(new Expectations() {
			{
				allowing(selectableTagValueServiceLocator).getSelectableTagValueProvider(withoutSelectableValuesTagValueType);
				will(returnValue(null));
			}
		});

		final List<SelectableValue<Object>> list = selectableTagValueServiceFacadeImpl.getSelectableValues(
				withoutSelectableValuesTagValueType,
				locale,
				null);

		assertNull(list);
	}

	/**
	 * Test, that facade can not retrieve value for null tag value type object. 
	 */
	@Test
	public void testFacadeCanNotRetreiveValueForNullTagValueType() {
		context.checking(new Expectations() {
			{
				allowing(selectableTagValueServiceLocator).getSelectableTagValueProvider(null);
				will(returnValue(null));
			}
		});

		final List<SelectableValue<Object>>  list = selectableTagValueServiceFacadeImpl.getSelectableValues(null, locale, null);
		
		assertNull(list);
	}

}
