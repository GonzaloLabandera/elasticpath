/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

import java.lang.annotation.Annotation;
import java.util.Optional;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;

/**
 * Abstract payment provider plugin using its bean name as a unique ID.
 */
public abstract class AbstractPaymentProviderPlugin implements PaymentProviderPlugin {

	private String uniquePluginId;

	@Override
	public void setUniquePluginId(final String uniquePluginId) {
		this.uniquePluginId = uniquePluginId;
	}

	@Override
	public String getUniquePluginId() {
		return uniquePluginId;
	}

	@Override
	public <T extends Capability> Optional<T> getCapability(final Class<T> capability) {
		if (hasCapability(capability)) {
			return Optional.of(capability.cast(this));
		}
		return Optional.empty();
	}

	/**
	 * Checks if capability is supported by this plugin.
	 *
	 * @param capability {@link Capability} class
	 * @param <T>        {@link Capability} class
	 * @return true if capability is supported
	 */
	protected <T extends Capability> boolean hasCapability(final Class<T> capability) {
		return capability.isAssignableFrom(getClass());
	}

	@Override
	public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
		return getClass().isAnnotationPresent(annotationClass);
	}

	@Override
	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

}
