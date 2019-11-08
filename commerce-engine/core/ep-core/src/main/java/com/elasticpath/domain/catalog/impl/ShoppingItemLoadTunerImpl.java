/**
 * Copyright (c) Elastic Path Software Inc., 2009.
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.DEFAULT_ASSOCIATION_QUANTITY;
import static com.elasticpath.persistence.support.FetchFieldConstants.DEFAULT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.DEPENDENT_ITEMS_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.GUID;
import static com.elasticpath.persistence.support.FetchFieldConstants.PARENT_ITEM;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRICE;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKUS_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.QUANTITY_INTERNAL;

import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.catalog.ShoppingItemLoadTuner;
import com.elasticpath.domain.impl.AbstractEpDomainImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * Load tuner for a shopping item.
 */
public class ShoppingItemLoadTunerImpl extends AbstractEpDomainImpl implements ShoppingItemLoadTuner {

	private static final long serialVersionUID = 1L;

	private Boolean loadingRecursiveDependentItems = false;

	private Boolean loadingDefaultAssociationQuantity = false;

	private Boolean loadingDependentItems = false;

	private Boolean loadingParentItem = false;

	private Boolean loadingProductSku = false;

	private Boolean loadingQuantity = false;

	private Boolean loadingPrice = false;

	@Override
	public Boolean isLoadingRecursiveDependentItems() {
		return loadingRecursiveDependentItems;
	}

	@Override
	public Boolean isLoadingDefaultAssociationQuantity() {
		return loadingDefaultAssociationQuantity;
	}

	@Override
	public Boolean isLoadingDependentItems() {
		return loadingDependentItems;
	}

	@Override
	public Boolean isLoadingParentItem() {
		return loadingParentItem;
	}

	@Override
	public Boolean isLoadingProductSku() {
		return loadingProductSku;
	}

	@Override
	public Boolean isLoadingQuantity() {
		return loadingQuantity;
	}

	@Override
	public Boolean isLoadingPrice() {
		return loadingPrice;
	}

	@Override
	public void setLoadingDefaultAssociationQuantity(final Boolean flag) {
		loadingDefaultAssociationQuantity = flag;
	}

	@Override
	public void setLoadingDependentItems(final Boolean flag) {
		loadingDependentItems = flag;
	}

	@Override
	public void setLoadingParentItem(final Boolean flag) {
		loadingParentItem = flag;
	}

	@Override
	public void setLoadingProductSku(final Boolean flag) {
		loadingProductSku = flag;
	}

	@Override
	public void setLoadingQuantity(final Boolean flag) {
		loadingQuantity = flag;
	}

	@Override
	public void setLoadingRecursiveDependentItems(final Boolean flag) {
		loadingRecursiveDependentItems = flag;
	}

	@Override
	public void setLoadingPrice(final Boolean flag) {
		loadingPrice = flag;
	}

	@Override
	public boolean contains(final LoadTuner loadTuner) {
		return false;
	}

	@Override
	public LoadTuner merge(final LoadTuner loadTuner) {
		return this;
	}

	@Override
	public void configure(final FetchPlan fetchPlan) {
		// always load the following fields
		fetchPlan.addField(GUID);

		if (isLoadingRecursiveDependentItems()) {
			fetchPlan.addFetchGroup(FetchGroupConstants.SHOPPING_ITEM_CHILD_ITEMS);
		}
		if (isLoadingDefaultAssociationQuantity()) {
			fetchPlan.addField(ShoppingItemImpl.class, DEFAULT_ASSOCIATION_QUANTITY);
		}
		if (isLoadingQuantity()) {
			fetchPlan.addField(ShoppingItemImpl.class, QUANTITY_INTERNAL);
		}
		if (isLoadingDependentItems()) {
			fetchPlan.addField(ShoppingItemImpl.class, DEPENDENT_ITEMS_INTERNAL);
		}
		if (isLoadingParentItem()) {
			fetchPlan.addField(ShoppingItemImpl.class, PARENT_ITEM);
		}
		if (isLoadingProductSku()) {
			fetchPlan.addField(ShoppingItemImpl.class, PRODUCT_SKU);
			fetchPlan.addField(ProductImpl.class, DEFAULT_SKU);
			fetchPlan.addField(ProductImpl.class, PRODUCT_SKUS_INTERNAL);
		}
		if (isLoadingPrice()) {
			fetchPlan.addField(ShoppingItemImpl.class, PRICE);
		}
	}
}
