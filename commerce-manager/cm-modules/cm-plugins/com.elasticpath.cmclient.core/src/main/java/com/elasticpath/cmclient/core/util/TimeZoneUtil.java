/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.elasticpath.cmclient.core.CoreMessages;

/**
 *
 * TimeZoneUtil present time zones for UI.
 * TODO merge com.elasticpath.cmclient.admin.stores.util.TimeZonesUtil
 * TODO add just time zone offset if localization resource not found
 *
 */
public final class TimeZoneUtil {
	
	private static List<String> uniqueGmtTimeZonesList;
	private static Map<String, Float> gmtTimeZoneNameOffsetMap;
	private static Map<Float, String> gmtOffsetTimeZoneNameMap;
	
	static {
		uniqueGmtTimeZonesList = new ArrayList<String>();
		gmtTimeZoneNameOffsetMap = new HashMap<String, Float>();
		gmtOffsetTimeZoneNameMap = new HashMap<Float, String>();
		String[] zoneIds = TimeZone.getAvailableIDs();
		for (String zoneId : zoneIds) {
			final TimeZone timeZone = TimeZone.getTimeZone(zoneId);
			final int rawOffset = timeZone.getRawOffset();
			final int hour = rawOffset / (SimpleUITimeZone.MINUTES * SimpleUITimeZone.SECONDS * SimpleUITimeZone.MILISECONDS);
			final int minutes = Math.abs(rawOffset / (SimpleUITimeZone.MINUTES * SimpleUITimeZone.MILISECONDS)) % SimpleUITimeZone.MINUTES;
			final String timeZoneLocalizationKey = getGMTTimeZoneKeyName(hour, minutes);
			final SimpleUITimeZone simpleUITimeZone = new SimpleUITimeZone(
					rawOffset,
					CoreMessages.get().getMessage(timeZoneLocalizationKey));
			addGMTTimeZoneName(simpleUITimeZone);
		}		
	}
	
	
	/**
	 * Construct time zone message resource key by given hour and minutes.
	 *  
	 * @param hours given hours
	 * @param minutes given minutes
	 * @return String key for message resources. Result will be like this GMTplus05colon30
	 * 
	 */
	static String getGMTTimeZoneKeyName(final int hours, final int minutes) {
		StringBuilder keyName = new StringBuilder();
		keyName.append("GMT");		 //$NON-NLS-1$
		if (hours < 0) {
			keyName.append("minus"); //$NON-NLS-1$
		} else {
			keyName.append("plus"); //$NON-NLS-1$
		}
		keyName.append(String.format("%02d", Math.abs(hours))); //$NON-NLS-1$		
		keyName.append("colon"); //$NON-NLS-1$		
		keyName.append(String.format("%02d", minutes));	//$NON-NLS-1$
		return keyName.toString();
	}
	
	/**
	 * Add given time zone key name to given list, if list not contains time zone key name.   
	 * @param gmtTimeZone time zone message resource key.
	 */
	private static void addGMTTimeZoneName(final SimpleUITimeZone gmtTimeZone) {
		if (!uniqueGmtTimeZonesList.contains(gmtTimeZone.getID())) {
			uniqueGmtTimeZonesList.add(gmtTimeZone.getID());
			gmtTimeZoneNameOffsetMap.put(
					gmtTimeZone.getID(), 
					gmtTimeZone.getFloatGMTOffset());
			gmtOffsetTimeZoneNameMap.put(
					gmtTimeZone.getFloatGMTOffset(),
					gmtTimeZone.getID()
					);
		}
	}
	
	/**
	 * Get the GMT time zone offset by name.
	 * @param name given time zone name.
	 * @return time zone offset.
	 */
	public static float getTimeZoneOffsetByName(final String name) {
		return gmtTimeZoneNameOffsetMap.get(name);
	}

	/**
	 * Get the GMT time zone  name by offset.
	 * @param offset given offset
	 * @return time zone name
	 */
	public static String getTimeZoneNameByOffset(final float offset) {
		return gmtOffsetTimeZoneNameMap.get(offset);
	}
	
	
	/**
	 * 
	 * Get the list of unique time zones.
	 * @return List of unique time zones.
	 * 
	 */
	public static List<String> getUniqueGmtTimeZones() {
		return uniqueGmtTimeZonesList;
	}
	
	private TimeZoneUtil() {
		
	}

}
