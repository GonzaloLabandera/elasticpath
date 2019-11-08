/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.jsonobjects;

import java.util.List;

/**
 * Detail class state object.
 */
public class Detail {
	private List<String> displayValues;
	private String displayName;
	private List<String> values;
	private String name;

	public List<String> getDisplayValues() {
		return displayValues;
	}

	public void setDisplayValues(final List<String> displayValues) {
		this.displayValues = displayValues;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(final List<String> values) {
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}
