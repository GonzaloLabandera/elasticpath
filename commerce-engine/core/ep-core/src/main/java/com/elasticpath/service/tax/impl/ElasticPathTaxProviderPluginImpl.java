/**
 * Copyright (c) Elastic Path Software Inc., 2013-2014
 */
package com.elasticpath.service.tax.impl;

import java.util.Objects;

import com.elasticpath.plugin.tax.TaxProviderPluginInvoker;
import com.elasticpath.plugin.tax.calculator.TaxCalculator;
import com.elasticpath.plugin.tax.capability.TaxCalculationCapability;
import com.elasticpath.plugin.tax.domain.TaxableItemContainer;
import com.elasticpath.plugin.tax.domain.TaxedItemContainer;
import com.elasticpath.plugin.tax.rate.TaxRateDescriptor;
import com.elasticpath.plugin.tax.resolver.TaxOperationResolvers;
import com.elasticpath.plugin.tax.spi.AbstractTaxProviderPluginSPI;
import com.elasticpath.service.tax.resolver.TaxRateDescriptorResolver;

/**
 * ElasticPath tax provider plugin.
 */
public class ElasticPathTaxProviderPluginImpl extends AbstractTaxProviderPluginSPI implements TaxCalculationCapability {

	/**
	 * Elastic Path default Tax Provider name.
	 */
	public static final String PROVIDER_NAME = "ElasticPath";

	private final TaxOperationResolvers taxOperationResolvers = new TaxOperationResolvers();

	private TaxCalculator taxCalculator;
	private TaxRateDescriptorResolver taxRateDescriptorResolver;

	/**
	 * This method is called by Spring and used to store an instance of {@link TaxRateDescriptorResolver} into {@link TaxRateDescriptorResolver}.
	 */
	public void init() {
		taxOperationResolvers.putResolver(TaxRateDescriptorResolver.class, taxRateDescriptorResolver);
	}

	@Override
	public String getName() {
		return PROVIDER_NAME;
	}

	@Override
	public TaxedItemContainer calculate(final TaxableItemContainer container) {
		return getTaxCalculator().calculate(container, getTaxOperationResolvers());
	}


	private TaxOperationResolvers getTaxOperationResolvers() {
		return taxOperationResolvers;
	}

	public TaxCalculator getTaxCalculator() {
		return taxCalculator;
	}

	public void setTaxCalculator(final TaxCalculator taxCalculator) {
		this.taxCalculator = taxCalculator;
	}

	public void setTaxRateDescriptorResolver(final TaxRateDescriptorResolver taxRateDescriptorResolver) {
		this.taxRateDescriptorResolver = taxRateDescriptorResolver;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TaxRateDescriptor)) {
			return false;
		}
		TaxProviderPluginInvoker taxProvider = (TaxProviderPluginInvoker) obj;
		return Objects.equals(getName(), taxProvider.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

}
