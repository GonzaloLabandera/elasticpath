package com.elasticpath.cucumber.definitions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.WebDriver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.elasticpath.selenium.dialogs.ChangePasswordDialog;
import com.elasticpath.selenium.dialogs.ChangeTimezoneDialog;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreateUserDialog;
import com.elasticpath.selenium.dialogs.ExpiredPasswordDialog;
import com.elasticpath.selenium.dialogs.SignInDialog;
import com.elasticpath.selenium.dialogs.UserMenuDialog;
import com.elasticpath.selenium.framework.util.SeleniumDriverSetup;
import com.elasticpath.selenium.navigations.UserSearch;
import com.elasticpath.selenium.resultspane.UsersResultPane;
import com.elasticpath.selenium.toolbars.ActivityToolbar;
import com.elasticpath.selenium.toolbars.ConfigurationActionToolbar;
import com.elasticpath.selenium.util.Constants;
import com.elasticpath.selenium.util.DBConnector;
import com.elasticpath.selenium.util.Utility;
import com.elasticpath.selenium.wizards.EditUser;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * User Roles step definitions.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class UsersDefinition {
	private static final String COMMA = "','";
	private final ConfigurationActionToolbar configurationActionToolbar;
	private final UserSearch userSearch;
	private final UsersResultPane usersResultPane;
	private CreateUserDialog createUserDialog;
	private final SignInDialog signInDialog;

	private String newUserName;
	private String firstNameChanged;

	private UserMenuDialog userMenuDialog;
	private ChangeTimezoneDialog changeTimezoneDialog;
	private static final String PASSWORD = "Password";
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(UsersDefinition.class);
	private final WebDriver driver;

	/**
	 * Constructor.
	 */
	public UsersDefinition() {
		driver = SeleniumDriverSetup.getDriver();
		this.userSearch = new UserSearch(driver);
		this.usersResultPane = new UsersResultPane(driver);
		configurationActionToolbar = new ConfigurationActionToolbar(driver);
		signInDialog = new SignInDialog(driver);
	}

	public String getNewUserName() {
		return newUserName;
	}

	/**
	 * Open the users view.
	 */
	@When("^I go to Users$")
	public void clickUsers() {
		configurationActionToolbar.clickUsers();
	}

	/**
	 * Click Search button in User Search tab.
	 */
	@And("^I click Search button in User Search tab$")
	public void clickUserSearch() {
		userSearch.clickSearchButton();
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
		new ActivityToolbar(driver).clickConfigurationButton();
		clickUsers();
		clickUserSearch();
		clickCreateUser();
		int counter = 0;
		while (!createUserDialog.isDialogPresent() && counter < Constants.RETRY_COUNTER_3) {
			LOGGER.warn((counter + 1) + ". Failed to open Create User Dialog");
			clickCreateUser();
			counter++;
		}
		newUserName = userMap.get("User Name") + Utility.getRandomUUID();
		createUserDialog.enterUserName(newUserName);
		createUserDialog.enterFirstName(userMap.get("First Name"));
		createUserDialog.enterLastName(userMap.get("Last Name"));
		createUserDialog.enterEmailAddress(newUserName + "@elasticpath.com");
		createUserDialog.clickNextButton();
		createUserDialog.selectAvailableRole(userMap.get("User Role"));
		createUserDialog.clickMoveRightButton();
		createUserDialog.clickFinishButton();

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String brcyptPassword = passwordEncoder.encode(userMap.get(PASSWORD));

		DBConnector dbConnector = new DBConnector();
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET PASSWORD='" + brcyptPassword + "' WHERE USER_NAME='" + newUserName + "'");
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET STATUS='1' WHERE USER_NAME='" + newUserName + "'");
		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET LAST_CHANGED_PASSWORD_DATE='" + dbConnector.getDate("2025-01-01 12:00:00")
				+ "' WHERE USER_NAME='" + newUserName + "'");
	}

	/**
	 * Searches for user by userName of the new user created in the same scenerio and verifies it is there.
	 */
	@Then("^I search for newly created user$")
	public void searchUserByUserName() {
		searchUserByUserName(newUserName);
	}

	/**
	 * Searches for user by userName and verifies it is there.
	 *
	 * @param userName the user name
	 */
	@Then("^(?:I can|I) search for user with the username (.+)$")
	public void searchUserByUserName(final String userName) {
		new ActivityToolbar(driver).clickConfigurationButton();
		clickUsers();
		userSearch.enterUserName(userName);
		usersResultPane.verifyUserExistsInList(userName);
	}

	/**
	 * Searches for user by firstName and verifies it is there.
	 *
	 * @param firstName the user's first name
	 */
	@Then("^(?:I can|I) search for user with the firstName (.+)$")
	public void searchUserByFirstName(final String firstName) {
		new ActivityToolbar(driver).clickConfigurationButton();
		clickUsers();
		userSearch.enterFirstName(firstName);
		usersResultPane.verifyUserExistsInList(firstName);
	}

	/**
	 * Create a new user through a db insert and sign in.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	@And("^I sign in as a new user with following values")
	public void createUserInDBAndSignIn(final Map<String, String> userMap) {
		createUserInDB(userMap);
		new SignInDefinition().signInAsUser(newUserName, userMap.get(PASSWORD));
	}

	/**
	 * Create a new user through a db insert.
	 *
	 * @param userMap "User Name", "First Name", "Last Name", "Password"
	 */
	@And("^I create a new user with following values")
	public void createNewUser(final Map<String, String> userMap) {
		createUserInDB(userMap);
	}


	/**
	 * Select and disable newly created user.
	 */
	@And("^I disable newly created user$")
	public void disableNewlyCreatedUser() {
		usersResultPane.verifyUserExistsInList(newUserName);
		usersResultPane.disableUser();
	}

	/**
	 * Select and disable newly created user.
	 *
	 * @param userName user name
	 */
	@And("^I disable user with the given name (.+)")
	public void disableUser(final String userName) {
		usersResultPane.verifyUserExistsInList(userName);
		usersResultPane.disableUser();
	}

	/**
	 * Select and enable newly created user.
	 *
	 * @param userName user name
	 */
	@And("^I enable user with the given name (.+)")
	public void enableUser(final String userName) {
		usersResultPane.verifyUserExistsInList(userName);
		usersResultPane.enableUser(userName);
	}


	/**
	 * Sign the user out.
	 */
	@And("^I sign out")
	public void signOut() {
		openCmUserMenu();
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
		enterNewPassword(oldPassword, newPassword);
		new ConfirmDialog(driver).clickOKButton("CoreMessages.ChangePasswordDialog_Confirmation_Dialog");
	}

	/**
	 * Change the first name.
	 *
	 * @param userName     user name
	 * @param newFirstName new first name
	 */
	@And("^I change (.+) user's first name to (.+)")
	public void changeFirstName(final String userName, final String newFirstName) {
		firstNameChanged = newFirstName;
		usersResultPane.openEditUserWizard(userName);
		final EditUser editUserWizard = new EditUser(driver);
		editUserWizard.enterFirstName(newFirstName);
		editUserWizard.clickFinish();
	}

	/**
	 * Change the username.
	 */
	@Then("^I should see the new first name in the list for the same user$")
	public void verifyFirstNameChange() {
		usersResultPane.verifyUserExistsInList(firstNameChanged, "First Name");
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
		signInDialog.initialSignIn(newUserName, "password1");
	}

	/**
	 * Sign in with the new user.
	 */
	@And("^I sign in with expired password for the new user")
	public void signInWithWithInvalidCredentials() {
		signInDialog.performSignInWithInvalidCredentials(newUserName, "password1");
	}

	/**
	 * Verifies the new user can sign in.
	 *
	 * @param password newuser's password
	 */
	@And("^I (?:can sign|sign) in as the new user with password (.+)")
	public void signInWithNewUser(final String password) {
		new SignInDialog(driver).performSignIn(newUserName, password);
		new ActivityToolbar(driver).clickCatalogManagementButton();
	}

	/**
	 * Verifies newly created user exists.
	 */
	@And("^I verify newly created user exists")
	public void doesCreatedUserNameExist() {
		usersResultPane.verifyUserExistsInList(newUserName);
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

		dbConnector.executeUpdateQuery("UPDATE TCMUSER SET LAST_CHANGED_PASSWORD_DATE='" + dbConnector.getDate(lastChangedDate)
				+ "' WHERE USER_NAME='" + newUserName + "'");

		dbConnector.closeAll();
	}

	/**
	 * Verify password change dialog is displayed.
	 */
	@And("^I am prompted to change my password")
	public void verifyPasswordExpiredFormIsDisplayed() {
		new ExpiredPasswordDialog(driver);
	}

	/**
	 * Verify password change dialog is displayed.
	 */
	@And("^I am prompted with User is Disabled message Dialog$")
	public void verifyUserDisabledDialogIsDisplayed() {
		signInDialog.verifyDisabledUserDialogIsPresent();
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
	 * Update password in password change dialog.
	 *
	 * @param oldPassword the original password
	 * @param newPassword the new password
	 */
	public void enterNewPassword(final String oldPassword, final String newPassword) {
		openCmUserMenu();
		ChangePasswordDialog changePasswordDialog = userMenuDialog.clickChangePassword();
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

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String brcyptPassword = passwordEncoder.encode(userMap.get(PASSWORD));

		String query = "INSERT INTO TCMUSER (UIDPK,USER_NAME,EMAIL,FIRST_NAME,LAST_NAME,PASSWORD,CREATION_DATE,LAST_LOGIN_DATE"
				+ ",LAST_CHANGED_PASSWORD_DATE,FAILED_LOGIN_ATTEMPTS,GUID,STATUS,ALL_WAREHOUSE_ACCESS,ALL_CATALOG_ACCESS"
				+ ",ALL_STORE_ACCESS,ALL_PRICELIST_ACCESS,LAST_MODIFIED_DATE) VALUES (" + uidpk + ",'" + newUserName
				+ COMMA + newUserName + "@elasticpath.com','" + userMap.get("First Name") + COMMA + userMap.get("Last Name")
				+ COMMA + brcyptPassword + COMMA + dbConnector.getDate(date) + COMMA
				+ dbConnector.getDate(date) + COMMA + dbConnector.getDate(date) + "',0,'" + UUID.randomUUID().toString()
				+ "',1,1,1,1,1,'" + dbConnector.getDate(date) + "')";

		dbConnector.executeUpdateQuery(query);

		dbConnector.executeUpdateQuery("INSERT INTO TCMUSERROLEX (CM_USER_UID, USER_ROLE_UID) VALUES (" + uidpk + ", '201')");
		dbConnector.closeAll();
	}

	/**
	 * Opens Change Time Zone dialog in CM users menu.
	 */
	@And("^I open Change Time Zone dialog")
	public void openTimeZoneDialog() {
		openCmUserMenu();
		changeTimezoneDialog = userMenuDialog.clickChangeTimezone();
	}

	/**
	 * Checks that browser time zone is selected in Change Time Zone dialog in CM users menu.
	 */
	@And("^I see that browser time zone is selected")
	public void verifyBrowserTimeZoneSelected() {
		assertThat(changeTimezoneDialog.isBrowserTimeZoneSelected())
				.as("Browser time zone is not selected")
				.isTrue();
		assertThat(changeTimezoneDialog.isCustomTimeZoneSelected())
				.as("Custom time zone is selected")
				.isFalse();
	}

	/**
	 * Checks that custom time zone is selected in Change Time Zone dialog in CM users menu.
	 *
	 * @param timeZone for custom time zone verification
	 */
	@And("^I see that custom time zone (.+) is selected")
	public void verifyBrowserTimeZoneSelected(final String timeZone) {
		assertThat(changeTimezoneDialog.isBrowserTimeZoneSelected())
				.as("Browser time zone is selected")
				.isFalse();
		assertThat(changeTimezoneDialog.isCustomTimeZoneSelected())
				.as("Custom time zone is not selected")
				.isTrue();
		assertThat(changeTimezoneDialog.getCustomTimeZone())
				.as("Custom time zone value is unexpected")
				.isEqualTo(timeZone);
	}

	/**
	 * Chooses custom timezone in Change Time Zone dialog.
	 *
	 * @param timezone which should be selected from a dropdown
	 */
	@And("^I set (.+) time zone")
	public void chooseCustomTimeZone(final String timezone) {
		changeTimezoneDialog.selectTimezone(timezone);
	}

	/**
	 * Chooses browser timezone in Change Time Zone dialog.
	 *
	 */
	@And("^I choose browser time zone")
	public void chooseBrowserTimeZone() {
		changeTimezoneDialog.selectBrowserTimezone();
	}

	/**
	 * Saves changes in Change Time Zone dialog in CM users menu.
	 */
	@And("^I save changes in Change Time Zone dialog")
	public void saveTimeZoneDialogChanges() {
		changeTimezoneDialog.clickSaveButton();
	}

	/**
	 * Opens Cm user menu.
	 */
	private void openCmUserMenu(){
		new ActivityToolbar(driver).clickUserMenu();
		userMenuDialog = new UserMenuDialog(driver);
	}
}