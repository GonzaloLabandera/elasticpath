/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.Transient;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.exception.EpProductInUseException;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CyclicBundleException;
import com.elasticpath.domain.catalog.InvalidBundleConstituentPricingMechanism;
import com.elasticpath.domain.catalog.InvalidBundleProductTypeException;
import com.elasticpath.domain.catalog.InvalidBundleSelectionRuleException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.dao.ProductBundleDao;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.service.catalog.BundleValidator;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.ProductBundleService;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.ProductSkuService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.pricing.dao.BaseAmountDao;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryResult;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.BrandRelation;
import com.elasticpath.service.query.relations.CategoryRelation;
import com.elasticpath.service.query.relations.ProductRelation;
import com.elasticpath.service.query.relations.ProductSkuRelation;
import com.elasticpath.service.query.relations.StoreRelation;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * The default implementation of <code>ProductService</code>.
 *
 * Do not add findXXX methods to this class. The ProductQueryService should be used instead
 * as this allows flexible queries using the Query Object Pattern and Builder Pattern.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveClassLength", "PMD.GodClass" })
public class ProductServiceImpl implements ProductService {

	private CategoryLookup categoryLookup;
	private CategoryService categoryService;

	private ProductDao productDao;

	private BaseAmountDao baseAmountDao;

	private ProductBundleDao productBundleDao;
	private ProductBundleService productBundleService;

	private BundleValidator bundleValidator;

	private ProductSkuService productSkuService;

	private IndexNotificationService indexNotificationService;

	private QueryService<Product> queryService;

	private TimeService timeService;

	private BeanFactory beanFactory;
	private ProductLookup productLookup;

	/**
	 * Set a <code>ProductCategory</code> as a featured product, if the featuredProductOrder is set to be greater than 0, means this
	 * productCategory is featured.
	 *
	 * @param productUid  the unique identifier for the product
	 * @param categoryUid the unique identifier for the category
	 * @return featuredProductOrder the featured product order of the <code>ProductCategory</code>
	 */
	@Override
	public int setProductCategoryFeatured(final long productUid, final long categoryUid) {
		final Product product = getProductLookup().findByUid(productUid);
		final int maxFeaturedProductOrder = productDao.getMaxFeaturedProductOrder(categoryUid);
		Category categoryToFeatureProduct = getCategoryFromProductByUid(product, categoryUid);
		if (categoryToFeatureProduct == null) {
			categoryToFeatureProduct = getCategoryLookup().findByUid(categoryUid);
			product.addCategory(categoryToFeatureProduct);
		}

		final int newFeaturedProductOrder = maxFeaturedProductOrder + 1;
		product.setFeaturedRank(categoryToFeatureProduct, newFeaturedProductOrder);
		productDao.saveOrUpdate(product);
		return newFeaturedProductOrder;
	}

	/**
	 * Remove the "featured" status of a given product in a given category.
	 * This implementation calls {@link #getCategoryFromProductByUid(Product, long)}.
	 *
	 * @param productUid the unique identifier for the product
	 * @param categoryUid the unique identifier for the category
	 */
	@Override
	public void resetProductCategoryFeatured(final long productUid, final long categoryUid) {
		final Product product = getProductLookup().findByUid(productUid);
		final Category category = getCategoryFromProductByUid(product, categoryUid);
		product.setFeaturedRank(category, 0);
		productDao.saveOrUpdate(product);
	}

	/**
	 * Gets a category from a product given the category Uid.
	 *
	 * @param product the product
	 * @param categoryUid the unique identifier of the category
	 * @return the category
	 */
	public Category getCategoryFromProductByUid(final Product product, final long categoryUid) {
		for (final Category category : product.getCategories()) {
			if (category.getUidPk() == categoryUid) {
				return category;
			}
		}
		return null;
	}

