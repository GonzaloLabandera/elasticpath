package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.util.Constants;

/**
 * Create User Dialog.
 */
public class CreateUserDialog extends AbstractDialog {

	private static final Logger LOGGER = LogManager.getLogger(CreateUserDialog.class);

	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String CREATE_USER_PARENT_CSS = "div[widget-id='Create User'][widget-type='Shell'] ";
	private static final String USER_NAME_INPUT_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='User Name'] input";
	private static final String FIRST_NAME_INPUT_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='First Name'] input";
	private static final String LAST_NAME_INPUT_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='Last Name'] input";
	private static final String EMAIL_ADDRESS = CREATE_USER_PARENT_CSS + "div[widget-id='Email Address'] input";
	private static final String FINISH_BUTTON_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='Finish']";
	private static final String NEXT_BUTTON_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='Next >']";
	private static final String AVAILABLE_ROLES_PARENT_CSS = "div[automation-id='com.elasticpath.cmclient.admin.users.AdminUsersMessages"
			+ ".RoleAssignment_AvailableRoles'][seeable='true'][widget-type='Table'] ";
	private static final String AVAILABLE_ROLES_COLUMN_CSS = AVAILABLE_ROLES_PARENT_CSS + "div[column-id='%s']";
	private static final String MOVE_RIGHT_BUTTON_CSS = CREATE_USER_PARENT_CSS + "div[widget-id='Add']";
	private static final int VISIBLE_AVAILABLE_ROLE_LIST_SIZE = 6;

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public CreateUserDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Inputs user name.
	 *
	 * @param userName String
	 */
	public void enterUserName(final String userName) {
		clearAndType(USER_NAME_INPUT_CSS, userName);

	}

	/**
	 * Inputs first name.
	 *
	 * @param firstName String
	 */
	public void enterFirstName(final String firstName) {
		clearAndType(FIRST_NAME_INPUT_CSS, firstName);
	}

	/**
	 * Inputs last name.
	 *
	 * @param lastName String
	 */
	public void enterLastName(final String lastName) {
		clearAndType(LAST_NAME_INPUT_CSS, lastName);
	}

	/**
	 * Inputs email.
	 *
	 * @param emailAddress String
	 */
	public void enterEmailAddress(final String emailAddress) {
		clearAndType(EMAIL_ADDRESS, emailAddress);
	}

	/**
	 * Clicks finish button.
	 */
	public void clickFinishButton() {
		clickButton(FINISH_BUTTON_CSS, "Finish");
		waitTillElementDisappears(By.cssSelector(CREATE_USER_PARENT_CSS));
	}

	/**
	 * Clicks next button.
	 */
	public void clickNextButton() {
		clickButton(NEXT_BUTTON_CSS, "Next");
	}

	/**
	 * Clicks move right button.
	 */
	public void clickMoveRightButton() {
		clickButton(MOVE_RIGHT_BUTTON_CSS, "Move Right");
	}

	/**
	 * Selects role from available roles list.
	 *
	 * @param role String
	 */
	public void selectAvailableRole(final String role) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(AVAILABLE_ROLES_PARENT_CSS));
		scrollToFirstElementWithUpArrowKey(AVAILABLE_ROLES_PARENT_CSS + "div[column-num='0']");

		boolean roleExists = false;
		List<String> beforeList;
		List<String> afterList;
		LOGGER.debug("--------------- initial visible role list ---------------: " + getVisibleAvailableRoleList());
		do {
			beforeList = getVisibleAvailableRoleList();
			LOGGER.debug("--------------- role list before scrollDownWithDownArrowKey ---------------: " + beforeList);

			setWebDriverImplicitWait(Constants.IMPLICIT_WAIT_FOR_ELEMENT_NOT_EXISTS);
			if (verifyItemIsNotInDialog(AVAILABLE_ROLES_PARENT_CSS, AVAILABLE_ROLES_COLUMN_CSS, role, "Available Roles")) {
				roleExists = true;
				break;
			}
			setWebDriverImplicitWaitToDefault();

			scrollDownWithDownArrowKey(getDriver().findElement(By.cssSelector(AVAILABLE_ROLES_PARENT_CSS)), VISIBLE_AVAILABLE_ROLE_LIST_SIZE);
			afterList = getVisibleAvailableRoleList();
			LOGGER.debug("--------------- role list after scrollDownWithDownArrowKey ---------------: " + afterList);
		} while (!beforeList.equals(afterList));

		assertThat(roleExists)
				.as("Unable to find role - " + role)
				.isTrue();
	}

	/**
	 * Scroll up with arrow key till it reaches at the top of the list.
	 *
	 * @param constantCssSelector The initial css selector.
	 */
	private void scrollToFirstElementWithUpArrowKey(final String constantCssSelector) {
		WebElement initialRow = getDriver().findElement(By.cssSelector(constantCssSelector));
		WebElement parentTable = getDriver().findElement(By.cssSelector(AVAILABLE_ROLES_PARENT_CSS));
		initialRow.click();
		String currentDisplayName = initialRow.getText();
		String previousDisplayName = "";
		LOGGER.debug("--------------- initial selected item --------------- : " + currentDisplayName);

		do {
			WebElement selectedRow = getDriver().findElement(By.cssSelector(constantCssSelector));
			previousDisplayName = selectedRow.getText();
			LOGGER.debug("--------------- previousDisplayedName --------------- : " + previousDisplayName);

			parentTable.sendKeys(Keys.ARROW_UP);

			currentDisplayName = getDriver().findElement(By.cssSelector(constantCssSelector)).getText();
			LOGGER.debug("--------------- currentDisplayName --------------- : " + currentDisplayName);
		} while (!currentDisplayName.equals(previousDisplayName));
	}

	/**
	 * Returns list of visible available roles.
	 *
	 * @return visibleAvailableRoleList
	 */
	private List<String> getVisibleAvailableRoleList() {
		sleep(Constants.SLEEP_THREE_SECONDS_IN_MILLIS);
		List<WebElement> elements = getDriver().findElements(By.cssSelector(AVAILABLE_ROLES_PARENT_CSS + "div[widget-type='table_row']"));
		List<String> visibleAvailableRoleList = new ArrayList<>();
		for (WebElement element : elements) {
			visibleAvailableRoleList.add(element.getText());
		}
		Collections.sort(visibleAvailableRoleList);
		return visibleAvailableRoleList;
	}

	/**
	 * Checks if the dialog is present.
	 *
	 * @return boolean
	 */
	public boolean isDialogPresent() {
		return isElementPresent(By.cssSelector(CREATE_USER_PARENT_CSS));
	}
}
