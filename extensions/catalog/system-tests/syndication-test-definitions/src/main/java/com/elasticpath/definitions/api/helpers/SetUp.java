package com.elasticpath.definitions.api.helpers;

import static io.restassured.RestAssured.basic;
import static io.restassured.config.ConnectionConfig.connectionConfig;

import cucumber.api.java.Before;
import io.restassured.RestAssured;
import org.apache.log4j.Logger;

/**
 * Object which is responsible for initial set up of http client.
 */
public class SetUp {

	private static final Logger LOGGER = Logger.getLogger(SetUp.class);

	/**
	 * Sets up Cm Login user to be used for authentication by http client.
	 */
	public static void cmLoginUserAuth() {
		RestAssured.authentication = basic(Constants.CM_LOGIN_USER_NAME, Constants.PASSWORD);
	}

	/**
	 * Sets up super user to be used for authentication by http client.
	 */
	public static void superUserAuth() {
		RestAssured.authentication = basic(Constants.SUPER_USER_NAME, Constants.PASSWORD);
	}

	/**
	 * Initial set up of http client.
	 */
	@Before
	public void setUp() {
		RestAssured.config().connectionConfig(connectionConfig().closeIdleConnectionsAfterEachResponse());
		RestAssured.authentication = basic(Constants.WS_USER_NAME, Constants.PASSWORD);
		RestAssured.baseURI = Constants.SYNDICATION_WS_BASE_URL;
		LOGGER.warn("Scenario is run with base URL:" + Constants.SYNDICATION_WS_BASE_URL);
	}
}
