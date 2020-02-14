/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_PAYMENT_CONFIGURATION_NAME

import com.google.common.collect.ImmutableMap
import cucumber.api.DataTable

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Payment.
 */
class Payment extends CommonMethods {

	static def createDefaultProfilePaymentInstrument(String configurationName) {
		createDefaultProfilePaymentInstrument(configurationName, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", configurationName + " " + UUID.randomUUID().toString()) as List<String>
		)))
	}

	static def createProfilePaymentInstrument(String configurationName) {
		createProfilePaymentInstrument(configurationName, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", configurationName + " " + UUID.randomUUID().toString()) as List<String>
		)))
	}

	static def createProfilePaymentInstrumentWithDefaultName() {
		createProfilePaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", DEFAULT_PAYMENT_CONFIGURATION_NAME) as List<String>
		)))
	}

	static def createProfilePaymentInstrument(String configurationName, DataTable dataTable) {
		Profile.getPaymentConfigurationWithName(configurationName)
		Map configurationMap = ImmutableMap.of(
				"default-on-profile", "false",
				"payment-instrument-identification-form", dataTable.asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static def createDefaultProfilePaymentInstrument(String configurationName, DataTable dataTable) {
		Profile.getPaymentConfigurationWithName(configurationName)
		Map configurationMap = ImmutableMap.of(
				"default-on-profile", "true",
				"payment-instrument-identification-form", dataTable.asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static def createSavedPaymentInstrumentWithDefaultName() {
		createSavedPaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", DEFAULT_PAYMENT_CONFIGURATION_NAME) as List<String>
		)))
	}

	static def createSavedPaymentInstrument(String configurationName) {
		createSavedPaymentInstrument(configurationName, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", configurationName + " " + UUID.randomUUID().toString()) as List<String>
		)))
	}

	static def createUnsavedPaymentInstrumentWithDefaultName() {
		createUnsavedPaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", DEFAULT_PAYMENT_CONFIGURATION_NAME) as List<String>
		)))
	}

	static def createUnsavedPaymentInstrument(String configurationName) {
		createUnsavedPaymentInstrument(configurationName, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", configurationName + " " + UUID.randomUUID().toString()) as List<String>
		)))
	}

	static def createDefaultPaymentInstrument(String configurationName) {
		createDefaultPaymentInstrument(configurationName, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", configurationName + " " + UUID.randomUUID().toString()) as List<String>
		)))
	}

	static def createSavedPaymentInstrument(String configurationName, DataTable dataTable) {
		Order.getPaymentConfigurationWithName(configurationName)
		Map configurationMap = ImmutableMap.of(
				"save-on-profile", "true",
				"payment-instrument-identification-form", dataTable.asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static def createUnsavedPaymentInstrument(String configurationName, DataTable dataTable) {
		Order.getPaymentConfigurationWithName(configurationName)
		Map configurationMap = ImmutableMap.of(
				"save-on-profile", "false",
				"payment-instrument-identification-form", dataTable.asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static def createInstrumentUsingSelectedCartOrder(){
		client.paymentmethodinfo()
				.stopIfFailure()
		openLinkRelWithFieldWithValue("element", "name", DEFAULT_PAYMENT_CONFIGURATION_NAME)
		Map configurationMap = ImmutableMap.of(
				"save-on-profile", "false",
				"payment-instrument-identification-form", DataTable.create(Arrays.asList(
				Arrays.asList("display-name", DEFAULT_PAYMENT_CONFIGURATION_NAME + " " + UUID.randomUUID().toString()) as List<String>
		)).asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static def createDefaultPaymentInstrument(String configurationName, DataTable dataTable) {
		Order.getPaymentConfigurationWithName(configurationName)
		Map configurationMap = ImmutableMap.of(
				"save-on-profile", "false",
				"default-on-profile", "true",
				"payment-instrument-identification-form", dataTable.asMap(String, String)
		)
		createPaymentInstrumentWithConfigurationAndData(configurationMap)
	}

	static void createPaymentInstrumentWithConfigurationAndData(Map paymentInstrumentConfiguration) {
		client.paymentinstrumentform()
				.createpaymentinstrumentaction(paymentInstrumentConfiguration)
	}

}
