/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;
/**
 * Brand class state object.
 */
public class Brand {

	private String code;
	private final Map<String, String> names = new HashMap<>();
	private final Map<String, String> oldNames = new HashMap<>();

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getName(final String language) {
		return names.get(language);
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public void setOldName(final String language, final String name) {
		oldNames.put(language, name);
	}

	public String getOldName(final String language) {
		return oldNames.get(language);
	}
}
