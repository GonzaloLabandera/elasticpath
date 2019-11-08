/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

/**
 * Domain object to represent a TagDefinition.  Used in selenium tests.
 */
public class TagDefinition {

	private String code;
	private String name;
	private String language;
	private String displayName;
	private String description;
	private String dictionaries;
	private String fieldType;

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(final String dictionaries) {
		this.dictionaries = dictionaries;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(final String fieldType) {
		this.fieldType = fieldType;
	}
}
