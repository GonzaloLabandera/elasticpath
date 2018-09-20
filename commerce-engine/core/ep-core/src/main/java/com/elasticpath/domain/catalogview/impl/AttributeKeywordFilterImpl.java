/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.catalogview.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.SeoConstants;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeValueWithType;
import com.elasticpath.domain.catalogview.AttributeKeywordFilter;
import com.elasticpath.domain.catalogview.EpCatalogViewRequestBindException;
import com.elasticpath.service.attribute.AttributeService;

/**
 * The implementation of AttributeFilter.
 */
public class AttributeKeywordFilterImpl extends AbstractFilterImpl<AttributeKeywordFilter> implements AttributeKeywordFilter {

	/** Serial version id. */
	public static final long serialVersionUID = 5000000001L;
	
	private Attribute attribute;

	private String attributeKey;

	private AttributeValueWithType attributeValue;

	private Locale locale;
	
	private String displayName;

	private static final Logger LOG = Logger.getLogger(AttributeKeywordFilterImpl.class);
	
	private String alias;

	/**
	 * Generate and return an attribute filter id.
	 *
	 * @param attributeKey the attributeKey
	 * @param seoId the seoId
	 * @return an attribute filter id
	 */
	public String getAttributeFilterId(final String attributeKey, final String seoId) {
		final StringBuilder sbf = new StringBuilder();

		sbf.append(SeoConstants.ATTRIBUTE_KEYWORD_FILTER_PREFIX).append(attributeKey);
		if (seoId != null) {
			sbf.append(SEPARATOR);
			sbf.append(seoId);
		}
		return sbf.toString();
	}

	/**
	 * Sets the display name that will be shown when describing the AttributeKeywordFilter. Typically
	 * this will be used as the label for the filtered navigation link.
	 *
	 * @param displayName the display name for this AttributeKeywordFilter.
	 */
	@Override
	public void setDisplayName(final String displayName) {
		if (null != displayName && StringUtils.isBlank(displayName)) {
			LOG.info("Blank display name was specified for attribute filter: attribute="
					+ getAttributeKey() + ", id=" + getId() + ", this was probably unintential,"
					+ " there may be a problem with your filtered navigation configuration."
					+ " Ignoring assignment.");
		} else {
			this.displayName = displayName;
		}
	}

	/**
	 * Get the display name for the filter.
	 *
	 * @param locale the given locale.
	 * @return the display name.
	 */
	@Override
	public String getDisplayName(final Locale locale) {
		if (null != displayName) {
			return displayName;
		}

		if (this.getAttributeValue() != null) {
			return String.valueOf(this.getAttributeValue());
		}

		if (this.getAttribute() != null) {
			return this.getAttribute().getName();
		}
		return null;
	}

	/**
	 * Get the locale for this attribute.
	 *
	 * @return the locale
	 */
	@Override
	public Locale getLocale() {
		if (isLocalized()) {
			return locale;
		}
		return null;
	}

	/**
	 * Set the locale for this attribute.
	 *
	 * @param locale the locale to set
	 */
	@Override
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Get the attribute key.
	 *
	 * @return the attributeKey
	 */
	@Override
	public String getAttributeKey() {
		return attributeKey;
	}

	/**
	 * Set the attribute Key.
	 *
	 * @param attributeKey the attributeKey to set
	 */
	@Override
	public void setAttributeKey(final String attributeKey) {
		final AttributeService attributeService = (AttributeService) getBean(ContextIdNames.ATTRIBUTE_SERVICE);
		attribute = attributeService.findByKey(attributeKey);
		this.attributeKey = attributeKey;
	}

	/**
	 * @return the attribute
	 */
	@Override
	public Attribute getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute the attribute to set
	 */
	@Override
	public void setAttribute(final Attribute attribute) {
		this.attribute = attribute;
		this.attributeKey = attribute.getKey();
	}

	/**
	 * This method is not used.
	 *
	 * @param locale the locale
	 * @return the SEO url of the filter with the given locale.
	 */
	@Override
	public String getSeoName(final Locale locale) {
		final String displayName = this.getDisplayName(locale);
		if (displayName == null) {
			return null;
		}
		return this.getUtility().escapeName2UrlFriendly(displayName, locale);
	}

	/**
	 * Gets the SEO id for this filter.
	 *
	 * @return the SEO identifier of the filter with the given locale.
	 */
	@Override
	public String getSeoId() {
		StringBuilder seoString = new StringBuilder(getAttributePrefixAndKey());
		seoString.append(getSeparatorInToken());
		seoString.append(encodeAlias(getAlias()));
		return seoString.toString();
	}

	private String encodeAlias(final String alias) {
		return StringUtils.strip(Base64.encodeBase64URLSafeString(alias.getBytes(Charsets.UTF_8)), "=");
	}
	
	private String decodeAlias(final String keyword) {
		return new String(Base64.decodeBase64(keyword), Charsets.UTF_8);
	}

	
	@Override
	public String getAttributePrefixAndKey() {
		StringBuilder seoString = new StringBuilder(SeoConstants.ATTRIBUTE_KEYWORD_FILTER_PREFIX);
		seoString.append(getAttributeKey());
		return seoString.toString();
	}

