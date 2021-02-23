/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.uat.stepdefs;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.service.order.OrderService;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.uat.ScenarioContextValueHolder;

/**
 * Step Definitions for OrderHold-based functionality.
 */
public class OrderHoldStepDefinitions {

	private static final String ON_HOLD_NOTIFICATION_EMAIL_SETTING_PATH = "COMMERCE/SYSTEM/ONHOLD/holdNotificationRecipientEmail";

	@Autowired
	private ScenarioContextValueHolder<SimpleStoreScenario> storeScenarioHolder;

	@Autowired
	private TestApplicationContext tac;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ScenarioContextValueHolder<Runnable> emailSendingCommandHolder;

	/**
	 * Update the setting value for the ON_HOLD_NOTIFICATION_EMAIL_SETTING_PATH with the specified email address.
	 * @param emailAddress the email address the hold notification email should be sent to
	 */
	@And("^the email address (.+) is configured as the on hold notification email address$")
	public void theEmailAddressIsConfiguredAsTheOnHoldNotificationEmailAddress(final String emailAddress) {
		tac.getPersistersFactory().getSettingsTestPersister().updateSettingValue(
				ON_HOLD_NOTIFICATION_EMAIL_SETTING_PATH,
				storeScenarioHolder.get().getStore().getCode(),
				emailAddress
		);
	}

	/**
	 * Invoke the service that executes the order hold notification job.
	 */
	@When("^the order hold notification job runs$")
	public void theOrderHoldNotificationJobRuns() {
		emailSendingCommandHolder.set(() -> orderService.sendOrderHoldNotificationEvent(storeScenarioHolder.get().getStore().getCode()));
	}
}