	/**
	 * Swap two <code>ProductCategory</code> featured product order.
	 *
	 * @param productUid the unique identifier for the product
	 * @param categoryUid the unique identifier for the category
	 * @param productUid2 the unique identifier for the product to be swapped
	 * @throws EpServiceException if a category with the given uid doesn't exist in both of the given products
	 */
	@Override
	public void updateFeaturedProductOrder(final long productUid, final long categoryUid, final long productUid2) {
		final Product product = getProductLookup().findByUid(productUid);
		final Product product2 = getProductLookup().findByUid(productUid2);

		final Category category = getCategoryFromProductByUid(product, categoryUid);
		if (category == null || !product2.getCategories().contains(category)) {
			throw new EpServiceException("Category with uid=" + categoryUid
					+ " must exist in productUid= " + productUid + " and productUid=" + productUid2);
		}
		//Save the index from product1
		final int tempOrder = product.getFeaturedRank(category);
		//Set the product1 index to the product2 index
		product.setFeaturedRank(category, product2.getFeaturedRank(category));
		//Set the product2 index to the old product1 index
		product2.setFeaturedRank(category, tempOrder);

		productDao.saveOrUpdate(product);
		productDao.saveOrUpdate(product2);
	}

	/**
	 * Notifies the product service that a <code>ProductType</code> has been updated.
	 *
	 * @param productType the productType that was updated
	 */
	@Override
	public void notifyProductTypeUpdated(final ProductType productType) {
		productDao.updateProductLastModifiedTime(productType);
	}

	/**
	 * Notifies the product service that a <code>ProductSku</code> has been updated.
	 *
	 * @param productSku the productSku that was updated
	 */
	@Override
	public void notifySkuUpdated(final ProductSku productSku) {
		productDao.updateProductLastModifiedTime(productSku.getProduct());
	}

	/**
	 * Notifies the product service that a <code>Brand</code> has been updated.
	 *
	 * @param brand the brand that was updated
	 */
	@Override
	public void notifyBrandUpdated(final Brand brand) {
		productDao.updateProductLastModifiedTime(brand);
	}

	/**
	 * Notifies the product service that a <code>Category</code> has been updated.
	 *
	 * @param category the category that was updated
	 *
	 */
	@Override
	public void notifyCategoryUpdated(final Category category) {
		final List<Long> categoryUids = new ArrayList<>();
		categoryUids.add(category.getUidPk());
		final List<Long> affectedProductUids = productDao.findUidsByCategoryUids(categoryUids);
		productDao.updateLastModifiedTimes(affectedProductUids);
	}

