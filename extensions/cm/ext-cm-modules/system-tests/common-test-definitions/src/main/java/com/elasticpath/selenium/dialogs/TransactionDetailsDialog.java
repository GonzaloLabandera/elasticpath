/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.elasticpath.selenium.domainobjects.PaymentConfiguration;

/**
 * View Payment Details Dialog.
 */
public class TransactionDetailsDialog extends AbstractDialog {
	/**
	 * CSS selector used to identify the dialog.
	 */
	public static final String TRANSACTION_DETAIL_PARENT = "div[automation-id='com.elasticpath.cmclient.fulfillment"
			+ ".FulfillmentMessages.OrderPaymentsHistorySection_ViewPaymentDetails_WindowTitle']";
	public static final String ORDER_PAYMENT_TABLE = TRANSACTION_DETAIL_PARENT + " div[widget-id='Order Payment'][widget-type='Table']";
	private static final String ORDER_PAYMENT_COLUMN = "div[column-id='%s']";
	private static final String ORDER_PAYMENT_COLUMN_VALUE = "~div[column-num='1']";
	private static final String ORDER_PAYMENT_DATA_TABLE = TRANSACTION_DETAIL_PARENT + " div[widget-id='Order Payment Data'][widget-type='Table']";
	private static final String ORDER_PAYMENT_DATA_COLUMN = ORDER_PAYMENT_DATA_TABLE + " div[column-id='%s']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public TransactionDetailsDialog(final WebDriver driver) {
		super(driver);
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(TRANSACTION_DETAIL_PARENT));
	}

	/**
	 * Verifies order transaction fields values.
	 *
	 * @param orderPaymentDetailsMap Order Payment Details map.
	 */
	public void verifyOrderPaymentDetails(final PaymentConfiguration paymentConfiguration, final Map<String, String> orderPaymentDetailsMap) {
		orderPaymentDetailsMap.forEach((key, value) -> {
			final String configurableFieldColumnCSS = String.format(ORDER_PAYMENT_COLUMN, key);
			selectItemInDialog(ORDER_PAYMENT_TABLE, configurableFieldColumnCSS, key, StringUtils.EMPTY);

			if (("Method").equals(key) && !(StringUtils.EMPTY).equals(paymentConfiguration.getConfigurationName())) {
				value = paymentConfiguration.getConfigurationName();
			}
			assertThat(getOptionalElementText(By.cssSelector(configurableFieldColumnCSS + ORDER_PAYMENT_COLUMN_VALUE)))
					.as("Expected payment details value not match")
					.isEqualTo(value);
		});
		clickClose();
	}

	/**
	 * Verifies Order Payment Data
	 * @param orderPaymentDataMap map
	 */
	public void verifyOrderPaymentData(final Map<String, String> orderPaymentDataMap) {
		orderPaymentDataMap.forEach((key, value) -> {
			String configurableFieldColumnCSS = String.format(ORDER_PAYMENT_DATA_COLUMN, key);
			selectItemInDialog(ORDER_PAYMENT_DATA_TABLE, configurableFieldColumnCSS, key, "");
			assertThat(getOptionalElementText(By.cssSelector(configurableFieldColumnCSS + ORDER_PAYMENT_COLUMN_VALUE)))
					.as("Expected order payment data value not match")
					.isEqualTo(value);
		});
	}

	private String getOptionalElementText(final By selector) {
		setWebDriverImplicitWait(0);
		final List<WebElement> elements = getDriver().findElements(selector);
		setWebDriverImplicitWaitToDefault();
		return elements.isEmpty() ? "" : elements.get(0).getText();
	}

}
