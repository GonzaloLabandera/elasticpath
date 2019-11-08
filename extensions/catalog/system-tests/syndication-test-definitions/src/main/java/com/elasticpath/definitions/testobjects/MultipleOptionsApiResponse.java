package com.elasticpath.definitions.testobjects;


/**
 * Sku option multiple options API response test object.
 */
public final class MultipleOptionsApiResponse extends SingleOptionApiResponse {

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
	private static final String POST_OPTION_URL = "%s/optionevents";
	private static final String OPTIONS_URL = "%s/options";

	public String getPostOptionUrl() {
		return POST_OPTION_URL;
	}

	public String getOptionsUrl() {
		return OPTIONS_URL;
	}

	/**
	 * Returns a path to option projection type in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection type in results array multiple option projections API response JSON
	 */
	public String getType(final String code) {
		return getOptionProjectionPath(code) + TYPE;
	}

	/**
	 * Returns a path to option projection code in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection code in results array multiple option projections API response JSON
	 */
	public String getCode(final String code) {
		return getOptionProjectionPath(code) + CODE;
	}

	/**
	 * Returns a path to option projection code in results array multiple option projections API response JSON.
	 *
	 * @return Returns a path to option projection code in results array multiple option projections API response JSON
	 */
	public String getCodePath() {
		return RESULTS + "." + CODE;
	}

	/**
	 * Returns a path to all the option projections codes in results array multiple option projections API response JSON.
	 *
	 * @return a path to all the option projections codes in results array multiple option projections API response JSON
	 */
	public String getAllCodesPath() {
		return getCodePath() + ".findAll{it}";
	}

	/**
	 * Returns a path to option projection store in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection store in results array multiple option projections API response JSON
	 */
	public String getStore(final String code) {
		return getOptionProjectionPath(code) + STORE;
	}

	/**
	 * Returns a path to option projection modified date in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection modified date in results array multiple option projections API response JSON
	 */
	public String getModifiedDate(final String code) {
		return getOptionProjectionPath(code) + MODIFIED_DATE_TIME;
	}

	/**
	 * Returns a path to option projection 'deleted' field in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection 'deleted' field in results array multiple option projections API response JSON
	 */
	public String getDeleted(final String code) {
		return getOptionProjectionPath(code) + DELETED;
	}

	/**
	 * Returns a path to option projection display name in results array multiple option projections API response JSON.
	 *
	 * @param code     sku option code
	 * @param language locale code
	 * @return Returns a path to option projection display name in results array multiple option projections API response JSON
	 */
	public String getDisplayNamePath(final String code, final String language) {
		return getOptionProjectionPath(code) + getDisplayNamePath(language);
	}

	/**
	 * Returns a path to the first sku option value code in results array multiple option projections API response JSON.
	 *
	 * @param code     sku option code
	 * @param language locale code
	 * @return Returns a path to the first sku option value code in results array multiple option projections API response JSON
	 */
	public String getFirstOptionValuePath(final String code, final String language) {
		return getOptionProjectionPath(code) + getFirstOptionValuePath(language);
	}

	/**
	 * Returns a path to option projection option value in results array multiple option projections API response JSON.
	 *
	 * @param code     sku option code
	 * @param language locale code
	 * @return Returns a path to option projection option value in results array multiple option projections API response JSON
	 */
	public String getFirstOptionValueNamePath(final String code, final String language) {
		return getOptionProjectionPath(code) + getFirstOptionValueNamePath(language);
	}

	/**
	 * Returns a path to option projection block in results array multiple option projections API response JSON.
	 *
	 * @param code sku option code
	 * @return Returns a path to option projection block in results array multiple option projections API response JSON
	 */
	public String getOptionProjectionPath(final String code) {
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
