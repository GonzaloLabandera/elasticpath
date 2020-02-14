package com.elasticpath.definitions.api.helpers;

/**
 * Http client constants.
 */
public class Constants {

	/**
	 * Syndication projections base URL.
	 */
	public static final String SYNDICATION_WS_BASE_URL =
			System.getProperty("ep.syndication.ws.baseurl", "http://localhost:8083/integration/api/syndication/v1/catalog/");

	/**
	 * Web service access user name.
	 */
	public static final String WS_USER_NAME = "apiuser";

	/**
	 * CM login user name.
	 */
	public static final String CM_LOGIN_USER_NAME = "cmuser";

	/**
	 * Super user name.
	 */
	public static final String SUPER_USER_NAME = "admin";

	/**
	 * Password.
	 */
	public static final String PASSWORD = "111111";

	/**
	 * Etag header name.
	 */
	public static final String ETAG = "ETag";

	/**
	 * API sleep time.
	 */
	public static final long API_SLEEP_TIME = 2000;
}
