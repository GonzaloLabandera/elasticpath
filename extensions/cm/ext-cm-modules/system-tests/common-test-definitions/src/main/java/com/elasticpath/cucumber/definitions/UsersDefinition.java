package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import org.apache.commons.codec.digest.DigestUtils;

import com.elasticpath.selenium.dialogs.ChangePasswordDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateUserDialog;
import com.elasticpath.selenium.dialogs.ExpiredPasswordDialog;
import com.elasticpath.selenium.dialogs.UserMenuDialog;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.UserSearch;
import com.elasticpath.selenium.resultspane.UsersResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.DBConnector;
import com.elasticpath.selenium.util.Utility;

/**
 * User Roles step definitions.
 */
public class UsersDefinition {
	private static final String COMMA = "','";
	private final ConfigurationActionToolbar configurationActionToolbar;
	private UserSearch userSearch;
	private UsersResultPane usersResultPane;
	private CreateUserDialog createUserDialog;
	private String newUserName;
	private UserMenuDialog userMenuDialog;
	private ChangePasswordDialog changePasswordDialog;
	private static final int SLEEP_COUNT = 4;
	private static final int SLEEP_TIME = 500;

	/**
	 * Constructor.
	 */
	public UsersDefinition() {
		configurationActionToolbar = new ConfigurationActionToolbar(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Open the users view.
	 */
	@When("^I go to Users$")
	public void clickUsers() {
		userSearch = configurationActionToolbar.clickUsers();
	}

	/**
	 * Click Search button in User Search tab.
	 */
	@And("^I click Search button in User Search tab$")
	public void clickUserSearch() {
		usersResultPane = userSearch.clickSearchButton();
	}

	/**
	 * Click the Create User button.
	 */
	@And("^I click the Create User button$")
	public void clickCreateUser() {
		createUserDialog = usersResultPane.clickCreateUserButton();
	}

	/**
	 * Create a new user through cm ui.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	@And("^I create a user with following values")
	public void createUser(final Map<String, String> userMap) {
		new ActivityToolbar(SeleniumDriverSetup.getDriver()).clickConfigurationButton();
		clickUsers();
		clickUserSearch();
		clickCreateUser();
		newUserName = userMap.get("User Name") + Utility.getRandomUUID();
		createUserDialog.enterUserName(newUserName);
		createUserDialog.enterFirstName(userMap.get("First Name"));
		createUserDialog.enterLastName(userMap.get("Last Name"));
		createUserDialog.enterEmailAddress(newUserName + "@elasticpath.com");
		createUserDialog.clickNextButton();
		createUserDialog.selectAvailableRole(userMap.get("User Role"));
		createUserDialog.clickMoveRightButton();
		createUserDialog.clickFinishButton();

		DBConnector dbConnector = new DBConnector();
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET PASSWORD='" + DigestUtils.sha1Hex(userMap.get("Password")) + "' WHERE USER_NAME='"
				+ newUserName + "';");
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET STATUS='1' WHERE USER_NAME='" + newUserName + "';");
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET LAST_CHANGED_PASSWORD_DATE='2022-01-01 12:00:00' WHERE USER_NAME='" + newUserName + "';");
		signOut();
	}

	/**
	 * Create a new user through a db insert and sign in.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	@And("^I sign in as a new user with following values")
	public void createUserInDBAndSignIn(final Map<String, String> userMap) {
		createUserInDB(userMap);
		new SignInDefinition().signInAsUser(newUserName, userMap.get("Password"));
	}

	/**
	 * Create a new user through a db insert.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	@And("^a new user with following values")
	public void createNewUser(final Map<String, String> userMap) {
		createUserInDB(userMap);
	}

	/**
	 * Sign the user out.
	 */
	@And("^I sign out")
	public void signOut() {
		new ActivityToolbar(SeleniumDriverSetup.getDriver()).clickUserMenu();
		userMenuDialog = new UserMenuDialog(SeleniumDriverSetup.getDriver());
		userMenuDialog.clickLogout();
	}

	/**
	 * Change the password.
	 *
	 * @param oldPassword current password
	 * @param newPassword new password
	 */
	@And("^I (?:can change|change) the password from (.+) to (.+)")
	public void changePassword(final String oldPassword, final String newPassword) {
		new ActivityToolbar(SeleniumDriverSetup.getDriver()).clickUserMenu();
		enterNewPassword(oldPassword, newPassword);
		new ConfirmDialog(SeleniumDriverSetup.getDriver()).clickOKButton("CoreMessages.ChangePasswordDialog_Confirmation_Dialog");
	}

	/**
	 * Change password.
	 *
	 * @param oldPassword current password
	 * @param newPassword new password
	 */
	@And("^I attempt to change the password from (.+) to (.+)")
	public void attemptToChangePassword(final String oldPassword, final String newPassword) {
		enterNewPassword(oldPassword, newPassword);
	}

	/**
	 * Sign in with the new user.
	 */
	@And("^I sign in with the new user")
	public void signInWithNewUser() {
		new SignInDefinition().attemptSignInAsUser(newUserName, "password1");
	}

	/**
	 * Verifies the new user can sign in.
	 *
	 * @param password newuser's password
	 */
	@And("^I (?:can sign|sign) in as the new user with password (.+)")
	public void signInWithNewUser(final String password) {
		new SignInDefinition().signInAsUser(newUserName, password);
		new ActivityToolbar(SeleniumDriverSetup.getDriver()).clickCatalogManagementButton();
	}

	/**
	 * Verifies newly created user exists.
	 */
	@And("^I verify newly created user exists")
	public void doesCreatedUserNameExist() {
		isUserInList(newUserName);
	}

	/**
	 * Set the new user's password age.
	 *
	 * @param age number of days the password age will be set back
	 */
	@And("^the new user's password is more than (.+) days old")
	public void changePasswordAge(final int age) {
		int ageInDays = age;
		DBConnector dbConnector = new DBConnector();

		ageInDays += 1;

		Calendar calendar = Calendar.getInstance(); // this would default to now
		calendar.add(Calendar.DAY_OF_MONTH, -ageInDays);

		String lastChangedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).format(calendar.getTime());

		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET LAST_CHANGED_PASSWORD_DATE='" + lastChangedDate + "' WHERE USER_NAME='" + newUserName
				+ "';");

		dbConnector.closeAll();
	}

