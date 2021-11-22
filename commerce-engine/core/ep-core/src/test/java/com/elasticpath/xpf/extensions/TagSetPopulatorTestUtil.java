/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

/**
 * Utilities for use in tagset populator tests.
 */
public final class TagSetPopulatorTestUtil {

	private TagSetPopulatorTestUtil() {
		//static class
	}

	/**
	 * Test Utility method to create CaseInsensitiveMap with a single key,value pair.
	 * @param key key of the map entry
	 * @param value value of the map entry
	 * @return CaseInsensitiveMap with a single key,value entry as per provided input.
 	 */
	public static Map<String, String> singletonCaseInsensitiveMap(final String key, final String value) {
		CaseInsensitiveMap<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>();
		caseInsensitiveMap.put(key, value);
		return caseInsensitiveMap;
	}
}
