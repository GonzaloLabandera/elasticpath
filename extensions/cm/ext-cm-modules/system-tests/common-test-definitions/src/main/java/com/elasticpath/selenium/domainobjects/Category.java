/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Category class.
 */
public class Category {
	private String categoryCode;
	private final Map<String, String> names = new HashMap<>();
	private String categoryName;
	private String categoryType;
	private String storeVisible;
	private String attrLongTextName;
	private String attrLongTextValue;
	private String attrDecimalName;
	private String attrDecimalValue;
	private String attrShortTextName;
	private String attrShortTextValue;
	private String enableDateTime;
	private String disableDateTime;
	private String parentCategory;

	public String getName(final String language) {
		return names.get(language);
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public String getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(final String parentCategory) {
		this.parentCategory = parentCategory;
	}
	public String getEnableDateTime() {
		return enableDateTime;
	}

	public String getDisableDateTime() {
		return disableDateTime;
	}

	public void setEnableDateTime(final String enableDateTime) {
		this.enableDateTime = enableDateTime;
	}

	public void setDisableDateTime(final String disableDateTime) {
		this.disableDateTime = disableDateTime;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(final String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(final String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(final String categoryType) {
		this.categoryType = categoryType;
	}

	public String getStoreVisible() {
		return storeVisible;
	}

	public void setStoreVisible(final String storeVisible) {
		this.storeVisible = storeVisible;
	}

	public String getAttrLongTextName() {
		return attrLongTextName;
	}

	public void setAttrLongTextName(final String attrLongTextName) {
		this.attrLongTextName = attrLongTextName;
	}

	public String getAttrLongTextValue() {
		return attrLongTextValue;
	}

	public void setAttrLongTextValue(final String attrLongTextValue) {
		this.attrLongTextValue = attrLongTextValue;
	}

	public String getAttrDecimalName() {
		return attrDecimalName;
	}

	public void setAttrDecimalName(final String attrDecimalName) {
		this.attrDecimalName = attrDecimalName;
	}

	public String getAttrDecimalValue() {
		return attrDecimalValue;
	}

	public void setAttrDecimalValue(final String attrDecimalValue) {
		this.attrDecimalValue = attrDecimalValue;
	}

	public String getAttrShortTextName() {
		return attrShortTextName;
	}

	public void setAttrShortTextName(final String attrShortTextName) {
		this.attrShortTextName = attrShortTextName;
	}

	public String getAttrShortTextValue() {
		return attrShortTextValue;
	}

	public void setAttrShortTextValue(final String attrShortTextValue) {
		this.attrShortTextValue = attrShortTextValue;
	}
}
