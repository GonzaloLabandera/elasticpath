/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.search.solr.query;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.exception.EpUnsupportedOperationException;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.query.EpEmptySearchCriteriaException;
import com.elasticpath.service.search.query.ProductAutocompleteSearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * A Product autocomplete query composer, based on ProductQueryComposerImpl.
 * Main requirement - to search a product with name which starts with search keyword.
 */
public class ProductAutocompleteQueryComposerImpl extends ProductQueryComposerImpl {

	@Override
	public Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		return super.composeFuzzyQueryInternal(searchCriteria, searchConfig);
	}

	@Override
	public Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {


		if (searchCriteria.getLocale() == null) {
			throw new EpServiceException("Locale not set on product search criteria");
		}

		ProductAutocompleteSearchCriteria productSearchCriteria = (ProductAutocompleteSearchCriteria) searchCriteria;
		final String searchText = productSearchCriteria.getSearchText();

		if (!isValidText(searchText)) {
			throw new EpEmptySearchCriteriaException("Search text not valid: [" + searchText + "]");
		}

		boolean hasSomeCriteria = false;
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		// split should always produce valid text
		for (String str : searchText.trim().toLowerCase().split("\\\\\\s")) {
			if (str.length() == 0) {
				continue;
			}

			hasSomeCriteria |= addSearchCriteriaForTerm(str, booleanQueryBuilder, searchCriteria, searchConfig);
		}

		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}
		// check in category and subcategories
		Query categoryQuery = checkForCategory(productSearchCriteria);
		if (categoryQuery != null) {
			booleanQueryBuilder.add(categoryQuery, Occur.MUST);
		}
		// query for enabled products only
		this.addTermForEnabledProducts(productSearchCriteria, booleanQueryBuilder, searchConfig);
		// query for active products only
		this.addTermForActiveOnly(productSearchCriteria, booleanQueryBuilder);

		return booleanQueryBuilder.build();
	}

	private boolean addSearchCriteriaForTerm(final String word, final BooleanQuery.Builder booleanQueryBuilder,
			final SearchCriteria searchCriteria, final SearchConfig searchConfig) {

		BooleanQuery.Builder booleanSubQueryBuilder = new BooleanQuery.Builder();

		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.PRODUCT_NAME,
				word, searchCriteria.getLocale(), searchConfig, booleanSubQueryBuilder, Occur.SHOULD, false);

		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.BRAND_NAME,
				word, searchCriteria.getLocale(), searchConfig, booleanSubQueryBuilder, Occur.SHOULD, false);

		booleanQueryBuilder.add(booleanSubQueryBuilder.build(), Occur.MUST);

		return hasSomeCriteria;

	}

	private boolean addTermForEnabledProducts(final ProductSearchCriteria productSearchCriteria,
			final BooleanQuery.Builder booleanQueryBuilder, final SearchConfig searchConfig) {

		if (productSearchCriteria.isDisplayableOnly()) {
			if (StringUtils.isBlank(productSearchCriteria.getStoreCode())) {
				throw new EpUnsupportedOperationException("StoreCode must be defined to include displayable products");
			}
			return addWholeFieldToQuery(getIndexUtility().createDisplayableFieldName(SolrIndexConstants.DISPLAYABLE,
					productSearchCriteria.getStoreCode()), String.valueOf(true), null, searchConfig, booleanQueryBuilder, Occur.MUST,
					false);
		}
		return false;
	}

	/**
	 * @param productSearchCriteria
	 */
	private Query checkForCategory(final ProductAutocompleteSearchCriteria productSearchCriteria) {

		BooleanQuery query = null;

		if (productSearchCriteria.getCategoryUid() != null) {

			Category category = getCategoryLookup().findByUid(productSearchCriteria.getCategoryUid());

			if (category != null) {
				// check for catalog code
				String catalogCode;
				if (productSearchCriteria.getCatalogCode() == null) {
					catalogCode = category.getCatalog().getCode();
				} else {
					catalogCode = productSearchCriteria.getCatalogCode();
				}
				BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
				// add parent category
				createQueryForCategoryAndSubCategories(booleanQueryBuilder, category, catalogCode);
				query = booleanQueryBuilder.build();
			}
		}

		return query;
	}

	/**
	 * @param booleanQueryBuilder boolean query
	 * @param category category object
	 * @param catalogCode catalog code
	 */
	private void createQueryForCategoryAndSubCategories(final BooleanQuery.Builder booleanQueryBuilder,
														final Category category, final String catalogCode) {
		// get category, just not lost a children categories
		Category testCategory = getCategoryLookup().findByUid(category.getUidPk());

		// create query
		Query categoryQuery = createQueryForCategory(testCategory, catalogCode);
		booleanQueryBuilder.add(categoryQuery, Occur.SHOULD);

		// add subcategories
		for (Category subCategory : getCategoryLookup().findChildren(testCategory)) {
			createQueryForCategoryAndSubCategories(booleanQueryBuilder, subCategory, catalogCode);
		}
	}

	/**
	 * @param category category object
	 * @param catalogCode catalog code
	 * @return query object
	 */
	private Query createQueryForCategory(final Category category, final String catalogCode) {
		return new TermQuery(
			new Term(this.getIndexUtility().createProductCategoryFieldName(SolrIndexConstants.PRODUCT_CATEGORY, catalogCode),
			this.getAnalyzer().analyze(category.getCode())));
	}

	@Override
	protected Query createQuery(final String text, final String fieldName) {
		String fixedText = getAnalyzer().analyze(text);
		BooleanQuery.Builder resultQueryBuilder = new BooleanQuery.Builder();
		resultQueryBuilder.add(new PrefixQuery(new Term(fieldName, fixedText)), Occur.SHOULD);
		resultQueryBuilder.add(new TermQuery(new Term(fieldName, fixedText)), Occur.SHOULD);
		return resultQueryBuilder.build();
	}

}
