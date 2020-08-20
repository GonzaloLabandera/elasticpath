/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.attribute.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.ElementClassCriteria;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.ForeignKey;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.impl.AbstractLegacyEntityImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.AttributeLocalizedPropertyValueImpl;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * <code>Attribute</code> represents a customized property of an object like <code>Category</code> or <code>Product</code>.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.GodClass"})
@Entity
@Table(name = AttributeImpl.TABLE_NAME)
@FetchGroups({
		@FetchGroup(name = FetchGroupConstants.CATEGORY_ATTRIBUTES,
				attributes = {@FetchAttribute(name = "required"), @FetchAttribute(name = "system"), @FetchAttribute(name = "valueLookupEnabled"),
						@FetchAttribute(name = "localizedPropertiesMap")
				},
				fetchGroups = {FetchGroupConstants.PRODUCT_INDEX}),
		@FetchGroup(name = FetchGroupConstants.PRODUCT_INDEX, attributes = {@FetchAttribute(name = "localeDependant"),
				@FetchAttribute(name = "multiValueType"), @FetchAttribute(name = "attributeTypeId"), @FetchAttribute(name = "key")
		}),
		@FetchGroup(name = FetchGroupConstants.ATTRIBUTE_VALUES,
				attributes = {@FetchAttribute(name = "multiValueType")})
})
public class AttributeImpl extends AbstractLegacyEntityImpl implements Attribute {
	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TATTRIBUTE";

	/**
	 * The name of localized property -- display name.
	 */
	public static final String LOCALIZED_PROPERTY_DISPLAY_NAME = "attributeDisplayName";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;
	// Give an empty string as default key to make comparison easier.
	private String key = "";

	private boolean localeDependant;

	private boolean required;

	private boolean valueLookupEnabled;

	private boolean system;

	private AttributeMultiValueType multiValueType = AttributeMultiValueType.SINGLE_VALUE;

	private int attributeTypeId;

	private int attributeUsageId;

	private long uidPk;

	private Catalog catalog;

	private boolean global;

	private LocalizedProperties localizedProperties;

	private Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();

	/**
	 * Get the attribute key.
	 *
	 * @return the attribute key
	 */
	@Override
	@Basic
	@Column(name = "ATTRIBUTE_KEY")
	public String getKey() {
		return key;
	}

	/**
	 * Set the attribute key.
	 *
	 * @param key the key to set
	 */
	@Override
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Return <code>true</code> if the attribute is dependant on the locale.
	 *
	 * @return <code>true</code> if the attribute is dependant on the locale
	 */
	@Override
	@Basic
	@Column(name = "LOCALE_DEPENDANT")
	public boolean isLocaleDependant() {
		return localeDependant;
	}

	/**
	 * Set the locale dependant flag.
	 *
	 * @param localeDependant the locale-dependant flag.
	 */
	@Override
	public void setLocaleDependant(final boolean localeDependant) {
		this.localeDependant = localeDependant;
	}

	/**
	 * Return the <code>AttributeType</code> of this attribute.
	 *
	 * @return the <code>AttributeType</code> of this attribute
	 */
	@Override
	@Transient
	public AttributeType getAttributeType() {
		try {
			return AttributeType.valueOf(getAttributeTypeId());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}

	/**
	 * Set the <code>AttributeType</code> of this attribute.
	 *
	 * @param attributeType the attribute type.
	 */
	@Override
	public void setAttributeType(final AttributeType attributeType) {
		if (attributeType == null) {
			setAttributeTypeId(0);
		} else {
			setAttributeTypeId(attributeType.getTypeId());
		}
	}

	@Override
	@Transient
	public String getDisplayName(final Locale locale) {
		return getDisplayName(locale, true, false);
	}

	@Override
	@Transient
	public String getDisplayName(final Locale locale, final boolean broadenLocale, final boolean fallback) {
		String displayName = broadenLocale ? getLocalizedProperties().getValue(LOCALIZED_PROPERTY_DISPLAY_NAME, locale)
				: getLocalizedProperties().getValueWithoutFallBack(LOCALIZED_PROPERTY_DISPLAY_NAME, locale);

		if (displayName == null && fallback) {
			Locale fallbackLocale;
			if (catalog == null) {
				// Catalog might not exist in case of Customer Profile attributes, hence use JVM default locale.
				// Commerce Manager Admin call for customer profile attributes uses Accept-Language request header.
				fallbackLocale = Locale.getDefault();
			} else {
				// For, Catalog, Product, SKU attributes use the Catalog's default locale to find a display name for the Attribute
				fallbackLocale = getCatalog().getDefaultLocale();
			}
			displayName = getLocalizedProperties().getValue(LOCALIZED_PROPERTY_DISPLAY_NAME, fallbackLocale);
		}

		if (displayName == null) {
			displayName = "";
		}

		return displayName;
	}

	@Override
	@Transient
	public LocalizedProperties getLocalizedProperties() {
		if (localizedProperties == null) {
			localizedProperties = getPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class);
			localizedProperties.setLocalizedPropertiesMap(getLocalizedPropertiesMap(), ContextIdNames.ATTRIBUTE_LOCALIZED_PROPERTY_VALUE);
		}
		return localizedProperties;
	}

