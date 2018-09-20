package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Uer Roles Result Pane.
 */
public class UserRolesResultPane extends AbstractPageObject {
	private static final String USER_ROLES_RESULTS_XPATH = "//div[@widget-id='Role']//div[contains(text(), 'Super User')]";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public UserRolesResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies given user role exists.
	 *
	 * @param expectedUserRole String
	 */
	public void verifyUserRoleExists(final String expectedUserRole) {
		String fullXpath = String.format(USER_ROLES_RESULTS_XPATH, expectedUserRole);
		assertThat(getWaitDriver().waitForElementToBeVisible(By.xpath(fullXpath)).getText())
				.as("Expected user role does not exist")
				.contains(expectedUserRole);
	}
}
