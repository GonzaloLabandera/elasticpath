/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.stores.editors.facets;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.domain.search.FieldKeyType;

/**
 * Model class for Facet Configuration.
 */
@SuppressWarnings("PMD.GodClass")
public class FacetModel {

	private static final int ONE_HUNDRED = 100;
	private static final int NUM_OF_ROWS = 3;

	private static final String DASH = " - ";
	private static final String PLUS = " +";
	private static final String ZERO_TO_ONE_HUNDRED = "0 - 100";
	private static final String ONE_HUNDRED_TO_TWO_HUNDRED = "100 - 200";
	private static final String TWO_HUNDRED_TO_THREE_HUNDRED = "200 - 300";
	private static final String THREE_HUNDRED_PLUS = "300 +";

	private static final BigDecimal BIG_DECIMAL_ONE_HUNDRED = new BigDecimal("100");
	private static final BigDecimal BIG_DECIMAL_TWO_HUNDRED = new BigDecimal("200");
	private static final BigDecimal BIG_DECIMAL_THREE_HUNDRED = new BigDecimal("300");

	private String facetName;
	private boolean searchable;
	private FacetType facetType;
	private Map<String, String> displayNameMap = new HashMap<>();
	private FieldKeyType fieldKeyType;
	private String defaultLocaleDisplayName;
	private Collection<Locale> locales;
	private SortedSet<RangeFacet> rangeFacets = new TreeSet<>();
	private String attributeKey;
	private FacetGroup facetGroup;
	private String guid;

	/**
	 * Constructor.
	 * @param facetName field key
	 * @param fieldKeyType field key type
	 * @param searchable searchable
	 * @param facetType facet type
	 * @param locales locales
	 * @param defaultLocaleDisplayName default locale display name
	 * @param attributeKey attribute key
	 */
	public FacetModel(final String facetName, final FieldKeyType fieldKeyType, final boolean searchable, final FacetType facetType,
					  final Collection<Locale> locales, final String defaultLocaleDisplayName, final String attributeKey) {
		this.defaultLocaleDisplayName = defaultLocaleDisplayName;
		this.facetName = facetName;
		this.searchable = searchable;
		this.facetType = facetType;
		this.locales = locales;
		this.attributeKey = attributeKey;
		setFieldKeyType(fieldKeyType);
		populateMaps();
	}

	private void populateMaps() {
		for (Locale locale : locales) {
			String localeString = locale.toString();
			addDisplayName(localeString, StringUtils.EMPTY);
		}
		addDisplayName(defaultLocaleDisplayName, facetName);
	}

	public String getFacetName() {
		return facetName;
	}

