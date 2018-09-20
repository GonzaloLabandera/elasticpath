/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.util;

import java.util.SimpleTimeZone;

/**
 *
 * SimpleUITimeZone represent SimpleTimeZone for UI.
 *
 */
public class SimpleUITimeZone extends SimpleTimeZone {
	
	private static final long serialVersionUID = -4806863069302884241L;

	/** Minutes in hour. */
	public static final int MINUTES = 60;
	
	/** Seconds in minute. */
	public static final int SECONDS = 60;
	
	/** Miliseconds in second. */
	public static final int MILISECONDS = 1000;

	/**
	 * Constructs a SimpleUITimeZone with the given base time zone offset from GMT
	 * and time zone ID with no daylight saving time schedule.
	 *
	 * @param rawOffset  The base time zone offset in milliseconds to GMT.
	 * @param timeZoneStringId         The time zone name that is given to this instance.
	 */
	public SimpleUITimeZone(final int rawOffset, final String timeZoneStringId) {
		super(rawOffset, timeZoneStringId);		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		int idHashCore = 0;
		if (getID() != null) {
			idHashCore = getID().hashCode();
		}
		result = prime * result + idHashCore;
		result = prime * result + getRawOffset();		
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (super.equals(obj)) {
			final SimpleUITimeZone other = (SimpleUITimeZone) obj;
			return getRawOffset() == other.getRawOffset();
		}
		
		return false;
	}
	
	
	@Override
	public String toString() {
		return getID();
	}

	/**
	 * Get the float gmt offset.  
	 * TODO  First option       5:30 , 30 min equal to half from hour, i.e. return can be 5.5
	 * Second option  5:30 return will be as 5.3
	 * In this case 30 minutes treated as half from 1 hour. So return for 30 minutes will be .5 
	 * @return float gmt offset.
	 */
	public float getFloatGMTOffset() {
		int rawOffset = getRawOffset();
		int hour = rawOffset / (MINUTES * SECONDS * MILISECONDS);
		int minutes = (rawOffset / (MINUTES * MILISECONDS)) % MINUTES;
		return hour +  (float) minutes /  MINUTES;
	}
	
	

}
