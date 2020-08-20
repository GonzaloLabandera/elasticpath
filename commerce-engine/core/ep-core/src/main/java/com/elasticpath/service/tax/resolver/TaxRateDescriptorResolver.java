/**
 * Copyright (c) Elastic Path Software Inc., 2013-2014
 */
package com.elasticpath.service.tax.resolver;

import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.plugin.tax.domain.TaxableItem;
import com.elasticpath.plugin.tax.domain.TaxableItemContainer;
import com.elasticpath.plugin.tax.rate.TaxRateDescriptorResult;
import com.elasticpath.plugin.tax.resolver.TaxOperationResolver;

/**
 * Resolves tax rate descriptors and tax jurisdictions for a {@link TaxableItemContainer}.
 */
public interface TaxRateDescriptorResolver extends TaxOperationResolver {

	/**
	 * Returns tax rate descriptors based for a {@link TaxableItem} and {@link TaxableItemContainer}.
	 *
	 * @param taxableItem the taxable item
	 * @param container the taxable item container
	 * @param taxJurisdiction the tax jurisdiction
	 * @return the tax rate descriptor result
	 */
	TaxRateDescriptorResult findTaxRateDescriptors(TaxableItem taxableItem, TaxableItemContainer container, TaxJurisdiction taxJurisdiction);
	
	/**
	 * Returns the tax rate descriptor with tax jurisdiction for the specified store and address in a {@link TaxableItemContainer}.
	 * 
	 * @param container the taxable item container
	 * @param taxJurisdiction the tax jurisdiction
	 * @return the tax rate descriptor result containing only the jurisdiction
	 */
	TaxRateDescriptorResult findTaxRateDescriptorResult(TaxableItemContainer container, TaxJurisdiction taxJurisdiction);

}
