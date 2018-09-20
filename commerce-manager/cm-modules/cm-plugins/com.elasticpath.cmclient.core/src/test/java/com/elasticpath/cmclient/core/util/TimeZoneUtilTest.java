/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;


import org.eclipse.rap.rwt.testfixture.TestContext;

/**
*
* Test TimeZoneUtil class, that present time zones for UI.
*
*/

public class TimeZoneUtilTest 	{
	
	private static final int FORTY_FIVE = 45;
	
	private static final int TEN = 10;

	@Rule
	public TestContext context = new TestContext();


	/**
	 * Test obtaine localization key for time zone.
	 */
	@Test
	public void testGetGMTTimeZoneKeyName() {
		
		assertEquals("GMTminus10colon45", TimeZoneUtil.getGMTTimeZoneKeyName(-1 * TEN, FORTY_FIVE)); //$NON-NLS-1$
		
		assertEquals("GMTplus10colon45", TimeZoneUtil.getGMTTimeZoneKeyName(TEN, FORTY_FIVE)); //$NON-NLS-1$
		
	}
	
	/**
	 * Test the unique time list.
	 */
	@Test
	public void testgetUniqueGmtTimeZones() {
		
		Map<String, String> uniqueMap = new HashMap<String, String>(); 
		
		List<String> uniqueGmtTimeZones = TimeZoneUtil.getUniqueGmtTimeZones();
		
		assertNotNull(uniqueGmtTimeZones);
		
		assertFalse(uniqueGmtTimeZones.isEmpty());
		
		for (String simpleUITimeZone : uniqueGmtTimeZones) {
			assertNotNull(simpleUITimeZone);
			uniqueMap.put(simpleUITimeZone, simpleUITimeZone);
		}
		
		assertEquals(uniqueMap.size(), uniqueGmtTimeZones.size());
		
	}

}
