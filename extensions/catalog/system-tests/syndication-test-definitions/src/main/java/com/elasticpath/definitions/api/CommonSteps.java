package com.elasticpath.definitions.api;

import static org.hamcrest.Matchers.isEmptyOrNullString;

import cucumber.api.java.en.Then;

import com.elasticpath.definitions.api.helpers.SetUp;
import com.elasticpath.definitions.stateobjects.Context;

/**
 * Syndication API Common steps.
 */
public class CommonSteps {

	private final Context context;

	/**
	 * Constructor.
	 *
	 * @param context state object
	 */
	public CommonSteps(final Context context) {
		this.context = context;
	}

	/**
	 * Verifies response status code.
	 *
	 * @param code expected status code
	 */
	@Then("^Response status code is (\\d+)$")
	public void validateResponseCode(final int code) {
		context.getResponse()
				.then()
				.statusCode(code);
	}

	/**
	 * Verifies that response contains empty body.
	 */
	@Then("^Response does not have content$")
	public void validateEmptyContent() {
		context.getResponse()
				.then()
				.assertThat()
				.content(isEmptyOrNullString());
	}

	/**
	 * Sets up super user to be used for authentication by http client.
	 */
	@Then("^I use Super User for API calls$")
	public void superUserApiAuth() {
		SetUp.superUserAuth();
	}

	/**
	 * Sets up Cm login user to be used for authentication by http client.
	 */
	@Then("^I use Cm Login User for API calls$")
	public void cmLoginUserApiAuth() {
		SetUp.cmLoginUserAuth();
	}
}
