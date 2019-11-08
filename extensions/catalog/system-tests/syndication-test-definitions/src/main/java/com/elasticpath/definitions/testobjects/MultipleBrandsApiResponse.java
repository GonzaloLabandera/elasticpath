package com.elasticpath.definitions.testobjects;


/**
 * Multiple brands API response test object.
 */
public final class MultipleBrandsApiResponse extends SingleBrandApiResponse {

	/**
	 * JSON path to pagination currentDateTime.
	 */
	public static final String CURRENT_DATE_TIME = "currentDateTime";

	/**
	 * JSON path to pagination results.
	 */
	public static final String RESULTS = "results";
	private static final String PAGINATION = "pagination";
	private static final String NEXT = PAGINATION + ".next";

	/**
	 * JSON path to pagination limit.
	 */
	public static final String LIMIT = NEXT + ".limit";

	/**
	 * JSON path to pagination startAfter.
	 */
	public static final String START_AFTER = NEXT + ".startAfter";

	/**
	 * JSON path to pagination hasMoreResults.
	 */
	public static final String HAS_MORE_RESULTS = NEXT + ".hasMoreResults";
	private static final String POST_URL = "%s/brandevents";
	private static final String BRANDS_URL = "%s/brands";

	public String getPostUrl() {
		return POST_URL;
	}

	public String getBrandsUrl() {
		return BRANDS_URL;
	}

	/**
	 * Returns a path to brand projection type in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection type in results array multiple brand projections API response JSON
	 */
	public String getType(final String code) {
		return getBrandProjectionPath(code) + TYPE;
	}

	/**
	 * Returns a path to brand projection code in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection code in results array multiple brand projections API response JSON
	 */
	public String getCode(final String code) {
		return getBrandProjectionPath(code) + CODE;
	}

	/**
	 * Returns a path to brand projection code in results array multiple brand projections API response JSON.
	 *
	 * @return Returns a path to brand projection code in results array multiple brand projections API response JSON
	 */
	public String getCodePath() {
		return RESULTS + "." + CODE;
	}

	/**
	 * Returns a path to all the brand projections codes in results array multiple brand projections API response JSON.
	 *
	 * @return a path to all the brand projections codes in results array multiple brand projections API response JSON
	 */
	public String getAllCodesPath() {
		return getCodePath() + ".findAll{it}";
	}

	/**
	 * Returns a path to brand projection store in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection store in results array multiple brand projections API response JSON
	 */
	public String getStore(final String code) {
		return getBrandProjectionPath(code) + STORE;
	}

	/**
	 * Returns a path to brand projection modified date in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection modified date in results array multiple brand projections API response JSON
	 */
	public String getModifiedDate(final String code) {
		return getBrandProjectionPath(code) + MODIFIED_DATE_TIME;
	}

	/**
	 * Returns a path to brand projection 'deleted' field in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection 'deleted' field in results array multiple brand projections API response JSON
	 */
	public String getDeleted(final String code) {
		return getBrandProjectionPath(code) + DELETED;
	}

	/**
	 * Returns a path to brand projection display name in results array multiple brand projections API response JSON.
	 *
	 * @param code     sku brand code
	 * @param language locale code
	 * @return Returns a path to brand projection display name in results array multiple brand projections API response JSON
	 */
	public String getDisplayNamePath(final String code, final String language) {
		return getBrandProjectionPath(code) + getDisplayNamePath(language);
	}

	/**
	 * Returns a path to brand projection block in results array multiple brand projections API response JSON.
	 *
	 * @param code sku brand code
	 * @return Returns a path to brand projection block in results array multiple brand projections API response JSON
	 */
	public String getBrandProjectionPath(final String code) {
		return RESULTS + ".find{it.identity.code == \"" + code + "\"}.";
	}

	/**
	 * Returns query which checks if given element exists in JSON.
	 *
	 * @param elementPath JSON path to the element
	 * @return query which checks if given element exists in JSON.
	 */
	public String getExistenceCheckPath(final String elementPath) {
		return "any{it.key == \"" + elementPath + "\"}";
	}
}