	/**
	 * @param attributeValue the given attributeValue
	 * @return true when the attributeValue matchs with this filter.
	 */
	public boolean matchAttributeMultiValue(final AttributeValueWithType attributeValue) {
		// Currently only short text can have multi values.
		List<String> multiValues = attributeValue.getShortTextMultiValues();
		if (multiValues != null && !multiValues.isEmpty()) {
			String filterValue = getAttributeValue().getStringValue();
			for (String singleValue : multiValues) {
				if (filterValue.equals(singleValue)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the {@link AttributeValueWithType} of this filter.
	 *
	 * @return the {@link AttributeValueWithType} of this filter
	 */
	@Override
	public AttributeValueWithType getAttributeValue() {
		return attributeValue;
	}

	/**
	 * Set the attribute value.
	 * The given value should be a String, and it should be convert to the AttributeValue.
	 * @param attributeValue the attributeValue to set
	 */
	@Override
	public void setAttributeValue(final AttributeValueWithType attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	/**
	 * Set the attribute value.
	 * The given value should be a String, and it should be convert to the AttributeValue.
	 * @param attributeValue the attributeValue to set
	 */
	@Override
	public void setAttributeValueFromString(final String attributeValue) {
		setAttributeValue(constructAttributeWithType(attributeValue));
	}


	/**
	 * Compares this object with the specified {@link AttributeKeywordFilter} for ordering.
	 * 
	 * @param other the given object
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal
	 *         to, or greater than the specified object
	 */
	@Override
	public int compareTo(final AttributeKeywordFilter other) {
		if (this == other) {
			return 0;
		}

		// if either is null or they are not the same
		if (getAttributeKey() != null ^ other.getAttributeKey() != null || !getAttributeKey().equals(other.getAttributeKey())) {
			throw new EpDomainException("Cannot compare attribute filter with a differenct keys.");
		}
		return getAttributeValue().compareTo(other.getAttributeValue());
	}
	
	private AttributeValueWithType constructAttributeWithType(final String string) {
		final AttributeValueWithType attr = (AttributeValueWithType) getBean(ContextIdNames.ATTRIBUTE_VALUE);
		attr.setAttribute(getAttribute());
		attr.setAttributeType(getAttribute().getAttributeType());
		attr.setStringValue(string);
		return attr;
	}

	@Override
	public void initialize(final Map<String, Object> properties) {
		if (properties.get(ATTRIBUTE_PROPERTY) == null) {
			this.setAttributeKey((String) properties.get(ATTRIBUTE_KEY_PROPERTY));
		} else {
			this.setAttribute((Attribute) properties.get(ATTRIBUTE_PROPERTY));
		}
		this.setAttributeValue(constructAttributeWithType((String) properties.get(ATTRIBUTE_KEYWORD_PROPERTY)));
		this.setAlias((String) properties.get(ATTRIBUTE_VALUES_ALIAS_PROPERTY));
		this.setId(getSeoId());
	}

	@Override
	public Map<String, Object> parseFilterString(final String filterIdStr) {
		final String idWithoutPrefix = filterIdStr.substring(filterIdStr.indexOf(SeoConstants.ATTRIBUTE_KEYWORD_FILTER_PREFIX)
				+ SeoConstants.ATTRIBUTE_KEYWORD_FILTER_PREFIX.length());
		
		String attributeKeyToken = StringUtils.substringBeforeLast(idWithoutPrefix, SeoConstants.DEFAULT_SEPARATOR_IN_TOKEN);
		String attributeKeywordToken = StringUtils.substringAfterLast(idWithoutPrefix, SeoConstants.DEFAULT_SEPARATOR_IN_TOKEN);
		
		if (StringUtils.isBlank(attributeKeyToken) || StringUtils.isBlank(attributeKeywordToken)) {
			throw new EpCatalogViewRequestBindException("Invalid filter id:" + filterIdStr);
		}
		
		Map<String, Object> tokenMap = new HashMap<>();
		try {
			tokenMap.put(ATTRIBUTE_KEY_PROPERTY, attributeKeyToken);
			tokenMap.put(ATTRIBUTE_KEYWORD_PROPERTY, decodeAlias(attributeKeywordToken));
			tokenMap.put(ATTRIBUTE_VALUES_ALIAS_PROPERTY, decodeAlias(attributeKeywordToken));
		} catch (RuntimeException e) {
			throw new EpCatalogViewRequestBindException("Invalid filter id:" + filterIdStr, e);
		}
		return tokenMap;
	}

	/**
	 * Set an alias for the attribute value.
	 * 
	 * @param alias an SEO-friendly alias
	 */
	public void setAlias(final String alias) {
		this.alias = alias;
	}

	/**
	 * Get the SEO alias for the attribute value.
	 * 
	 * @return an SEO friendly alias for the attribute value
	 */
	public String getAlias() {
		return alias;
	}
}
