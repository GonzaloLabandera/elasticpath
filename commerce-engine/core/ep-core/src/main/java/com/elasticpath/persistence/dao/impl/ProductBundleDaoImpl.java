/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.persistence.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.persistence.dao.ProductBundleDao;

/**
 * DAO implementation for {@link ProductBundle} objects.
 */
public class ProductBundleDaoImpl extends ProductDaoImpl implements ProductBundleDao {

	@Override
	public List<ProductBundle> findByGuids(final List<String> guids) throws DataAccessException {
		sanityCheck();
		if (guids == null || guids.isEmpty()) {
			return Collections.emptyList();
		}

		return getPersistenceEngine().retrieveByNamedQueryWithList("PRODUCT_BUNDLE_BY_GUIDS", "list", guids);
	}

	@Override
	public ProductBundle findByBundleConstituentGuid(final String guid) throws DataAccessException {
		sanityCheck();
		if (guid == null) {
			return null;
		}

		final List<ProductBundle> bundleList =
				getPersistenceEngine().retrieveByNamedQuery("PRODUCT_BUNDLE_BY_CONSTITUENT_GUID", guid);
		ProductBundle bundle = null;
		if (bundleList.size() == 1) {
			bundle = bundleList.get(0);
		} else if (bundleList.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate product code exist -- " + guid);
		}
		return bundle;
	}

	/**
	 * Temporarily implementation. {@inheritDoc}
	 */
	@Override
	public List<ProductBundle> findByGuids(final List<String> guids, final ProductLoadTuner productLoadTuner) throws DataAccessException {
		if (productLoadTuner == null) {
			throw new EpServiceException("ProductLoadTuner can't be null");
		}

		getFetchPlanHelper().configureLoadTuner(productLoadTuner);

		final List<ProductBundle> bundles = findByGuids(guids);

		getFetchPlanHelper().clearFetchPlan();

		return bundles;
	}

	/**
	 * Gets all the {@link ProductBundle}s in the system. Or empty list if none in the system.
	 *
	 * @return a list of {@link ProductBundle}
	 * @throws DataAccessException might throw a {@link DataAccessException}
	 */
	@Override
	public List<ProductBundle> getProductBundles() throws DataAccessException {
		sanityCheck();
		final List<ProductBundle> result = getPersistenceEngine().retrieveByNamedQuery("PRODUCT_BUNDLE_ALL");
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		return result;
	}

	/**
	 * Saves the given {@link ProductBundle} object to DB.
	 *
	 * @param bundle a {@link ProductBundle} to saved or updated
	 * @return the saved {@link ProductBundle}
	 * @throws DataAccessException might throw a {@link DataAccessException}
	 */
	@Override
	public ProductBundle saveOrUpdate(final ProductBundle bundle) throws DataAccessException {
		sanityCheck();
		bundle.setLastModifiedDate(getTimeService().getCurrentTime());

		return getPersistenceEngine().saveOrMerge(bundle);
	}

	@Override
	public List<ProductBundle> findByProduct(final String productCode) {
		sanityCheck();
		final List<ProductBundle> result = getPersistenceEngine().retrieveByNamedQuery("FIND_BUNDLES_BY_PRODUCT_CODE", productCode);
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		return result;
	}

	@Override
	public List<ProductBundle> findByProductSku(final String skuCode) {
		sanityCheck();
		final List<ProductBundle> result = getPersistenceEngine().retrieveByNamedQuery("FIND_BUNDLES_BY_PRODUCT_SKU_CODE", skuCode);
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		return result;
	}

	@Override
	public List<ProductBundle> findByProductSkusOfProduct(final String productCode) {
		sanityCheck();
		final List<ProductBundle> result =
				getPersistenceEngine().retrieveByNamedQuery("FIND_BUNDLES_CONTAINING_PRODUCT_SKU_BY_PRODUCT_CODE", productCode);
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		return result;
	}

	/**
	 * Retrieve all descendant bundle UIDs of the given constituent UIDs.
	 *
	 * @param constituentUids the constituent UIDs.
	 * @return the list of UIDs of the direct and indirect parent bundle of the given start
	 *         constituent UIDs.
	 */
	@Override
	public List<Long> findBundleUids(final List<Long> constituentUids) {
		if (constituentUids == null || constituentUids.isEmpty()) {
			return Collections.emptyList();
		}

		final List<Long> result = new ArrayList<>();
		List<Long> bundleUids = getPersistenceEngine().retrieveByNamedQueryWithList("PRODUCT_BUNDLE_UID_SELECT_BY_CONSTITUENT_UIDS",
				"list", constituentUids);
		while (!bundleUids.isEmpty()) {
			result.addAll(bundleUids);
			bundleUids = getPersistenceEngine().retrieveByNamedQueryWithList("PRODUCT_BUNDLE_UID_SELECT_BY_CONSTITUENT_UIDS",
					"list", bundleUids);
		}

		return result;
	}

	@Override
	public boolean bundleExistsWithGuid(final String guid) {
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", guid);
		final List<Long> results = getPersistenceEngine().retrieveByNamedQuery("PRODUCT_BUNDLE_EXISTS", parameters);
		return !results.isEmpty() && results.get(0) > 0;
	}

	@Override
	public String findBundleCodeBySkuCode(final String skuCode) {
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("skuCode", skuCode);
		final List<String> results = getPersistenceEngine().retrieveByNamedQuery("PRODUCT_BUNDLE_CODE_BY_SKUCODE", parameters);
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

}