	@Override
	public void setLocalizedProperties(final LocalizedProperties localizedProperties) {
		this.localizedProperties = localizedProperties;
		if (localizedProperties != null) {
			setLocalizedPropertiesMap(localizedProperties.getLocalizedPropertiesMap());
		}
	}

	/**
	 * Set the product system display name.
	 *
	 * @param name   the product system display name
	 * @param locale the display name's locale
	 */
	@Override
	public void setDisplayName(final String name, final Locale locale) {
		getLocalizedProperties().setValue(LOCALIZED_PROPERTY_DISPLAY_NAME, locale, name);
	}

	@Override
	@OneToMany(targetEntity = AttributeLocalizedPropertyValueImpl.class, fetch = FetchType.EAGER, cascade = {CascadeType.ALL
	}, orphanRemoval = true)
	@MapKey(name = "localizedPropertyKey")
	@ElementJoinColumn(name = "OBJECT_UID", referencedColumnName = "UIDPK", nullable = false)
	@ElementClassCriteria
	@ElementDependent
	public Map<String, LocalizedPropertyValue> getLocalizedPropertiesMap() {
		return localizedPropertiesMap;
	}

	@Override
	public void setLocalizedPropertiesMap(final Map<String, LocalizedPropertyValue> localizedPropertiesMap) {
		this.localizedPropertiesMap = localizedPropertiesMap;
	}

	/**
	 * Compares this attribute with the specified object for order.
	 *
	 * @param attribute the given attribute
	 * @return a negative integer, zero, or a positive integer if this object is less than, equal to, or greater than the specified object.
	 * @throws EpDomainException in case the given object is not a <code>Attribute</code>
	 */
	@Override
	public int compareTo(final Attribute attribute) throws EpDomainException {
		return Comparator.comparing((Attribute other) -> other.getDisplayName(getLocaleForAttribute(attribute)))
				.thenComparing(Attribute::getKey)
				.thenComparingLong(Attribute::getUidPk)
				.compare(this, attribute);
	}

	/**
	 * Derives the locale applicable for a attribute based on attribute usage value. Catalog, Product and SKU use
	 * corresponding catalog's default locale, customer profile attributes use Locale.getDefault.
	 *
	 * @param attribute Attribute for which associated locale is being derived.
	 * @return Locale of the attribute specified.
	 */
	private Locale getLocaleForAttribute(final Attribute attribute) {
		return attribute.getCatalog() == null ? Locale.getDefault() : attribute.getCatalog().getDefaultLocale();
	}

	/**
	 * Return the <code>AttributeUsage</code> of this attribute.
	 *
	 * @return the <code>AttributeUsage</code> of this attribute
	 */
	@Override
	@Transient
	public AttributeUsage getAttributeUsage() {
		return lookupAttributeUsage(getAttributeUsageIdInternal());
	}

	/**
	 * Set the <code>AttributeUsage</code> of this attribute.
	 *
	 * @param attributeUsage the attribute usage
	 */
	@Override
	public void setAttributeUsage(final AttributeUsage attributeUsage) {
		if (attributeUsage == null) {
			setAttributeUsageId(0);
		} else {
			setAttributeUsageId(attributeUsage.getValue());
		}
	}

	/**
	 * Return <code>true</code> if the attribute is required.
	 *
	 * @return <code>true</code> if the attribute is required
	 */
	@Override
	@Basic
	@Column(name = "REQUIRED")
	public boolean isRequired() {
		return required;
	}

	/**
	 * Set the required flag.
	 *
	 * @param required the required flag
	 */
	@Override
	public void setRequired(final boolean required) {
		this.required = required;
	}

	/**
	 * Return <code>true</code> if the attribute is system attribute.
	 *
	 * @return <code>true</code> if the attribute is system attribute
	 */
	@Override
	@Basic
	@Column(name = "SYSTEM")
	public boolean isSystem() {
		return system;
	}

	/**
	 * Set the system flag.
	 *
	 * @param system the system flag
	 */
	@Override
	public void setSystem(final boolean system) {
		this.system = system;
	}

	/**
	 * Return <code>true</code> if the value lookup for this attribute is enabled. If value-lookup is enabled, users have the option of selecting
	 * from existing previously existing attribute values when setting the attribute's value.
	 *
	 * @return <code>true</code> if the value lookup for this attribute is enabled.
	 */
	@Override
	@Basic
	@Column(name = "VALUE_LOOKUP_ENABLED")
	public boolean isValueLookupEnabled() {
		return valueLookupEnabled;
	}

