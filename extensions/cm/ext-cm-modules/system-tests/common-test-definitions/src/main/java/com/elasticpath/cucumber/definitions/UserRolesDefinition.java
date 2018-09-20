package com.elasticpath.cucumber.definitions;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.resultspane.UserRolesResultPane;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;

/**
 * User Roles step definitions.
 */
public class UserRolesDefinition {
	private final ConfigurationActionToolbar configurationActionToolbar;
	private UserRolesResultPane userRolesResultPane;

	/**
	 * Constructor.
	 */
	public UserRolesDefinition() {
		configurationActionToolbar = new ConfigurationActionToolbar(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Click user roles.
	 */
	@When("^I go to User Roles$")
	public void clickUserRoles() {
		userRolesResultPane = configurationActionToolbar.clickUserRoles();
	}

	/**
	 * Verify User roles exists.
	 *
	 * @param expectedRole the expected role.
	 */
	@Then("^User role (.+) exists$")
	public void verifyUserRoleExists(final String expectedRole) {
		userRolesResultPane.verifyUserRoleExists(expectedRole);
	}

	/**
	 * Verify User Role link is present.
	 */
	@And("^I can view User Roles link")
	public void verifyUserRolesLinkIsPresent() {
		configurationActionToolbar.verifyUserRolesLinkIsPresent();
	}

}
