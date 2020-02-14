/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;

/**
 * Base class for implementing payment provider plugins.
 */
public interface PaymentProviderPlugin {

	/**
	 * <p>Retrieve the requested capability from the plugin.</p>
	 *
	 * @param <T>  the particular Capability type being requested
	 * @param type the class of the capability being requested
	 * @return an optional of the requested Capability
	 */
	<T extends Capability> Optional<T> getCapability(Class<T> type);

	/**
	 * Gets payment Vendor Id for display name.
	 *
	 * @return the payment provider Id
	 */
	String getPaymentVendorId();

	/**
	 * Gets payment method Id for display name.
	 *
	 * @return the payment method name
	 */
	String getPaymentMethodId();

	/**
	 * Sets unique plugin name.
	 *
	 * @param uniquePluginId unique plugin identifier
	 */
	void setUniquePluginId(String uniquePluginId);

	/**
	 * Gets unique plugin name.
	 *
	 * @return the unique plugin identifier
	 */
	String getUniquePluginId();

	/**
	 * <p>Retrieve the keys associated with this plugin.</p>
	 * The PluginConfigurationKey required to configure payment plugin
	 *
	 * @return a PluginConfigurationKey object
	 */
	List<PluginConfigurationKey> getConfigurationKeys();

	/**
	 * Returns true if an annotation for the specified type is present on this class, else false.
	 * <p/>
	 * Calls {@link Class#isAnnotationPresent(Class)} for a plugin class behind the potential proxy.
	 *
	 * @param annotationClass the Class object corresponding to the annotation type
	 * @return true if an annotation for the specified annotation type is present on this element, else false
	 */
	boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

	/**
	 * Returns the class loader for the class.
	 * <p/>
	 * Calls {@link Class#getClassLoader()} for a plugin class behind the potential proxy.
	 *
	 * @return the class loader that loaded the class or interface represented by this object.
	 */
	ClassLoader getClassLoader();

}
