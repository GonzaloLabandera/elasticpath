package com.elasticpath.selenium.resultspane;

import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.CreateUserDialog;

/**
 * User Result Pane.
 */
public class UsersResultPane extends AbstractPageObject {
	private static final String CREATE_USER_BUTTON_CSS = "div[widget-id='Create User'][widget-type='ToolItem'][seeable='true']";
	private static final String USER_SEARCH_RESULT_PARENT_CSS = "div[widget-id='User List'][widget-type='Table'] ";
	private static final String USER_SEARCH_RESULT_LIST = "div[column-id='%s']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public UsersResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Open create user dialog.
	 *
	 * @return CreateUserDialog String
	 */
	public CreateUserDialog clickCreateUserButton() {
		clickButton(CREATE_USER_BUTTON_CSS, "Create User");
		return new CreateUserDialog(getDriver());
	}

	/**
	 * Is user in the list.
	 *
	 * @param userName user to search for
	 * @return true/false
	 */
	public boolean isUserInList(final String userName) {
		return selectItemInCenterPane(USER_SEARCH_RESULT_PARENT_CSS, USER_SEARCH_RESULT_LIST,
				userName, "User Name");
	}
}
