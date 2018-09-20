package com.elasticpath.cucumber.definitions;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import com.elasticpath.selenium.common.CM;
import com.elasticpath.selenium.dialogs.SignInDialog;
import com.elasticpath.selenium.framework.util.PropertyManager;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;

/**
 * Sign In steps.
 */
public class SignInDefinition {
	private SignInDialog signInDialog;

	/**
	 * Constructor.
	 */
	public SignInDefinition() {
		signInDialog = new SignInDialog(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Sign In CM as admin user.
	 */
	@Given("^I sign in to CM as admin user$")
	public void signInAsAdmin() {
		String adminID = PropertyManager.getInstance().getProperty("admin_id");
		String adminPassword = PropertyManager.getInstance().getProperty("cm_password");

		signIn(adminID, adminPassword);
		//TODO - do we need change set isEnabled check?

	}

	/**
	 * Sign In CM as CSR user.
	 */
	@Given("^I sign in to CM as CSR user$")
	public void signInAsCSR() {
		String csrID = PropertyManager.getInstance().getProperty("csr_id");
		String csrPassword = PropertyManager.getInstance().getProperty("csr_password");

		signIn(csrID, csrPassword);
	}

	/**
	 * Sign In CM per given credentials.
	 *
	 * @param username the username.
	 * @param password the password.
	 */
	@Given("^I attempt to sign in to CM as (.*) with password (.+)$")
	public void attemptSignInAsUser(final String username, final String password) {
		signIn(username, password);
	}

	/**
	 * Sign In CM per given credentials and verify successfully signed in.
	 *
	 * @param username the username.
	 * @param password the password.
	 */
	@Then("^I(?: sign in| should be able to sign in again) to CM as (.*) with password (.+)$")
	public void signInAsUser(final String username, final String password) {
		signIn(username, password);
		signInDialog.verifySignInSuccessful();
	}

	/**
	 * Verify Sign in Error.
	 */
	@Then("^I should not be able to sign in$")
	public void verifySignInError() {
		signInDialog.verifySignInFailed();
	}

	/**
	 * Sign in.
	 *
	 * @param username the username.
	 * @param password the password.
	 */
	private void signIn(final String username, final String password) {
		CM commerceManager = new CM(SeleniumDriverSetup.getDriver());
		signInDialog = commerceManager.openCM();
		signInDialog.enterUsername(username);
		signInDialog.enterPassword(password);
		signInDialog.clickSignIn();
	}

}
