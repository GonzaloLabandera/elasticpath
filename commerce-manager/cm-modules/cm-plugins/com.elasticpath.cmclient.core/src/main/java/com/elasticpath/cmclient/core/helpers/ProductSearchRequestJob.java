/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.helpers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.EventType;
import com.elasticpath.cmclient.core.event.SearchResultEvent;
import com.elasticpath.cmclient.core.service.CatalogEventService;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSkuLoadTuner;
import com.elasticpath.service.catalog.ProductService;

/**
 * This class represents a job responsible for retrieving products from the database.
 */
public class ProductSearchRequestJob extends AbstractSearchRequestJob<Product> implements ISearchJobSource {

	private static final Logger LOG = LogManager.getLogger(ProductSearchRequestJob.class);

	private final ProductService productService;
	
	private final boolean loadSku;

	private Object source;

	/**
	 * Constructor.
	 * 
	 * @param loadSku the indicator specifying if retrieving SKU is needed.
	 */
	public ProductSearchRequestJob(final boolean loadSku) {
		super();

		this.productService = BeanLocator.getSingletonBean(ContextIdNames.PRODUCT_SERVICE, ProductService.class);
		this.loadSku = loadSku;
	}

	@Override
	public void fireItemsUpdated(final List<Product> itemList, final int startIndex, final int totalFound) {
		
		Object source = null;
		if (this.getSource() == null) {
			source = this;
		} else {
			source = this.getSource();
		}
		SearchResultEvent<Product> event = new SearchResultEvent<Product>(source, itemList, startIndex, totalFound, EventType.SEARCH);
		CatalogEventService.getInstance().notifyProductSearchResultReturned(event);
	}

	/**
	 * Get the source which call this job.
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}
	
	/**
	 * Get the source which call this job.
	 * @param source the source object
	 */
	public void setSource(final Object source) {
		this.source = source;
	}

	/**
	 * Gets a list of {@link Product} with the given <code>uidList</code>.
	 * 
	 * @param uidList a list of {@link Product} UIDs
	 * @return a list of {@link Product}s
	 */
	public List<Product> getItems(final List<Long> uidList) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Converting %1$S UID(s) to products.", uidList.size())); //$NON-NLS-1$
		}

		final ProductLoadTuner productLoadTuner = BeanLocator.getPrototypeBean(ContextIdNames.PRODUCT_LOAD_TUNER, ProductLoadTuner.class);
		productLoadTuner.setLoadingCategories(true);
		if (loadSku) {
			final ProductSkuLoadTuner productSkuLoadTuner = BeanLocator
					.getPrototypeBean(ContextIdNames.PRODUCT_SKU_LOAD_TUNER, ProductSkuLoadTuner.class);
			
			productLoadTuner.setProductSkuLoadTuner(productSkuLoadTuner);
			productLoadTuner.setLoadingSkus(true);
			productLoadTuner.setLoadingDefaultSku(true);
		}
			return productService.findByUids(uidList, productLoadTuner);
	}
}