	public void setFacetName(final String facetName) {
		this.facetName = facetName;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(final boolean searchable) {
		this.searchable = searchable;
	}

	public FacetType getFacetType() {
		return facetType;
	}

	public void setFacetType(final FacetType facetType) {
		this.facetType = facetType;
	}

	/**
	 * Add a display name for a certain locale display name.
	 * @param localeDisplayName locale display name
	 * @param displayName facet model display name
	 */
	public void addDisplayName(final String localeDisplayName, final String displayName) {
		displayNameMap.put(localeDisplayName, displayName);
	}

	public String getDefaultDisplayName() {
		return displayNameMap.get(defaultLocaleDisplayName);
	}

	public String getDefaultLocaleDisplayName() {
		return defaultLocaleDisplayName;
	}

	public FieldKeyType getFieldKeyType() {
		return fieldKeyType;
	}

	/**
	 * Need to populate rangefacets when the facet is of type integer or decimal.
	 * @param fieldKeyType field type
	 */
	public void setFieldKeyType(final FieldKeyType fieldKeyType) {
		this.fieldKeyType = fieldKeyType;
		if (fieldKeyType == FieldKeyType.INTEGER || fieldKeyType == FieldKeyType.DECIMAL) {
			populateDefaultRangeFacets();
		}
	}

	/**
	 * Add default range values for facets [0, 100) 0 - 100 , [100, 200) 100 - 200, [200, 300) 200 - 300, [300, *) 300 +.
	 */
	public void populateDefaultRangeFacets() {
		int multiplier = 0;
		for (; multiplier < NUM_OF_ROWS; multiplier++) {
			String startRange = String.valueOf(multiplier * ONE_HUNDRED);
			String endRange = String.valueOf((multiplier + 1) * ONE_HUNDRED);

			rangeFacets.add(new RangeFacet(
					new BigDecimal(startRange), new BigDecimal(endRange), createRangeFacetDisplayMap(startRange + DASH + endRange)));
		}

		String start = String.valueOf(multiplier * ONE_HUNDRED);
		rangeFacets.add(new RangeFacet(new BigDecimal(start), null, createRangeFacetDisplayMap(start + PLUS)));
	}

	private Map<String, String> createRangeFacetDisplayMap(final String displayName) {
		Map<String, String> displayNameMap = new HashMap<>();
		for (Locale locale : locales) {
			String localeString = locale.toString();
			displayNameMap.put(localeString, displayName);
		}
		return displayNameMap;
	}

	/**
	 * When a user adds a new locale, we need to add default display names only for the default ranges (0, 100) , (100, 200) , (200, 300) , (300, *).
	 */
	private void populateRangeFacetDefaultDisplayNameForNewLocales() {
		if (fieldKeyType == FieldKeyType.DECIMAL || fieldKeyType == FieldKeyType.INTEGER) {
			for (Locale locale : locales) {
				String localeString = locale.toString();
				for (RangeFacet rangeFacet : rangeFacets) {
					BigDecimal start = rangeFacet.getStart();
					BigDecimal end = rangeFacet.getEnd();
					Map<String, String> displayMap = rangeFacet.getDisplayNameMap();
					updateDefaultRangeDisplayMaps(localeString, start, end, displayMap);
				}
			}
		}
	}

	private void updateDefaultRangeDisplayMaps(final String localeString, final BigDecimal start, final BigDecimal end,
											   final Map<String, String> displayMap) {
		if (ObjectUtils.equals(start, BigDecimal.ZERO) && ObjectUtils.equals(end, BIG_DECIMAL_ONE_HUNDRED)) {
			updateNullDefaultValues(localeString, displayMap, ZERO_TO_ONE_HUNDRED);
		} else if (ObjectUtils.equals(start, BIG_DECIMAL_ONE_HUNDRED) && ObjectUtils.equals(end, BIG_DECIMAL_TWO_HUNDRED)) {
			updateNullDefaultValues(localeString, displayMap, ONE_HUNDRED_TO_TWO_HUNDRED);
		} else if (ObjectUtils.equals(start, BIG_DECIMAL_TWO_HUNDRED) && ObjectUtils.equals(end, BIG_DECIMAL_THREE_HUNDRED)) {
			updateNullDefaultValues(localeString, displayMap, TWO_HUNDRED_TO_THREE_HUNDRED);
		} else if (ObjectUtils.equals(start, BIG_DECIMAL_THREE_HUNDRED) && end == null) {
			updateNullDefaultValues(localeString, displayMap, THREE_HUNDRED_PLUS);
		} else {
			displayMap.putIfAbsent(localeString, StringUtils.EMPTY);
		}
	}

	private void updateNullDefaultValues(final String localeString, final Map<String, String> displayMap, final String value) {
		String localizedValue = displayMap.get(localeString);
		if (StringUtils.isBlank(localizedValue)) {
			displayMap.put(localeString, value);
		}
	}

	private void populateDisplayNameMap() {
		for (Locale locale : locales) {
			String localeString = locale.toString();
			if (!displayNameMap.containsKey(localeString)) {
				displayNameMap.put(localeString, StringUtils.EMPTY);
			}
		}
	}

	/**
	 * Adds default values for new locales in display map and range facet display maps.
	 */
	public void updateDefaultValuesInMaps() {
		populateRangeFacetDefaultDisplayNameForNewLocales();
		populateDisplayNameMap();
	}

	public void setDefaultLocaleDisplayName(final String defaultLocaleDisplayName) {
		this.defaultLocaleDisplayName = defaultLocaleDisplayName;
	}

	/**
	 * Get a display name based on locale display name.
	 * @param localeDisplayName locale display name
	 * @return display name
	 */
	public String getDisplayName(final String localeDisplayName) {
		return displayNameMap.get(localeDisplayName);
	}

	public SortedSet<RangeFacet> getRangeFacets() {
		return rangeFacets;
	}

	public void setRangeFacets(final SortedSet<RangeFacet> rangeFacets) {
		this.rangeFacets = rangeFacets;
	}

	public Map<String, String> getDisplayNameMap() {
		return displayNameMap;
	}

	public void setDisplayNameMap(final Map<String, String> displayNameMap) {
		this.displayNameMap = displayNameMap;
	}

	public Collection<Locale> getLocales() {
		return locales;
	}

	public void setLocales(final Collection<Locale> locales) {
		this.locales = locales;
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	public FacetGroup getFacetGroup() {
		return facetGroup;
	}

	public void setFacetGroup(final FacetGroup facetGroup) {
		this.facetGroup = facetGroup;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}
}