	/**
	 * Verify password change dialog is displayed.
	 */
	@And("^I am prompted to change my password")
	public void verifyPasswordExpiredFormIsDisplayed() {
		new ExpiredPasswordDialog(SeleniumDriverSetup.getDriver());
	}

	/**
	 * Verify password change error.
	 *
	 * @param errorMessage expected error message
	 */
	@And("^the password change fails with message \"(.+)\"")
	public void verifyPasswordError(final String errorMessage) {
// to be implemented with PB-2779
	}

	/**
	 * Is username in list.
	 *
	 * @param userName the username.
	 */
	public void isUserInList(final String userName) {
		usersResultPane = userSearch.clickSearchButton();
		boolean isUserInList = usersResultPane.isUserInList(userName);

		int count = 0;
		while (!isUserInList && count < SLEEP_COUNT) {
			usersResultPane.sleep(SLEEP_TIME);
			userSearch.clickSearchButton();
			isUserInList = usersResultPane.isUserInList(userName);
			count++;
		}

		assertThat(isUserInList)
				.as("Shipping Service Level does not exist in search result - " + userName)
				.isTrue();
	}

	/**
	 * Update password in password change dialog.
	 *
	 * @param oldPassword the original password
	 * @param newPassword the new password
	 */
	public void enterNewPassword(final String oldPassword, final String newPassword) {
		new ActivityToolbar(SeleniumDriverSetup.getDriver()).clickUserMenu();
		userMenuDialog = new UserMenuDialog(SeleniumDriverSetup.getDriver());
		changePasswordDialog = userMenuDialog.clickChangePassword();
		changePasswordDialog.enterOldPassword(oldPassword);
		changePasswordDialog.enterNewPassword(newPassword);
		changePasswordDialog.enterConfirmNewPassword(newPassword);
		changePasswordDialog.clickSaveButton();
	}

	/**
	 * Create user with db insert.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	public void createUserInDB(final Map<String, String> userMap) {
		newUserName = userMap.get("User Name") + Utility.getRandomUUID();

		Calendar calendar = Calendar.getInstance(); // this would default to now

		String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).format(calendar.getTime());

		DBConnector dbConnector = new DBConnector();
		int uidpk = dbConnector.getMaxUidpk("TCMUSER", "UIDPK < 200000") + 1;

		String query = "INSERT INTO TCMUSER (UIDPK,USER_NAME,EMAIL,FIRST_NAME,LAST_NAME,PASSWORD,CREATION_DATE,LAST_LOGIN_DATE"
				+ ",LAST_CHANGED_PASSWORD_DATE,FAILED_LOGIN_ATTEMPTS,GUID,STATUS,ALL_WAREHOUSE_ACCESS,ALL_CATALOG_ACCESS"
				+ ",ALL_STORE_ACCESS,ALL_PRICELIST_ACCESS,LAST_MODIFIED_DATE) VALUES (" + uidpk + ",'" + newUserName
				+ COMMA + newUserName + "@elasticpath.com','" + userMap.get("First Name") + COMMA + userMap.get("Last Name")
				+ COMMA + DigestUtils.sha1Hex(userMap.get("Password")) + COMMA + date + COMMA + date + COMMA + date + "',0,'" + UUID.randomUUID()
				.toString() + "',1,1,1,1,1,'" + date + "');";

		dbConnector.executeUpdateQuery(query);

		dbConnector.executeUpdateQuery("INSERT INTO TCMUSERROLEX (CM_USER_UID, USER_ROLE_UID) VALUES (" + uidpk + ", '201');");
		dbConnector.closeAll();
	}
}