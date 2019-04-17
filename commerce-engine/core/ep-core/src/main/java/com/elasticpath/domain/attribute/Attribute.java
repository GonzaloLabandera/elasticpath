/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.attribute;

import java.util.Locale;
import java.util.Map;

import com.elasticpath.domain.catalog.CatalogObject;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.persistence.api.Entity;

/**
 * An <code>Attribute</code> represents a customized property of an object like <code>Category</code> or
 * <code>Product</code>.
 */
public interface Attribute extends Entity, Comparable<Attribute>, CatalogObject {

	/**
	 * Get the attribute key.
	 * @return the attribute key
	 */
	String getKey();

	/**
	 * Set the attribute key.
	 * @param key the key to set
	 */
	void setKey(String key);

	/**
	 * Return <code>true</code> if the attribute is dependent on the locale.
	 * @return <code>true</code> if the attribute is dependent on the locale
	 */
	boolean isLocaleDependant();

	/**
	 * Set the locale-dependent flag.
	 * @param localeDependent the locale-dependent flag.
	 */
	void setLocaleDependant(boolean localeDependent);

	/**
	 * Return the <code>AttributeType</code> of this attribute.
	 * @return the <code>AttributeType</code> of this attribute
	 */
	AttributeType getAttributeType();

	/**
	 * Set the <code>AttributeType</code> of this attribute.
	 * @param attributeType the attribute type.
	 */
	void setAttributeType(AttributeType attributeType);

	/**
	 * Returns the <code>LocalizedProperties</code>.
	 *
	 * @return the <code>LocalizedProperties</code>
	 */
	LocalizedProperties getLocalizedProperties();

	/**
	 * Sets the <code>LocalizedProperties</code>.
	 *
	 * @param localizedProperties the <code>LocalizedProperties</code>
	 */
	void setLocalizedProperties(LocalizedProperties localizedProperties);

	/**
	 * Get the localized properties map.
	 *
	 * @return the map
	 */
	Map<String, LocalizedPropertyValue> getLocalizedPropertiesMap();

	/**
	 * Set the property map.
	 *
	 * @param localizedPropertiesMap the map to set
	 */
	void setLocalizedPropertiesMap(Map<String, LocalizedPropertyValue> localizedPropertiesMap);

	/**
	 * Get the localized product display name in the given locale.
	 * In the event of no match, retries with broadened versions of
	 * the locale.
	 *
	 * @param locale the locale in which to return the display name
	 * @return the product's display name
	 */
	String getDisplayName(Locale locale);

	/**
	 * Gets the display name of this Attribute for the given locale, optionally
	 * supporting locale broadening, and/or falling back to the Attribute's Catalog's default
	 * locale (in that order). Returns null if the display name for the Attribute isn't found.
	 *
	 * @param locale        the locale for which the display name should be returned
	 * @param broadenLocale true if locale broadening should be used to find a match
	 * @param fallback      true if the Attribute's Catalog's default locale should
	 *                      be used as a fallback, false if there is no fallback
	 * @return the display name
	 */
	String getDisplayName(Locale locale, boolean broadenLocale, boolean fallback);

	/**
	 * Set the product system display name.
	 *
	 * @param name   the product system display name
	 * @param locale the display name's locale
	 */
	void setDisplayName(String name, Locale locale);

	/**
	 * Return the <code>AttributeUsage</code> of this attribute.
	 *
	 * @return the <code>AttributeUsage</code> of this attribute
	 */
	AttributeUsage getAttributeUsage();

	/**
	 * Set the <code>AttributeUsage</code> of this attribute.
	 *
	 * @param attributeUsage the attribute usage
	 */
	void setAttributeUsage(AttributeUsage attributeUsage);

	/**
	 * Return <code>true</code> if the attribute is required.
	 *
	 * @return <code>true</code> if the attribute is required
	 */
	boolean isRequired();

	/**
	 * Set the required flag.
	 *
	 * @param required the required flag
	 */
	void setRequired(boolean required);

	/**
	 * Return <code>true</code> if the value lookup for this attribute is enabled.
	 * If value-lookup is enabled, users have the option of selecting from existing
	 * previously existing attribute values when setting the attribute's value.
	 * @return <code>true</code> if the value lookup for this attribute is enabled.
	 */
	boolean isValueLookupEnabled();

	/**
	 * Sets whether or not the user will have the option of selecting from previously
	 * existing values when editing an attribute.
	 * @param valueLookupEnabled set to <code>true</code> if lookup is to be enabled.
	 */
	void setValueLookupEnabled(boolean valueLookupEnabled);

	/**
	 * Return true if the attribute can have multi value.
	 * 
	 * @return true if the attribute can have multi value.
	 */
	boolean isMultiValueEnabled();
	
	/**
	 * Gets the Multi Value Type.
	 * 
	 * @return get the multi value type
	 */
	AttributeMultiValueType getMultiValueType();
	
	/**
	 * Sets the multi value type.
	 * 
	 * @param multiValueType the multi value type
	 */
	void setMultiValueType(AttributeMultiValueType multiValueType);
	
	/**
	 * Return <code>true</code> if the attribute is system attribute.
	 *
	 * @return <code>true</code> if the attribute is system attribute
	 */
	boolean isSystem();

	/**
	 * Set the system flag.
	 *
	 * @param system the system flag
	 */
	void setSystem(boolean system);

	/**
	 * Set the attribute's usage id.
	 * @param usageId the attribute's usage Id to be set.
	 */
	void setAttributeUsageId(int usageId);

	/**
	 * Gets whether this attribute is global.
	 *
	 * @return whether this attribute is global
	 */
	boolean isGlobal();

	/**
	 * Sets whether this attribute is global.
	 *
	 * @param global whether this attribute is global
	 */
	void setGlobal(boolean global);
}
