/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.EagerFetchMode;
import org.apache.openjpa.persistence.jdbc.ElementClassCriteria;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.FetchMode;

import com.elasticpath.persistence.api.AbstractEntityImpl;
import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValueImpl;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * The implementation for payment provider configuration.
 */
@Entity
@Table(name = PaymentProviderConfigurationImpl.TABLE_NAME)
@DataCache(enabled = true)
public class PaymentProviderConfigurationImpl extends AbstractEntityImpl implements PaymentProviderConfiguration {

	/**
	 * Payment configuration table name.
	 */
	public static final String TABLE_NAME = "TPAYMENTPROVIDERCONFIG";

	/**
	 * The name of payment localized property -- displayName.
	 */
	public static final String LOCALIZED_PROPERTY_DISPLAY_NAME = "paymentProviderDisplayName";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private Map<String, PaymentLocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();

	private PaymentLocalizedProperties paymentLocalizedProperties;

	private long uidPk;

	private String guid;

	private String paymentProviderPluginId;

	private String configurationName;

	private String defaultDisplayName;

	private Set<PaymentProviderConfigurationData> paymentConfigurationData;

	private PaymentProviderConfigurationStatus status = PaymentProviderConfigurationStatus.DRAFT;

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return this.uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Get the localized properties map.
	 *
	 * @return the map
	 */
	@OneToMany(targetEntity = PaymentLocalizedPropertyValueImpl.class, fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL }, orphanRemoval = true)
	@EagerFetchMode(FetchMode.PARALLEL)
	@MapKey(name = "paymentLocalizedPropertyKey")
	@ElementJoinColumn(name = "OBJECT_UID", referencedColumnName = "UIDPK", nullable = false)
	@ElementClassCriteria
	@ElementDependent
	public Map<String, PaymentLocalizedPropertyValue> getLocalizedPropertiesMap() {
		return localizedPropertiesMap;
	}
	
	/**
	 * Returns the display name of the <code>PaymentProviderConfiguration</code> with the given locale.
	 *
	 * @param locale the locale
	 * @return the display name of the PaymentProviderConfiguration displayName
	 */
	@Override
	@Transient
	public String getDisplayName(final Locale locale) {
		String displayName = null;
		if (getPaymentLocalizedProperties() != null) {
			displayName = getPaymentLocalizedProperties().getValue(LOCALIZED_PROPERTY_DISPLAY_NAME, locale);
		}
		if (displayName == null) {
			displayName = getDefaultDisplayName();
		}
		if (displayName == null) {
			displayName = getConfigurationName();
		}
		return displayName;
	}

	@Override
	@Basic
	@Column(name = "DEFAULT_DISPLAY_NAME")
	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	@Override
	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	/**
	 * Returns the <code>PaymentLocalizedProperties</code>, i.e. <code>PaymentProviderConfiguration</code> name.
	 *
	 * @return the <code>PaymentLocalizedProperties</code>
	 */
	@Override
	@Transient
	public PaymentLocalizedProperties getPaymentLocalizedProperties() {
		if (paymentLocalizedProperties == null) {
			paymentLocalizedProperties = new PaymentLocalizedPropertiesImpl();
			paymentLocalizedProperties.setPaymentLocalizedPropertiesMap(getLocalizedPropertiesMap(),
					PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE);
		}
		return paymentLocalizedProperties;
	}

	/**
	 * Set the <code>PaymentLocalizedProperties</code>, i.e. <code>PaymentProviderConfiguration</code> name.
	 *
	 * @param paymentLocalizedProperties - the <code>PaymentLocalizedProperties</code>
	 */
	@Override
	public void setPaymentLocalizedProperties(final PaymentLocalizedProperties paymentLocalizedProperties) {
		this.paymentLocalizedProperties = paymentLocalizedProperties;
		this.setLocalizedPropertiesMap(paymentLocalizedProperties.getPaymentLocalizedPropertiesMap());
	}

	public void setLocalizedPropertiesMap(final Map<String, PaymentLocalizedPropertyValue> localizedPropertiesMap) {
		this.localizedPropertiesMap = localizedPropertiesMap;
	}

	@Override
	@Basic
	@Column(name = "PAYMENT_PROVIDER_PLUGIN_ID")
	public String getPaymentProviderPluginId() {
		return paymentProviderPluginId;
	}

	@Override
	public void setPaymentProviderPluginId(final String paymentProviderPluginId) {
		this.paymentProviderPluginId = paymentProviderPluginId;
	}

	@Override
	@Basic
	@Column(name = "CONFIGURATION_NAME")
	public String getConfigurationName() {
		return configurationName;
	}

	@Override
	public void setConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
	}

	@Override
	@OneToMany(targetEntity = PaymentProviderConfigurationDataImpl.class, fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	@ElementJoinColumn(name = "PAYMENTPROVIDERCONFIG_UID", nullable = false)
	@ElementForeignKey
	@ElementDependent
	public Set<PaymentProviderConfigurationData> getPaymentConfigurationData() {
		return paymentConfigurationData;
	}

	@Override
	public void setPaymentConfigurationData(final Set<PaymentProviderConfigurationData> paymentConfigurationData) {
		this.paymentConfigurationData = paymentConfigurationData;
	}

	@Override
	@Basic
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	public PaymentProviderConfigurationStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(final PaymentProviderConfigurationStatus status) {
		this.status = status;
	}

	@Override
	@Transient
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}

	@Override
	@Transient
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PaymentProviderConfigurationImpl) {
			PaymentProviderConfigurationImpl other = (PaymentProviderConfigurationImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}

