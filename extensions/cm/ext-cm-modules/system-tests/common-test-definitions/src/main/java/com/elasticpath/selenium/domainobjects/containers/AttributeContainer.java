/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.containers;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.selenium.domainobjects.Attribute;

/**
 * Attribute container class.
 */
public class AttributeContainer {
	private final List<Attribute> attributes = new ArrayList<>();

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void addAtribute(final Attribute attribute) {
		if (attribute == null) {
			return;
		}
		attributes.add(attribute);
	}

	public String getAttributeKeyByPartialCode(final String key) {
		String resultCode = "";
		for (int i = 0; i < getAttributes().size(); i++) {
			if (getAttributes().get(i).getAttributeKey().startsWith(key)) {
				resultCode = getAttributes().get(i).getAttributeKey();
			}
		}
		return resultCode;
	}

	public String getAttributeNameByPartialCodeAndLanguage(final String key, final String language) {
		String resultCode = "";
		for (int i = 0; i < getAttributes().size(); i++) {
			if (getAttributes().get(i).getAttributeKey().startsWith(key)) {
				resultCode = getAttributes().get(i).getName(language);
			}
		}
		return resultCode;
	}
}