	/**
	 * Sets whether or not the user will have the option of selecting from previously existing values when editing an attribute.
	 *
	 * @param valueLookupEnabled set to <code>true</code> if lookup is to be enabled.
	 */
	@Override
	public void setValueLookupEnabled(final boolean valueLookupEnabled) {
		this.valueLookupEnabled = valueLookupEnabled;
	}

	/**
	 * Return the guid.
	 *
	 * @return the guid.
	 */
	@Override
	@Transient
	public String getGuid() {
		return getKey();
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid to set.
	 */
	@Override
	public void setGuid(final String guid) {
		setKey(guid);
	}

	/**
	 * Return true if the attribute can have multi value.
	 *
	 * @return true if the attribute can have multi value.
	 */
	@Transient
	@Override
	public boolean isMultiValueEnabled() {
		return !AttributeMultiValueType.SINGLE_VALUE.equals(multiValueType);
	}

	/**
	 * Unpacks the integer representation of multi value type and returns
	 * the AttributeMultiValueType of this attribute.
	 *
	 * @return attribute multi value type
	 */
	@Override
	@Persistent
	@Column(name = "MULTI_VALUE_ENABLED")
	@Externalizer("getOrdinal")
	@Factory("createAttributeMultiValueType")
	public AttributeMultiValueType getMultiValueType() {
		return multiValueType;
	}

	/**
	 * Unpacks the ordinal value of the type to persist in the database.
	 *
	 * @param multiValueType the multi value type
	 */
	@Override
	public void setMultiValueType(final AttributeMultiValueType multiValueType) {
		this.multiValueType = multiValueType;
	}

	/**
	 * Get the attribute type Id.
	 *
	 * @return the attribute type Id
	 */
	@Basic
	@Column(name = "ATTRIBUTE_TYPE")
	protected int getAttributeTypeId() {
		return attributeTypeId;
	}

	/**
	 * Set the attribute type Id.
	 *
	 * @param attributeTypeId the Id
	 */
	protected void setAttributeTypeId(final int attributeTypeId) {
		this.attributeTypeId = attributeTypeId;
	}

	/**
	 * Get the attribute usage Id.
	 *
	 * @return the Id
	 */
	@Basic
	@Column(name = "ATTRIBUTE_USAGE")
	protected int getAttributeUsageIdInternal() {
		return attributeUsageId;
	}

	/**
	 * Set the attribute usage Id.
	 *
	 * @param attributeUsageId the Id
	 */
	protected void setAttributeUsageIdInternal(final int attributeUsageId) {
		this.attributeUsageId = attributeUsageId;
	}

	/**
	 * Get the attribute usage Id.
	 *
	 * @return the Id
	 */
	@Transient
	public int getAttributeUsageId() {
		return getAttributeUsageIdInternal();
	}

	/**
	 * Set the attribute usage Id.
	 *
	 * @param attributeUsageId the Id
	 */
	@Override
	@Transient
	public void setAttributeUsageId(final int attributeUsageId) {
		setAttributeUsageIdInternal(attributeUsageId);
	}

	private AttributeUsage lookupAttributeUsage(final int attributeUsageId) {
		if (attributeUsageId == 0) {
			return null;
		}
		return getPrototypeBean(ContextIdNames.ATTRIBUTE_USAGE, AttributeUsage.class)
				.getAttributeUsageById(attributeUsageId);
	}

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	/**
	 * Get the catalog that this attribute belongs to (for catalog related attributes).
	 *
	 * @return the catalog
	 */
	@Override
	@ManyToOne(optional = true, targetEntity = CatalogImpl.class, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinColumn(name = "CATALOG_UID", nullable = true)
	@ForeignKey
	public Catalog getCatalog() {
		return catalog;
	}

	/**
	 * Set the catalog that this attribute type belongs to (for catalog related attributes).
	 *
	 * @param catalog the catalog to set
	 */
	@Override
	public void setCatalog(final Catalog catalog) {
		this.catalog = catalog;
	}

	/**
	 * Gets whether this attribute is global.
	 *
	 * @return whether this attribute is global
	 */
	@Override
	@Column(name = "ATTR_GLOBAL")
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Sets whether this attribute is global.
	 *
	 * @param global whether this attribute is global
	 */
	@Override
	public void setGlobal(final boolean global) {
		this.global = global;
	}

	@Override
	@SuppressWarnings("PMD.UselessOverridingMethod")
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!super.equals(obj)) {
			return false;
		}

		return getClass() == obj.getClass();
	}


	/**
	 * String representation of this object.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("Attribute -> GUID: ").append(getGuid());
		sbf.append(" Localized Properties: ").append(getLocalizedProperties());
		sbf.append(" Type Usage: ").append(getAttributeUsage());
		return sbf.toString();
	}

}