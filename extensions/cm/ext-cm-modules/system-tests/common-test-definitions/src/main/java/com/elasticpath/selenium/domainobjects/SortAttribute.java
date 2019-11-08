package com.elasticpath.selenium.domainobjects;

/**
 * Sort Attribute class.
 */
public class SortAttribute {

	public static final String ASCENDING = "Ascending";
	public static final String DESCENDING = "Descending";
	public static final String ATTRIBUTE = "Attribute";
	public static final String FIELD = "Field";


	private String sortGroup;
	private String attributeKey;
	private String sortOrder;
	private String type;
	private String language;
	private String displayName;

	public SortAttribute() {
		// Default
	}

	public SortAttribute(final String sortGroup, final String attributeKey, final String sortOrder, final String language,
						 final String displayName) {
		this.sortGroup = sortGroup;
		this.attributeKey = attributeKey;
		this.sortOrder = sortOrder;
		this.language = language;
		this.displayName = displayName;
	}

	public SortAttribute(final String sortOrder, final String language, final String displayName) {
		this.sortOrder = sortOrder;
		this.language = language;
		this.displayName = displayName;
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(final String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getSortGroup() {
		return sortGroup;
	}

	public void setSortGroup(final String sortGroup) {
		this.sortGroup = sortGroup;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}
}
