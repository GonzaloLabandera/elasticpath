/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.integration.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.tags.domain.SelectableValue;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.service.SelectableTagValueFacade;
import com.elasticpath.tags.service.TagValueTypeService;
import com.elasticpath.test.integration.BasicSpringContextTest;

/**
 * Tag Selectable Values tests.
 */
public class SelectableValuesTest extends BasicSpringContextTest {
	
	@Autowired
	private SelectableTagValueFacade selectableTagValueService;
    
	@Autowired
	private TagValueTypeService tagValueTypeService;
    
    /**
     * Tests selectable values services for 
     * tag value type, that not provide selectable values. 
     * 
     * @throws Exception a data access exception
     */
    @Test
    public void testNotExistingSelectableValuesProvider() throws Exception {
    	
    	TagValueType tagValueType = createTagValueType();

    	List<SelectableValue<String>> list = selectableTagValueService.getSelectableValues(tagValueType, Locale.getDefault(), null);

    	assertNull(list);    	
    	
    }    
    
    private TagValueType createTagValueType() {
    	return getBeanFactory().getPrototypeBean(ContextIdNames.TAG_VALUE_TYPE, TagValueType.class);
    }
    
}
