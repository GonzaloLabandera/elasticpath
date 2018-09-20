/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.util;

import junit.framework.TestCase;

/**
 * 
 * Test for SimpleUITimeZone class.
 *
 */
public class SimpleUITimeZoneTest  extends TestCase {
	
	private static final int FIVE = 5; // hours
	
	private static final int FORTY_FIVE = 45; // minutes 
	
	private static final int EIGHT = 8; // hours
	
	private static final int HALF_AN_HOUR = 30; // in minutes
	
	private static final String TIME_ZONE_ID = "Not important"; //$NON-NLS-1$
	
	private static final int PST = -1 * EIGHT * SimpleUITimeZone.MINUTES * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS;
	
	private static final float PST_FLOAT = -8.0f;
	
	//(GMT-08:30) Near Pasific time
	private static final int PST_N = -1 * (EIGHT * SimpleUITimeZone.MINUTES * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS 
		+ HALF_AN_HOUR * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS);
	
	private static final float PST_N_FLOAT = -8.5f;
	
	//(GMT+05:45) Kathmandu
	private static final int KATHMANDU = FIVE * SimpleUITimeZone.MINUTES * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS 
			+ FORTY_FIVE * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS;
	
	private static final float KATHMANDU_FLOAT = 5.75f;
	
	
	/**
	 * Test getFloatGMTOffset method.
	 */
	public void testGetFloatGMTOffset() {
		
		SimpleUITimeZone  simpleUITimeZone = new SimpleUITimeZone(0, TIME_ZONE_ID); 
		
		assertEquals(0f, simpleUITimeZone.getFloatGMTOffset());
		
		simpleUITimeZone = new SimpleUITimeZone(
				PST, 
				TIME_ZONE_ID); 
		
		assertEquals(PST_FLOAT, 
				simpleUITimeZone.getFloatGMTOffset());		
		
	
		simpleUITimeZone = new SimpleUITimeZone(
				PST_N, 
				TIME_ZONE_ID); 
		
		assertEquals(PST_N_FLOAT, 
				simpleUITimeZone.getFloatGMTOffset());
		
		simpleUITimeZone = new SimpleUITimeZone(
				KATHMANDU, 
				TIME_ZONE_ID); 
		
		assertEquals(KATHMANDU_FLOAT, 
				simpleUITimeZone.getFloatGMTOffset());		
		
		
		
	}
	
	

}
