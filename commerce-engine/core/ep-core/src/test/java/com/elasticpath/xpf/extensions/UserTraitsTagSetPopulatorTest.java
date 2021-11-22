/**
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;

@RunWith(MockitoJUnitRunner.class)
public class UserTraitsTagSetPopulatorTest {

	@Mock
	private XPFHttpTagSetContext mockContext;
	@Mock
	private CaseInsensitiveMap<String, String> userTraitValues;

	@Before
	public void setUp() {
		when(mockContext.getUserTraitValues()).thenReturn(userTraitValues);
	}

	@Test
	public void testTagValues() {
		UserTraitsTagSetPopulator classUnderTest = new UserTraitsTagSetPopulator();

		Map<String, String> actual = classUnderTest.collectTagValues(mockContext);

		assertEquals(userTraitValues, actual);
	}
}
