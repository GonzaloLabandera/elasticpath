package com.elasticpath.selenium.navigations;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.resultspane.UsersResultPane;

/**
 * User search.
 */
public class UserSearch extends AbstractNavigation {

	private static final String USER_SEARCH_BUTTON_CSS = "div[widget-id='Search'][seeable='true']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public UserSearch(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks on search button.
	 *
	 * @return UsersResultPane
	 */
	public UsersResultPane clickSearchButton() {
		clickButton(USER_SEARCH_BUTTON_CSS, "Search");
		return new UsersResultPane(getDriver());
	}
}