	@Override
	public boolean canDelete(final Product product) {
		if (isInBundle(product)) {
			return false;
		}

		for (final ProductSku productSku : product.getProductSkus().values()) {
			if (!productSkuService.canDelete(productSku)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * TODO - This should be provided by the ProductCharacteristicsService.
	 */
	@Override
	public boolean isInBundle(final Product product) {
		if (!productBundleDao.findByProduct(product.getCode()).isEmpty()) {
			return true;
		}

		for (final ProductSku sku : product.getProductSkus().values()) {
			if (productSkuService.isInBundle(sku)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Set the product DAO to use.
	 * @param productDao the productDao to set
	 */
	public void setProductDao(final ProductDao productDao) {
		this.productDao = productDao;
	}

	/**
	 * Set the <code>CategoryService</code> singleton.
	 *
	 * @param categoryService the <code>CategoryService</code> singleton.
	 */
	public void setCategoryService(final CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	/**
	 * Gets the product with the given UID. Return null if no matching records exist. You can
	 * given a fetch group load tuner to fine control what data to get populated of the returned
	 * product.
	 *
	 * @param productUid the product UID
	 * @param loadTuner the load tuner user to fine tune what is loaded.
	 * @return the product if it exists, otherwise null
	 * @throws EpServiceException in case of any errors
	 *
	 */
	@Override
	public Product getTuned(final long productUid, final FetchGroupLoadTuner loadTuner) throws EpServiceException {
		return productDao.getTuned(productUid, loadTuner);
	}

	/**
	 * Get the product with the given UID. Return null if no matching record exists. You can give a product load tuner to fine control what data get
	 * populated of the returned product.
	 *
	 * @param productUid the Product UID.
	 * @param loadTuner the product load tuner
	 * @return the product if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 *
	 */
	@Override
	public Product getTuned(final long productUid, final ProductLoadTuner loadTuner) throws EpServiceException {
		return productDao.getTuned(productUid, loadTuner);
	}

	/**
	 * Retrieve the list of top selling products that belongs to the category or its subcategories.
	 *
	 * @param categoryUid the category id
	 * @param topCount the number of top seller products to retrieve
	 * @return the list of top selling products that belongs to the category or its subcategories
	 * @throws EpServiceException in case of any error
	 */
	@Override
	public List<Product> findProductTopSellerForCategory(final long categoryUid, final int topCount) throws EpServiceException {
		final List<Long> categoryUids = categoryService.findDescendantCategoryUids(categoryUid);
		categoryUids.add(Long.valueOf(categoryUid));

		return productDao.findProductTopSellerForCategories(categoryUids, topCount);
	}

	/**
	 * Retrieve a list of top sellers of the whole store.
	 *
	 * @param topCount the number of top seller products to retrieve
	 * @param productLoadTuner the product load tunner to control data get loaded
	 * @return the list of top sellers of the whole store
	 * @throws EpServiceException in case of any error
	 */
	@Override
	public List<Product> findProductTopSeller(final int topCount, final ProductLoadTuner productLoadTuner) throws EpServiceException {
		return productDao.findProductTopSeller(topCount, productLoadTuner);
	}

	/**
	 * Save or update the given product.
	 *
	 * @param toSave the product to save or update
	 * @return the merged object if it is merged, or the persisted object for save action
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Product saveOrUpdate(final Product toSave) throws EpServiceException {
		final boolean isUpdate = toSave.isPersisted();

		if (toSave instanceof ProductBundle) {
			validate((ProductBundle) toSave);
		}

		final Product persistedProduct = productDao.saveOrUpdate(toSave);

		validate(persistedProduct);

		Set<Long> containingBundleUids = Collections.emptySet();
		if (isUpdate) {
			containingBundleUids = getProductBundleService().findAllProductBundleUidsContainingProduct(persistedProduct);
		}

		indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.PRODUCT, persistedProduct.getUidPk());

		for (final Long bundleUid : containingBundleUids) {
			indexNotificationService.addNotificationForEntityIndexUpdate(IndexType.PRODUCT, bundleUid);
		}

		return persistedProduct;
	}

	/**
	 * @param product to validate
	 */
	protected void validate(final ProductBundle product) {
		if (product.getProductType().isMultiSku()) {
			throw new InvalidBundleProductTypeException();
		}

		final ProductBundle bundle = product;
		if (!isBundleSelectionRuleValid(bundle)) {
			throw new InvalidBundleSelectionRuleException(bundle.getSelectionRule());
		}

		final Product constituent = getCyclicDependency(bundle);
		if (constituent != null) {
			throw new CyclicBundleException(bundle, constituent);
		}

		if (!areAllBundleConstituentsOfTheSamePricingMechanismType(bundle)) {
			throw new InvalidBundleConstituentPricingMechanism();
		}

	}

	/**
	 * Validate the integrity of the product and the existence of required fields.
	 * throws exception if the value for required attributes are missing
	 *
	 * @param product - the product to validate
	 */
	protected void validate(final Product product) {
		// validation for attribute - apply to product & bundles
		final Set<Locale> allLocalesSupported = getAllLocalesSupportedByTheCatalogsThatContainTheProduct(product);
		product.validateRequiredAttributes(allLocalesSupported);
	}

	private  Set<Locale> getAllLocalesSupportedByTheCatalogsThatContainTheProduct(final Product product) {
		// the value is required only for the master catalog default
		return new HashSet<>(Arrays.asList(product.getMasterCatalog().getDefaultLocale()));
	}

	/**
	 * Check that the selection rule on the bundle isn't more than the bundle's number of constituents.
	 * @param bundle the bundle
	 * @return true if SelectionRule is valid, or doesn't apply
	 */
	protected boolean isBundleSelectionRuleValid(final ProductBundle bundle) {
		return bundleValidator.isBundleSelectionRuleValid(bundle);
	}

	/**
	 * Searches the given product's bundle constituent tree to
	 * find any cyclical references and returns the offending constituent.
	 * @param bundle the root product bundle
	 * @return the consituent that contains a cyclical reference, or null if there
	 * are no cyclical dependencies or if the given product is not a bundle.
	 */
	protected Product getCyclicDependency(final ProductBundle bundle) {
		return bundleValidator.getCyclicDependency(bundle);
	}

	/**
	 * Checks if all bundle constituents have the same pricing mechanism type and the root bundle.
	 * @param bundle the product bundle
	 * @return true if all bundle constituents have the same pricing mechanism type and the root bundle
	 */
	protected boolean areAllBundleConstituentsOfTheSamePricingMechanismType(final ProductBundle bundle) {
		return bundleValidator.areAllBundleConstituentsOfTheSamePricingMechanismType(bundle);
	}

	/**
	 * {@inheritDoc}
	 * This implementation uses the ProductDao to remove the Product and its associations,
	 * and calls {@link ProductDao#removeProductTree(long)} to remove all of the prices.
	 */
	@Override
	public void removeProductTree(final long productUid) throws EpServiceException {
		final Product product = getProductLookup().findByUid(productUid);
		if (!canDelete(product)) {
			throw new EpProductInUseException("Product is being used!");
		}

		productDao.removeProductTree(productUid);
	}

	/**
	 * Retrieves list of <code>Product</code> where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Product</code> whose last modified date is later than the specified date
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByModifiedDate(final Date date) {
		QueryResult<Product> queryResult = getQueryService().query(
				productCriteria().modifiedAfter(date).returning(ResultType.ENTITY));

		return queryResult.getResults();
	}

	/**
	 * Retrieves list of product uids where the deleted date is later than the specified date.
	 *
	 * @param date date to compare with the deleted date
	 * @return list of product uids whose deleted date is later than the specified date
	 *
	 *
	 */
	@Override
	public List<Long> findUidsByDeletedDate(final Date date) {
		return productDao.findUidsByDeletedDate(date);
	}

	/**
	 * Returns a list of <code>Product</code> based on the given uids. The returned products will be populated based on the given load tuner.
	 *
	 * @param productUids a collection of product uids
	 * @param loadTuner the load tuner
	 * @return a list of <code>Product</code>s
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByUids(final Collection<Long> productUids, final ProductLoadTuner loadTuner) {
		return findByUidsWithLoadTuner(productUids, loadTuner);
	}

	/**
	 * Returns a list of <code>Product</code> based on the given uids. The returned products will be populated based on the given load tuner.
	 *
	 * @param productUids a collection of product uids
	 * @param loadTuner the load tuner
	 * @return a list of <code>Product</code>s
	 */
	protected List<Product> findByUidsWithLoadTuner(final Collection<Long> productUids, final LoadTuner loadTuner) {
		QueryResult<Product> queryResult = getQueryService().query(productCriteria().with(
				ProductRelation.having().uids(productUids)).usingLoadTuner(loadTuner).returning(ResultType.ENTITY));

		return queryResult.getResults();
	}

	/**
	 * Returns a list of <code>Product</code> based on the given uids.
	 * The returned products will be populated based on the given {@link FetchGroupLoadTuner}.
	 *
	 * @param productUids a collection of product uids
	 * @param fetchGroupLoadTuner the load tuner
	 * @return a list of <code>Product</code>s
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByUidsWithFetchGroupLoadTuner(final Collection<Long> productUids, final FetchGroupLoadTuner fetchGroupLoadTuner) {
		return findByUidsWithLoadTuner(productUids, fetchGroupLoadTuner);
	}

	/**
	 * Find the enriching data within given named query.
	 * @param queryName name of named query
	 * @param guids collection of product codes
	 * @param locale locale
	 * @return list of object array, that represent selected by named query fileds.
	 */
	@Override
	public List<Object[]> findEnrichingData(final String queryName, final Collection<String> guids,
											final Locale locale) {
		return productDao.findEnrichingData(queryName, guids, locale);
	}

	/**
	 * Returns all product uids as a list.
	 *
	 * @return all product uids as a list
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Long> findAllUids() {
		QueryResult<Long> queryResult = getQueryService().query(productCriteria().returning(ResultType.UID));
		return queryResult.getResults();
	}

	/**
	 * Returns a list of <code>Product</code> based on the given brand Uid.
	 *
	 * @param brandUid the brand Uid
	 * @return a list of <code>Product</code>
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByBrandUid(final long brandUid) {
		QueryResult<Product> result = getQueryService().query(
				productCriteria().with(BrandRelation.having().uids(brandUid)).returning(ResultType.ENTITY));
		return result.getResults();
	}

	/**
	 * Returns a list of <code>Product</code> based on the given category Uid.
	 *
	 * @param categoryUid the category Uid
	 * @return a list of <code>Product</code>
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByCategoryUid(final long categoryUid) {
		QueryResult<Product> queryResult = getQueryService().query(
				productCriteria().with(CategoryRelation.having().uids(categoryUid)).returning(ResultType.ENTITY));
		return queryResult.getResults();
	}

	/**
	 * Returns a list of <code>Product</code> based on the given category Uid.
	 *
	 * @param categoryUid the category Uid
	 * @param loadTuner the load tuner
	 * @return a list of <code>Product</code>
	 */
	protected List<Product> findByCategoryUidWithTuner(final long categoryUid, final LoadTuner loadTuner) {
		QueryResult<Product> queryResult = getQueryService().query(productCriteria()
				.with(CategoryRelation.having().uids(categoryUid)).usingLoadTuner(loadTuner).returning(ResultType.ENTITY));
		return queryResult.getResults();
	}

	/**
	 * Returns a collection of {@link Product}s that are directly within the given
	 * {@code categoryUid}. Give a load tuner (or {@code null} to use the default load tuner) to
	 * tune the result set.
	 *
	 * @param categoryUid the category UID to retrieve products for
	 * @param loadTuner the load tuner to use or {@code null}
	 * @return a collection of {@link Product}s directly within the category
	 * @throws EpServiceException in case of any errors
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public Collection<Product> findByCategoryUid(final long categoryUid, final FetchGroupLoadTuner loadTuner) {
		return findByCategoryUidWithTuner(categoryUid, loadTuner);
	}

	/**
	 * Returns a list of <code>Product</code> based on the given category Uid. The returned products will be populated based on the given load
	 * tuner.
	 *
	 * @param categoryUid the category Uid
	 * @param loadTuner the load tuner, give <code>null</code> to populate all related data
	 * @return a list of <code>Product</code>
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByCategoryUid(final long categoryUid, final ProductLoadTuner loadTuner) {
		return findByCategoryUidWithTuner(categoryUid, loadTuner);
	}

	/**
	 * Quick check if category contains any Products without loading products.
	 *
	 * @param categoryUid the category uid
	 * @return has products in category
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public boolean hasProductsInCategory(final long categoryUid) {
		QueryResult<Boolean> result = getQueryService().query(
				productCriteria().with(CategoryRelation.having().uids(categoryUid)).returning(ResultType.CONDITIONAL));

		return result.getSingleResult();
	}

	/**
	 * Returns a list of <code>Product</code>s based on the given category Uid. The returned products will be populated based on the given load
	 * tuner. This method allows a subset of the products to be returned at a time by giving the starting index of the first product and the number
	 * of products to be returned.
	 *
	 * @param categoryUid the category Uid
	 * @param startIndex the starting index of the first product to be returned
	 * @param numProducts the number of products to be returned, starting from the start index
	 * @param loadTuner the load tuner, give <code>null</code> to populate all related data
	 * @return a list of <code>Product</code>s
	 *
	 */
	@Override
	public List<Product> findByCategoryUidPaginated(final long categoryUid, final int startIndex, final int numProducts,
													final ProductLoadTuner loadTuner) {
		return productDao.findByCategoryUidPaginated(categoryUid, startIndex, numProducts, loadTuner);
	}

	/**
	 * Returns a list of <code>Product</code> based on the given brand Uid. The returned products will be populated based on the given load tuner.
	 *
	 * @param brandUid the brand Uid
	 * @param loadTuner the load tuner, give <code>null</code> to populate all related data
	 * @return a list of <code>Product</code>
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Product> findByBrandUid(final long brandUid, final ProductLoadTuner loadTuner) {
		QueryResult<Product> queryResult = getQueryService().query(productCriteria()
				.with(BrandRelation.having().uids(brandUid)).usingLoadTuner(loadTuner).returning(ResultType.ENTITY));
		return queryResult.getResults();
	}

	/**
	 * Returns all available product uids as a list.
	 *
	 * @return all available product uids as a list
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Long> findAvailableUids() {
		final Date now = getTimeService().getCurrentTime();

		QueryResult<Long> queryResult = getQueryService().query(productCriteria()
				.inDateRange(now, now).returning(ResultType.UID));
		return queryResult.getResults();
	}

	/**
	 * Retrieves list of <code>Product</code> uids where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Product</code> whose last modified date is later than the specified date
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Long> findAvailableUidsByModifiedDate(final Date date) {
		final Date now = getTimeService().getCurrentTime();

		QueryResult<Long> queryResult = getQueryService().query(productCriteria().modifiedAfter(date)
				.inDateRange(now, now).returning(ResultType.UID));
		return queryResult.getResults();
	}

	/**
	 * Retrieves list of <code>Product</code> uids where the last modified date is later than the specified date.
	 *
	 * @param date date to compare with the last modified date
	 * @return list of <code>Long</code> whose last modified date is later than the specified date
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Long> findUidsByModifiedDate(final Date date) {
		QueryResult<Long> queryResult = getQueryService().query(productCriteria().modifiedAfter(date)
				.returning(ResultType.UID));
		return queryResult.getResults();
	}

	/**
	 * Get the product UID of the given product identifier. The given product identifier will frist be dealt as a guid to try to find a product UID.
	 * It no product UID is found is found 0 will be returned.
	 *
	 * @param productId the Product Guid or UID.
	 * @return the product UID, otherwise 0
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public long findUidById(final String productId) throws EpServiceException {
		return productDao.findUidById(productId);
	}

	@Override
	public Map<Long, String> findCodesByUids(final List<Long> productUids) {
		return productDao.findCodesByUids(productUids);
	}

	/**
	 * Checks whether the given product guid exists or not, for product, i.e. product code.
	 *
	 * @param guid the product code.
	 * @return true if the given guid(code) exists.
	 * @throws EpServiceException - in case of any errors
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public boolean guidExists(final String guid) throws EpServiceException {
		QueryResult<Boolean> result = getQueryService().query(productCriteria().with(ProductRelation.having().codes(guid))
				.returning(ResultType.CONDITIONAL));

		return result.getSingleResult();
	}

	/**
	 * Deletes the list of products.
	 *
	 * @param productUidList the product Uid List to be removed
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public void removeProductList(final List<Long> productUidList) throws EpServiceException {
		productDao.removeProductList(productUidList);
	}

	/**
	 * Retrieves list of product uids belongs to either category uids given.
	 *
	 * @param categoryUids category uids
	 * @return list of product uids
	 */
	@Override
	public List<Long> findUidsByCategoryUids(final Collection<Long> categoryUids) {
		return productDao.findUidsByCategoryUids(categoryUids);
	}

	/**
	 * Get a count of <code>ProductSku</code>s belong to this product.
	 *
	 * @param productUid the uid of the product.
	 * @return a count of <code>ProductSku</code>s belong to this product.
	 *
	 */
	@Override
	public int getProductSkuCount(final long productUid) {
		return productDao.getProductSkuCount(productUid);
	}


	/**
	 * Check with database to find maximum featured product order for the given category.
	 * @param categoryUid the categoryUid of the give category.
	 * @return the maximum featured product order number.
	 */
	@Override
	public int getMaxFeaturedProductOrder(final long categoryUid) {
		return productDao.getMaxFeaturedProductOrder(categoryUid);
	}

	/**
	 * Update the product's last modified timestamp.
	 *
	 * @param product the product whose timestamp is to be updated.
	 * @deprecated Use saveOrUpdate which updates the last modified time anyway.
	 */
	@Override
	@Deprecated
	public void updateLastModifiedTime(final Product product) {
		productDao.saveOrUpdate(product);
	}

	/**
	 * Retrieves list of product UIDs that belongs to the given store UID.
	 *
	 * @param storeUid the store UID
	 * @return list of product UIDs
	 * @deprecated Use {@link QueryService} directly instead.
	 */
	@Override
	@Deprecated
	public List<Long> findUidsByStoreUid(final long storeUid) {
		QueryResult<Long> queryResult = getQueryService().query(productCriteria()
				.with(StoreRelation.having().uids(storeUid)).returning(ResultType.UID));
		return queryResult.getResults();
	}

	/**
	 * Retrieve all descendant bundle UIDs of the given constituent UIDs.
	 *
	 * @param constituentUids the constituent UIDs.
	 * @return the list of UIDs of the direct and indirect parent bundle of the given start
	 *         constituent UIDs.
	 *
	 */
	@Override
	public List<Long> findBundleUids(final List<Long> constituentUids) {
		return productBundleDao.findBundleUids(constituentUids);
	}

	/**
	 * @param validator BundleValidator
	 */
	public void setBundleValidator(final BundleValidator validator) {
		bundleValidator = validator;
	}

	/**
	 * Get guids of <code>ProductSku</code>s belonging to this product.
	 *
	 * @param productGuid the guid of the product.
	 * @return guids of <code>ProductSku</code>s belonging to this product.
	 *
	 */
	@Override
	public List<String> getProductSkuGuids(final String productGuid) {
		return productDao.getProductSkuGuids(productGuid);
	}

	@Override
	@Deprecated
	public long findUidBySkuCode(final String skuCode) {
		QueryResult<Long> queryResult = getQueryService().query(productCriteria()
				.with(ProductSkuRelation.having().codes(skuCode)).returning(ResultType.UID));
		Long result = queryResult.getSingleResult();
		if (result == null) {
			result = 0L;
		}
		return result;
	}

	@Transient
	@Override
	public boolean isInCategory(final Product product, final String compoundCategoryGuid) {
		for (Category category : product.getCategories()) {
			if (isInCategoryHierarchy(compoundCategoryGuid, category)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the specified category is in another category or one of its parents.
	 *
	 * @param searchCategoryCode the category code to search for in the category heirarchy
	 * @param categoryHierarchy the category hierarchy to search for the searchCategory
	 * @return true if searchCategory is found in categoryHierarchy
	 */
	@Transient
	private boolean isInCategoryHierarchy(final String searchCategoryCode, final Category categoryHierarchy) {
		if (categoryHierarchy == null) {
			return false;
		}
		if (searchCategoryCode.equals(categoryHierarchy.getCompoundGuid())) {
			return true;
		}
		return isInCategoryHierarchy(searchCategoryCode, getCategoryLookup().findParent(categoryHierarchy));
	}

	/**
	 * @param baseAmountDao the baseAmountDao to set
	 */
	public void setBaseAmountDao(final BaseAmountDao baseAmountDao) {
		this.baseAmountDao = baseAmountDao;
	}

	/**
	 * @return the baseAmountDao
	 */
	public BaseAmountDao getBaseAmountDao() {
		return baseAmountDao;
	}

	/**
	 * Sets the product bundle dao.
	 *
	 * @param productBundleDao a product bundle dao
	 */
	public void setProductBundleDao(final ProductBundleDao productBundleDao) {
		this.productBundleDao = productBundleDao;
	}

	/**
	 * Gets the product bundle dao.
	 *
	 * @return {@link ProductBundleDao}
	 */
	protected ProductBundleDao getProductBundleDao() {
		return productBundleDao;
	}

	protected ProductSkuService getProductSkuService() {
		return productSkuService;
	}

	/**
	 * @param productSkuService Product Sku service to use
	 */
	public void setProductSkuService(final ProductSkuService productSkuService) {
		this.productSkuService = productSkuService;
	}

	protected ProductBundleService getProductBundleService() {
		return productBundleService;
	}

	public void setProductBundleService(final ProductBundleService productBundleService) {
		this.productBundleService = productBundleService;
	}

	/**
	 * @param indexNotificationService instance to use
	 */
	public void setIndexNotificationService(final IndexNotificationService indexNotificationService) {
		this.indexNotificationService = indexNotificationService;
	}

	public void setQueryService(final QueryService<Product> queryService) {
		this.queryService = queryService;
	}

	protected QueryService<Product> getQueryService() {
		return queryService;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	protected TimeService getTimeService() {
		return timeService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Get the criteria builder for a product.
	 *
	 * @return a product criteria builder
	 */
	protected CriteriaBuilder<Product> productCriteria() {
		return CriteriaBuilder.criteriaFor(Product.class);
	}

	protected ProductLookup getProductLookup() {
		return productLookup;
	}

	public void setProductLookup(final ProductLookup productLookup) {
		this.productLookup = productLookup;
	}

	protected CategoryLookup getCategoryLookup() {
		return categoryLookup;
	}

	public void setCategoryLookup(final CategoryLookup categoryLookup) {
		this.categoryLookup = categoryLookup;
	}
}
