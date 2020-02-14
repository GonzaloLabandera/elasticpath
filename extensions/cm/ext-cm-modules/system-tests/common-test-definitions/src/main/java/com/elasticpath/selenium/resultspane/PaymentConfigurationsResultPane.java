package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.common.AbstractPageObject;
import com.elasticpath.selenium.dialogs.ConfirmDialog;
import com.elasticpath.selenium.dialogs.CreatePaymentConfigurationDialog;
import com.elasticpath.selenium.dialogs.EditPaymentConfigurationDialog;

/**
 * Payment Configuration Result Pane.
 */
public class PaymentConfigurationsResultPane extends AbstractPageObject {
	private static final String PAYMENT_CONFIGURATIONS_LIST_PARENT_CSS = "div[widget-id='Payment Configurations'][widget-type='Table'] ";
	private static final String PAYMENT_CONFIGURATION_LIST_CSS = PAYMENT_CONFIGURATIONS_LIST_PARENT_CSS + "div[column-num='0'][column-id='%s']";
	private static final String PAYMENT_CONFIGURATION_STATUS_CSS = PAYMENT_CONFIGURATIONS_LIST_PARENT_CSS + "div[row-id='%s'] div[column-num='4']";
	private static final String PAYMENT_CONFIGURATION_MESSAGE_PARENT = "div[automation-id='com.elasticpath.cmclient.admin.paymentconfigurations"
			+ ".AdminPaymentConfigurationMessages";
	private static final String CREATE_PAYMENT_CONFIGURATION_BUTTON = PAYMENT_CONFIGURATION_MESSAGE_PARENT + ".CreatePaymentConfiguration']";
	private static final String EDIT_PAYMENT_CONFIGURATION_BUTTON = PAYMENT_CONFIGURATION_MESSAGE_PARENT + ".EditPaymentConfiguration']";
	private static final String ACTIVATE_BUTTON = PAYMENT_CONFIGURATION_MESSAGE_PARENT + ".ActivatePaymentConfiguration']";
	private static final String DISABLE_BUTTON = PAYMENT_CONFIGURATION_MESSAGE_PARENT + ".DisablePaymentConfiguration']";
	private static final String DISABLE_DENIED_CONFIRMATION = PAYMENT_CONFIGURATION_MESSAGE_PARENT
			+ ".PaymentConfiguration_DisableDeniedConfirmation'";
	private static final String DESCRIPTION_NAME = "div[parent-widget-id='Payment Configuration Properties Table'] div[column-num='0']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public PaymentConfigurationsResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Clicks Create Payment Configuration button.
	 *
	 * @return CreatePaymentConfigurationDialog
	 */
	public CreatePaymentConfigurationDialog clickCreatePaymentConfigurationButton() {
		clickButton(CREATE_PAYMENT_CONFIGURATION_BUTTON, "Create");
		return new CreatePaymentConfigurationDialog(getDriver());
	}

	/**
	 * Clicks Edit Payment Configuration button.
	 *
	 * @return EditPaymentConfigurationDialog
	 */
	public EditPaymentConfigurationDialog clickEditPaymentConfigurationButton() {
		clickButton(EDIT_PAYMENT_CONFIGURATION_BUTTON, "Edit");
		return new EditPaymentConfigurationDialog(getDriver());
	}

	/**
	 * Verifies if given payment configuration exists in list.
	 * @param paymentConfigName the Payment Configuration name
	 */
	public void verifyPaymentConfigurationExist(final String paymentConfigName) {
		assertThat(selectItemInCenterPaneWithoutPagination(PAYMENT_CONFIGURATIONS_LIST_PARENT_CSS, PAYMENT_CONFIGURATION_LIST_CSS, paymentConfigName,
				"Configuration Name"))
				.as("Payment Configuration does not exist in the list - " + paymentConfigName)
				.isTrue();
	}

	/**
	 * Activates selected payment configuration.
	 */
	public void activatePaymentConfiguration() {
		clickButton(ACTIVATE_BUTTON, "Activate");
		new ConfirmDialog(getDriver()).clickOKButton("AdminPaymentConfigurationMessages.PaymentConfiguration_ActivateDialogTitle");
	}

	/**
	 * Disable selected payment configuration.
	 */
	public void disablePaymentConfiguration() {
		clickDisableButton();
		new ConfirmDialog(getDriver()).clickOKButton("AdminPaymentConfigurationMessages.PaymentConfiguration_DisableDialogTitle");
	}

	/**
	 * Clicks on Disable button.
	 */
	public void clickDisableButton() {
		clickButton(DISABLE_BUTTON, "Disable");
	}

	/**
	 * Disable denied for selected payment configuration.
	 */
	public void verifyDisableDenied() {
		assertThat(getWaitDriver().waitForElementToBeInteractable(DISABLE_DENIED_CONFIRMATION))
				.as("Payment Configuration Disable Denied dialog did not occur.")
				.isTrue();
		new ConfirmDialog(getDriver()).clickOKButton("AdminPaymentConfigurationMessages"
				+ ".PaymentConfiguration_DisableDeniedDialogTitle");
	}

	/**
	 * Verifies Payment Configuration state.
	 *
	 * @param paymentConfigName String
	 * @param state             String
	 */
	public void verifyPaymentConfigurationStatus(final String paymentConfigName, final String state) {
		verifyPaymentConfigurationExist(paymentConfigName);
		assertThat(getDriver().findElement(By.cssSelector(String.format(PAYMENT_CONFIGURATION_STATUS_CSS, paymentConfigName))).getText())
				.as("Unexpected Payment Configuration State")
				.isEqualTo(state);
	}

	/**
	 * Verifies if Activate button is disabled.
	 */
	public void verifyActivateButtonDisabled() {
		assertThat(isButtonEnabled(ACTIVATE_BUTTON))
				.as("Payment Configuration Activate button is not disabled")
				.isFalse();
	}

	/**
	 * Verifies if Disable button is disabled.
	 */
	public void verifyDisableButtonDisabled() {
		assertThat(isButtonEnabled(DISABLE_BUTTON))
				.as("Payment Configuration Disable button is not disabled")
				.isFalse();
	}

	/**
	 * Verifies the description order.
	 */
	public void verifyDescriptionOrder(final List<String> descriptions) {
		assertThat(getWaitDriver()
				.waitForElementsListVisible(By.cssSelector(DESCRIPTION_NAME))
				.stream()
				.map(WebElement::getText)
				.collect(Collectors.toList()))
				.containsExactlyElementsOf(descriptions);
	}
}
