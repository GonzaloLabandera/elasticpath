/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.impl;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.rules.RuleElement;
import com.elasticpath.domain.rules.RuleException;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.PromotionConfigureService;
import com.elasticpath.service.catalog.BrandService;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.catalog.ProductSkuService;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Service to help configure promotion rules.
 */
public class PromotionConfigureServiceImpl implements PromotionConfigureService {

	private ProductLookup productLookup;

	private ProductSkuService productSkuService;

	private BrandService brandService;

	private CategoryLookup categoryLookup;

	private ShippingServiceLevelService shippingServiceLevelService;

	/**
	 * Returns <code>RuleParameter</code> defined by key.
	 *
	 * @param element element in which we should find <code>RuleParameter</code>.
	 * @param key key parameter that defines concrete <code>RuleParameter</code> in <code>RuleElement</code>.
	 * @return <code>RuleParameter</code> by key or null if it doesn't exists in <code>RuleElement</code>.
	 */
	@Override
	public RuleParameter retrieveRuleParameterByKey(final RuleElement element, final String key) {
		for (RuleParameter parameter : element.getParameters()) {
			if (parameter.getKey().equals(key)) {
				return parameter;
			}
		}
		throw new EpServiceException("Rule parameter with the key " + key + " doesn't exist in the given ruleElement");
	}

	/**
	 * Returns <code>RuleParameter</code> defined by key.
	 *
	 * @param exception rule exception in which we should find <code>RuleParameter</code>
	 * @param key key parameter that defines concrete <code>RuleParameter</code> in <code>RuleException</code>
	 * @return <code>RuleParameter</code> by key or null if it doesn't exists in <code>RuleException</code>.
	 */
	@Override
	public RuleParameter retrieveRuleParameterByKey(final RuleException exception, final String key) {
		for (RuleParameter parameter : exception.getParameters()) {
			if (parameter.getKey().equals(key)) {
				return parameter;
			}
		}
		throw new EpServiceException("Rule parameter with the key " + key + " doesn't exist in the given ruleException");
	}

	/**
	 * Convert product GUID into product UIDPK.
	 *
	 * @param value product GUID string value
	 * @return product UIDPK string representation
	 */
	@Override
	public String getProductIdValue(final String value) {
		Product product = productLookup.findByGuid(value);
		if (product == null) {
			throw new EpServiceException("Product with code " + value + " doesn't exists in DB.");
		}
		return product.getCode();
	}

	/**
	 * Check if a product sku with the given SKU CODE exists in database.
	 *
	 * @param skuCode sku CODE
	 * @return string representation of product UIDPK
	 */
	@Override
	public String getSkuCodeValue(final String skuCode) {
		long skuUid = productSkuService.findUidBySkuCode(skuCode);
		if (skuUid == 0) {
			throw new EpServiceException("Product Sku with sku code " + skuCode + " doesn't exists in DB.");
		}
		return skuCode;
	}

	/**
	 * Check if a brand with the given CODE exists in database.
	 *
	 * @param value brand CODE
	 * @return brand CODE
	 */
	@Override
	public String getBrandIdValue(final String value) {
		Brand brand = brandService.findByCode(value);
		if (brand == null) {
			throw new EpServiceException("Brand with brande code " + value + " doesn't exists in DB.");
		}
		return brand.getCode();
	}

	/**
	 * Convert category GUID into category UIDPK.
	 *
	 * @param value category CODE (or GUID which is the same for category)
	 * @param catalog to find the category into
	 * @return string representation of category UIDPK
	 */
	@Override
	public String getCategoryIdValue(final String value, final Catalog catalog) {
		final Category category = categoryLookup.findByCategoryCodeAndCatalog(value, catalog);
		if (null == category) {
			throw new EpServiceException("Category with code " + value + " doesn't exist");
		}
		return category.getCompoundGuid();
	}

	/**
	 * Convert shipping service level display name into shipping service level UIDPK.
	 *
	 * @param value shipping service level display name
	 * @param store the store shipping service level is used by
	 * @return string representation of shipping service level UIDPK
	 */
	@Override
	public String getShippingServiceLevelIdValue(final String value, final Store store) {
		List<ShippingServiceLevel> methodList = shippingServiceLevelService.findByStore(store.getCode());
		for (ShippingServiceLevel shippingServiceLevel : methodList) {
			if (shippingServiceLevel.getDisplayName(store.getDefaultLocale(), true).equals(value)) {
				return shippingServiceLevel.getCode();
			}
		}
		throw new EpServiceException("Shipping service method " + value + " is not supported by store " + store.getName());
	}

	public void setProductLookup(final ProductLookup productLookup) {
		this.productLookup = productLookup;
	}

	public void setProductSkuService(final ProductSkuService productSkuService) {
		this.productSkuService = productSkuService;
	}

	public void setBrandService(final BrandService brandService) {
		this.brandService = brandService;
	}

	public void setCategoryLookup(final CategoryLookup categoryLookup) {
		this.categoryLookup = categoryLookup;
	}

	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}
}
