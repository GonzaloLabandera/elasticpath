/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cortex.dce.payment

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.*
import static com.elasticpath.cortex.dce.payment.PaymentConstants.*
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.CommonSteps
import com.elasticpath.cortexTestObjects.*

class PaymentInstrumentsSteps {

	static String defaultPaymentInstrumentName
	static String preChosenPaymentInstrument
	static String newlyChosenPaymentInstrument
	static String instrumentName

	@Given('^I authenticate as a registered shopper who has a (.+) as their default payment method$')
	static void authenticateWithDefaultPaymentInstrument(String instrumentName) {
		registerNewShopperAndAuthenticate()
		Payment.createDefaultProfilePaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", instrumentName) as List<String>
		)))
	}

	@Given('^a registered shopper has payment instruments saved to his profile$')
	static void addPaymentInstrumentToProfile() {
		registerNewShopperAndAuthenticate()
		Payment.createProfilePaymentInstrumentWithDefaultName()
	}

	@Given('^I authenticate as a registered shopper with a saved payment instrument on my profile$')
	static void authenticateWithSavedPaymentInstrument() {
		registerNewShopperAndAuthenticate()
		Payment.createSavedPaymentInstrumentWithDefaultName()
	}

	private static void registerNewShopperAndAuthenticate() {
		def USERNAME = UUID.randomUUID().toString() + "@elasticpath.com"

		client.authAsAPublicUser(DEFAULT_SCOPE)
				.stopIfFailure()

		registerShopper(TOKEN_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, USERNAME)

		client.authRegisteredUserByName(TOKEN_SCOPE, USERNAME)
	}

	private static void registerShopper(registrationScope, familyName, givenName, password, username) {
		client.authAsAPublicUser(registrationScope)

		client.GET("registrations/$registrationScope/newaccount/form")
				.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}

	@Given('^a registered shopper purchase was made with payment instrument$')
	static void createRegisteredShopperOrderWithPaymentInstrument() {
		client.authRegisteredUserByName(DEFAULT_SCOPE, DEFAULT_SCOPE_TEST_USER)
		CommonMethods.searchAndOpenItemWithKeyword(PURCHASEABLE_NON_SHIPPABLE_ITEM)
		Item.addItemToCart(1)
		Payment.createUnsavedPaymentInstrumentWithDefaultName()
		saveInstrumentDetails()
		Order.submitPurchase()
	}

	@Given('^an anonymous shopper purchase was made with payment instrument$')
	static void createAnonymousShopperOrderWithPaymentInstrument() {
		client.authAsAPublicUser(DEFAULT_SCOPE)
		Profile.createUniqueAddress()
		Profile.addEmailWithoutFollow(TEST_EMAIL_VALUE)
		CommonMethods.searchAndOpenItemWithKeyword(PURCHASEABLE_NON_SHIPPABLE_ITEM)
		Item.addItemToCart(1)
		Payment.createUnsavedPaymentInstrumentWithDefaultName()
		saveInstrumentDetails()
		Order.submitPurchase()
	}

	@And('^I make a purchase with newly created payment instrument for scope (.+) with following skus?$')
	static void purchaseSKUsWithoutDefaultPaymentInstrument(String scope, DataTable dataTable) {
		CommonMethods.addItemsToCart(dataTable)
		Profile.addUSBillingAddress()
		saveInstrumentDetails()
		Order.submitPurchase()
	}

	@And('^my order does not have a payment instrument applied$')
	static void verifyOrderHasNoPaymentInstrument() {
		Order.paymentinstrumentselector()
		assertLinkDoesNotExist(client, "chosen")
		assertLinkDoesNotExist(client, "default")
	}

	@Given('^a free product was purchased without payment$')
	static void purchaseFreeProduct() {
		def userName = UUID.randomUUID().toString() + "@elasticpath.com"
		registerShopper(DEFAULT_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, userName)
		client.authRegisteredUserByName(DEFAULT_SCOPE, userName)
		Profile.createUniqueAddress()
		CommonMethods.searchAndOpenItemWithKeyword(ZERO_DOLLAR_ITEM)
		Item.addItemToCart(1)
		Order.submitPurchase()
	}

	@Given('^I authenticate as a shopper with payment instruments X Y and Z and X as the default$')
	static void authenticateWithSelectedPaymentInstrument() {
		registerAndUseNewShopperWithXYZInstrumentsWhereXIsTheDefault()
		defaultPaymentInstrumentName = Profile.getDefaultPaymentInstrumentName()
	}

	private static void registerAndUseNewShopperWithXYZInstrumentsWhereXIsTheDefault() {
		registerNewShopperAndAuthenticate()
		Payment.createDefaultProfilePaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", DEFAULT_PAYMENT_INSTRUMENT_NAME) as List<String>
		)))
		Payment.createProfilePaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", SECOND_PAYMENT_INSTRUMENT_NAME) as List<String>
		)))
		Payment.createProfilePaymentInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME, DataTable.create(Arrays.asList(
				Arrays.asList("display-name", THIRD_PAYMENT_INSTRUMENT_NAME) as List<String>
		)))
	}

	@Given('^I authenticate as a shopper with payment instrument X as the chosen payment instrument for their order$')
	static void authenticateWithChosenPaymentInstrument() {
		registerAndUseNewShopperWithXYZInstrumentsWhereXIsTheDefault()
	}

	@Given('^I authenticate as a shopper with saved payment instruments X Y and Z and payment instrument X is chosen on their order$')
	static void authenticateWithPreChosenPaymentInstrument() {
		registerAndUseNewShopperWithXYZInstrumentsWhereXIsTheDefault()
		Profile.createUniqueAddress()
		CommonMethods.searchAndAddProductToCart(TOKEN_STORE_PURCHASEABLE_ITEM)
		preChosenPaymentInstrument = Order.getChosenPaymentInstrumentName()
	}

	@When('^I retrieve the default payment instrument on my profile$')
	static void getDefaultPaymentInstrument() {
		Profile.defaultPaymentInstrument()
	}

	@When('^I create a payment instrument for my profile$')
	static void createProfilePaymentInstrument() {
		Payment.createProfilePaymentInstrumentWithDefaultName()
	}

	@When('^a payment instrument is deleted from the profile$')
	static void deleteProfilePaymentInstrument() {
		Profile.deleteInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME)
	}

	@When('^the registered shopper attempts to edit payment instrument$')
	static void editProfilePaymentInstrument() {
		Profile.findInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME)
		CommonMethods.edit(CommonMethods.getSelfUri(), ["name": "edited"])
	}

	@When('^I view the payment instruments available to be selected for my order$')
	static void getOrderPaymentInstruments() {
		Order.paymentinstrumentselector()
	}

	@When('^I create a payment instrument for my order$')
	static void addOrderPaymentInstrument() {
		Payment.createUnsavedPaymentInstrumentWithDefaultName()
	}

	@And('^I retrieve my order$')
	static void getOrder() {
		Order.getOrder()
	}

	@When('^I view the purchase$')
	static void getPurchases() {
		Purchase.resume()
	}

	@When('^I access the payment instruments on my profile$')
	static void getProfilePaymentInstruments() {
		Profile.paymentinstruments()
	}

	@When('^I access default payment instruments selector on my profile$')
	static void getDefaultPaymentInstrumentSelectorOnProfile() {
		Profile.defaultinstrumentselector()
	}

	@When('^I access payment instruments selector choice on my profile$')
	static void getPaymentInstrumentSelectorChoiceOnProfile() {
		Profile.paymentInstrumentChoice()
	}

	@When('^I can go back to payment instruments on my profile$')
	static void getPaymentsInstrumentOnProfile() {
		Profile.paymentInstrumentsFromDefaultPaymentInstrumentSelector()
	}

	@When('^I can go back to choices from choice in profile default payment instrument selector$')
	static void getProfileDefaultPaymentsInstrumentChoices() {
		Profile.paymentInstrumentsChoicesFromChoice()
	}

	@When('^I get the payment instrument details for the chosen payment instrument X$')
	static void getPaymentInstrumentDetails() {
		Order.chosenPaymentInstrumentDescription()
	}

	@When('^I get the payment instrument details for the chosen payment instrument X on profile$')
	static void getChosenPaymentInstrumentDetailsOnProfile() {
		Profile.chosenPaymentInstrumentDescription()
	}

	@When('^I get the payment instrument details for payment instrument choice Y or Z$')
	static void getPaymentChoiceDescription() {
		Order.paymentInstrumentChoiceDescription()
	}

	@When('^I get the payment instrument details for payment instrument choice Y or Z on profile$')
	static void getPaymentChoiceDescriptionOnProfile() {
		Profile.paymentInstrumentChoiceDescription()
	}

	@When('^payment instruments X and Z are now choices as the payment instruments on the order$')
	static void verifyPaymentBecomesChoiceOnOrder() {
		findPaymentInstrumentChoiceFromOrderByName(DEFAULT_PAYMENT_INSTRUMENT_NAME)
		findPaymentInstrumentChoiceFromOrderByName(THIRD_PAYMENT_INSTRUMENT_NAME)
	}

	@When('^payment instruments X and Z are now choices as the payment instruments on the profile$')
	static void verifyPaymentBecomesChoiceOnProfile() {
		findPaymentInstrumentChoiceFromProfileByName(DEFAULT_PAYMENT_INSTRUMENT_NAME)
		findPaymentInstrumentChoiceFromProfileByName(THIRD_PAYMENT_INSTRUMENT_NAME)
	}

	@When('^I select payment instrument Y on the order$')
	static void selectPaymentOnOrder() {
		selectPaymentInstrumentByName(SECOND_PAYMENT_INSTRUMENT_NAME)
		newlyChosenPaymentInstrument = Order.getChosenPaymentInstrumentName()
	}

	@When('^I select payment instrument Y on profile$')
	static void selectPaymentOnProfile() {
		selectProfileDefaultPaymentInstrumentByName(SECOND_PAYMENT_INSTRUMENT_NAME)
		defaultPaymentInstrumentName = Profile.getDefaultPaymentInstrumentName()
	}

	@When('^I complete the purchase for the order$')
	static void submitPurchase() {
		Order.submitPurchase()
	}

	@Then('^I get the default instrument (.+)$')
	static void verifyInstrumentName(String instrumentName) {
		assertThat(client["name"])
				.as("Default instrument name is not as expected")
				.isEqualTo(instrumentName)
	}

	@Then('^the payment instrument is available from their profile$')
	static void verifyInstrumentAddedToProfile() {
		Profile.verifyInstrument(DEFAULT_PAYMENT_CONFIGURATION_NAME)
	}

	@Then('^the payment instrument (.+) is available in profile$')
	static void verifyInstrumentAvailableInProfile(String instrumentName) {
		Profile.verifyInstrument(instrumentName)
	}

	@Then('^the payment instrument (.+) is not available in profile$')
	static void verifyInstrumentNotAvailableInProfile(String instrumentName) {
		assertThat(Profile.findInstrument(instrumentName))
				.as("Payment instrument shouldn't be in profile")
				.isFalse()
	}

	@Then('^the default payment instrument is not (.+)$')
	static void verifyDefaultPaymentInstrumentIsNot(String instrumentName) {
		assertThat(Profile.isDefaultPaymentInstrument(instrumentName))
				.as("Default payment instrument shouldn't be " + instrumentName)
				.isFalse()
	}

	@When('^I create a payment instrument with a valid address and data$')
	static def submitInstrumentWithValidBillingAddress() {
		instrumentName = "instrument " + UUID.randomUUID().toString()
		client.createpaymentinstrumentaction(
				[
						"billing-address"                       : [
								organization  : "organization corp",
								"phone-number": "800-267-8888",
								address       : ["country-name"    : "CA",
												 "extended-address": "extended address",
												 "locality"        : "Vancouver",
												 "postal-code"     : "V7V7V7",
												 "region"          : "BC",
												 "street-address"  : "123 Broadway"],
								name          : ["family-name": "family-name",
												 "given-name" : "given-name"]
						],
						"payment-instrument-identification-form": [
								"PIC Field A" : "Field A",
								"PIC Field B" : "Field B",
								"display-name": instrumentName
						]
				]
		)
	}

	@When('^^I create a payment instrument with a blank address and data$')
	static def submitInstrumentWithBlankBillingAddress() {
		instrumentName = "instrument " + UUID.randomUUID().toString()
		client.createpaymentinstrumentaction(
				[
						"billing-address": [
								organization  : " ",
								"phone-number": " ",
								address       : ["country-name"    : " ",
												 "extended-address": " ",
												 "locality"        : " ",
												 "postal-code"     : " ",
												 "region"          : " ",
												 "street-address"  : " "],
								name          : ["family-name": " ",
												 "given-name" : " "]
						],
						data             : [
								"PIC Field A" : "Field A",
								"PIC Field B" : "Field B",
								"display-name": instrumentName
						]
				]
		)
	}

	@Then('^it no longer shows up in his list of saved payment instruments on his profile$')
	static void verifyInstrumentDeletedFromProfile() {
		Profile.paymentinstruments()

		boolean deletedInstrumentFound = client.body.links.findAll {
			link -> link.rel == "element"
		}.any {
			link -> client.GET(link.href)["name"] == DEFAULT_PAYMENT_CONFIGURATION_NAME
		}

		assertThat(deletedInstrumentFound)
				.as("The deleted payment instrument should have not been found")
				.isFalse()
	}

	@Then('^I see only the payment instrument available from my profile$')
	static void verifyOnlyProfileInstrumentsAvailable() {
		def name = Order.getChosenPaymentInstrumentName()

		assertThat(name)
				.as("The chosen payment instrument on the order is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_CONFIGURATION_NAME)

		Order.paymentinstrumentselector()
		assertLinkDoesNotExist(client, "choice")
	}

	@Then('^the new payment instrument will be set for their order')
	static void verifyNewPaymentSetOnOrder() {
		assertThat(Order.getChosenPaymentInstrumentName())
				.as("The chosen payment method on the order is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_CONFIGURATION_NAME)
	}

	@And('^the (?:new|default) payment instrument is (?:not available|removed) from (?:their|current) profile')
	static void verifyPaymentInstrumentsNotInProfile() {
		Profile.paymentinstruments()
		assertLinkDoesNotExist(client, "element")
		assertLinkDoesNotExist(client, "default")
	}

	@Then('^the default payment instrument is automatically applied to the order$')
	static void verifyDefaultPaymentInstrumentAppliedToOrder() {
		Order.paymentinstrumentselector()
		assertLinkExists(client, "chosen")

		def name = client.chosen().description()["name"]

		assertThat(name)
				.as("The chosen payment instrument on the order is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_CONFIGURATION_NAME)

		Order.paymentinstrumentselector()
		assertLinkDoesNotExist(client, "choice")
	}

	@Then('^the payment instrument is a (.+) type$')
	static void paymentInstrumentType(String type) {
		assertThat(client.body.self.type)
				.as("The payment instrument type is not as expected")
				.isEqualTo(type)
	}

	@And('^the purchase payment instrument name matches the instrument used to create the purchase$')
	static void verifyPurchasePaymentInstrument() {
		assertThat(Purchase.getPaymentInstrumentName())
				.as("The purchase payment instrument name is not as expected")
				.isEqualTo(instrumentName)
	}

	@Then('^the paymentinstruments link is empty$')
	static void verifyPaymentInstrumentsEmpty() {
		Purchase.purchasepaymentinstruments()
		assertLinkDoesNotExist(client, "element")
	}

	@And('^the billing address matches the billing address used to create the purchase$')
	static void verifyPurchaseBillingAddress(String address, String name) {
		assertThat(client["billing-address"]["address"])
				.as("the billing-address address is not as expected")
				.isEqualTo(address)
		assertThat(client["billing-address"]["name"])
				.as("the billing-address name is not as expected")
				.isEqualTo(name)
	}

	@Then('^the field (.+) has value (.+)$')
	static void verifyFieldHasValue(String field, String value) {
		assertThat(client[field])
				.as("the field " + field + " is not as expected")
				.isEqualTo(value)
	}

	@Then('^the list contains payment instruments X Y and Z and X is displayed as the default$')
	static void verifyListContainsTokens() {
		FindPaymentInstrument(DEFAULT_PAYMENT_INSTRUMENT_NAME)
		FindPaymentInstrument(SECOND_PAYMENT_INSTRUMENT_NAME)
		FindPaymentInstrument(THIRD_PAYMENT_INSTRUMENT_NAME)
		client.default()
		assertThat(client["name"])
				.as("The default instrument name is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_INSTRUMENT_NAME)
	}

	@Then('^the payment instrument details display the correct values$')
	static void verifyPaymentInstrumentDetails() {
		assertThat(client["name"])
				.as("The chosen payment instrument was not the default payment instrument")
				.isEqualTo(DEFAULT_PAYMENT_INSTRUMENT_NAME)
	}

	@Then('^the payment instrument details display the correct values for that choice$')
	static void verifyPaymentInstrumentDetailsForChoice() {
		def paymentInstrumentName = client["name"]

		assertThat(paymentInstrumentName == SECOND_PAYMENT_INSTRUMENT_NAME || paymentInstrumentName == THIRD_PAYMENT_INSTRUMENT_NAME)
				.as("The chosen payment instrument was not the default payment instrument")
				.isTrue()
	}

	@Then('^payment instrument Y is now chosen as the payment instrument on the order$')
	static void verifyPaymentIsSelectedOnOrder() {
		//Clean up test by completing the purchase
		Order.submitPurchase()
		assertThat(newlyChosenPaymentInstrument != preChosenPaymentInstrument)
				.as("The newly selected payment instrument should now be chosen for the order")
				.isTrue()
	}

	@Then('^payment instrument X is chosen as the payment instrument for my purchase$')
	static void verifyChosenPaymentInstrument() {
		assertThat(Order.getChosenPaymentInstrumentName())
				.as("The payment instrument selected was not the default payment instrument")
				.isEqualTo(defaultPaymentInstrumentName)
	}

	@Then('^payment instrument Y is chosen as the payment instrument for my purchase$')
	static void verifyProfileDefaultPaymentInstrument() {
		assertThat(Profile.getDefaultPaymentInstrumentName())
				.as("The payment instrument selected was not the default payment instrument")
				.isEqualTo(defaultPaymentInstrumentName)
	}

	@Then('^the chosen payment instrument is displayed correctly as the payment instrument for the purchase$')
	static void verifyChosenPurchasePaymentInstrument() {
		Purchase.selectPaymentInstruments(preChosenPaymentInstrument)
		assertThat(client["name"])
				.as("The payment instrument used for purchase should be the same as the chosen payment instrument")
				.isEqualTo(preChosenPaymentInstrument)
	}

	private static void saveInstrumentDetails() {
		instrumentName = Order.getChosenPaymentInstrumentName()
	}

	private static void FindPaymentInstrument(def instrumentName) {
		client.findElement {
			element ->
				element["name"] == instrumentName
		}.list()
	}

	@Given('^I have created (.+) payment instrument on my profile with the following fields:$')
	static def createPaymentInstrument(final String storePaymentProviderConfigName, final DataTable paymentInstrumentFields) {
		Payment.createProfilePaymentInstrument(storePaymentProviderConfigName, paymentInstrumentFields)
	}

	@When('^I create payment instrument without supplying any fields$')
	static def createPaymentInstrument() {
		client.paymentinstrumentform()
				.createpaymentinstrumentaction()
	}

	@When('^I open payment instrument form$')
	static def openPaymentInstrumentForm() {
		client.paymentinstrumentform()
	}

	@When('^I create payment instrument supplying following fields:$')
	static def createPaymentInstrument(DataTable dataTable) {
		client.paymentinstrumentform()
				.createpaymentinstrumentaction(["payment-instrument-identification-form": dataTable.asMap(String, String)])
	}

	@When('^I create a saved (.+) payment instrument from order supplying the following fields:$')
	static def createSavedPaymentInstrumentFromOrder(String configurationName, DataTable dataTable) {
		Payment.createSavedPaymentInstrument(configurationName, dataTable)
	}

	@When('^I create an unsaved (.+) payment instrument from order supplying following fields:$')
	static def createUnsavedPaymentInstrumentFromOrder(String configurationName, DataTable dataTable) {
		Payment.createUnsavedPaymentInstrument(configurationName, dataTable)
	}

	@When('^I create a default (.+) payment instrument from order supplying following fields:$')
	static def createDefaultPaymentInstrumentFromOrder(String configurationName, DataTable dataTable) {
		Payment.createDefaultPaymentInstrument(configurationName, dataTable)
	}

	@When('^I create a default (.+) payment instrument from profile supplying following fields:$')
	static def createDefaultPaymentInstrumentFromProfile(String configurationName, DataTable dataTable) {
		Payment.createDefaultProfilePaymentInstrument(configurationName, dataTable)
	}

	@When('^I (?:select|deselect) (.+) payment instrument$')
	static def selectPaymentInstrumentByName(String name) {
		boolean paymentInstrumentFound = false

		Order.paymentinstrumentselector()

		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					client.GET(instrumentHref)
							.selectaction()
							.follow()
							.stopIfFailure()
					paymentInstrumentFound = true
				}
			}
		}
		assertThat(paymentInstrumentFound)
				.as("Expected selectable payment instrument with the name " + name + " was not found.")
				.isTrue()
	}

	@When('^I (?:select|deselect) (.+) profile default payment instrument$')
	static def selectProfileDefaultPaymentInstrumentByName(String name) {
		boolean paymentInstrumentFound = false

		Profile.defaultinstrumentselector()

		selectProfilePaymentInstrumentByName(name, paymentInstrumentFound)
	}

	private static void selectProfilePaymentInstrumentByName(String name, boolean paymentInstrumentFound) {
		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					client.GET(instrumentHref)
							.selectaction()
							.follow()
							.stopIfFailure()
					paymentInstrumentFound = true
				}
			}
		}

		assertThat(paymentInstrumentFound)
				.as("Expected selectable profile payment instrument with the name " + name + " was not found.")
				.isTrue()
	}

	@When('^I delete (.+) payment instrument from order$')
	static def deletePaymentInstrumentFromOrderByName(String name) {
		boolean paymentInstrumentFound = false

		Order.paymentinstrumentselector()

		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					client.DELETE(client.body.self.uri)
							.stopIfFailure()
					paymentInstrumentFound = true
				}
			}
		}
		assertThat(paymentInstrumentFound)
				.as("Expected selectable payment instrument with the name " + name + " was not found.")
				.isTrue()
	}

	static def findPaymentInstrumentChoiceFromOrderByName(String name) {
		boolean paymentInstrumentFound = false

		Order.paymentinstrumentselector()

		client.body.links.find {
			if (it.rel == "choice") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					paymentInstrumentFound = true
				}
			}
		}
		assertThat(paymentInstrumentFound)
				.as("Expected payment instrument choice with the name " + name + " was not found.")
				.isTrue()
	}

	static def findPaymentInstrumentChoiceFromProfileByName(String name) {
		boolean paymentInstrumentFound = false

		Profile.defaultinstrumentselector()

		findPaymentInstrumentChoiceByName(name, paymentInstrumentFound)
	}

	private static void findPaymentInstrumentChoiceByName(String name, boolean paymentInstrumentFound) {
		client.body.links.find {
			if (it.rel == "choice") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					paymentInstrumentFound = true
				}
			}
		}
		assertThat(paymentInstrumentFound)
				.as("Expected payment instrument choice with the name " + name + " was not found.")
				.isTrue()
	}

	@When('^I access (.+) payment instrument from order$')
	static def accessOrderPaymentInstrumentByName(String name) {
		boolean paymentInstrumentExists = false

		Order.paymentinstrumentselector()

		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
						.description()
				if (client["name"] == name) {
					paymentInstrumentExists = true
				}
			}
		}
		assertThat(paymentInstrumentExists)
				.as("Expected selectable payment instrument with the name " + name + " was not found.")
				.isTrue()
	}

	@When('^I access (.+) payment instrument from profile$')
	static def accessProfilePaymentInstrumentByName(String name) {
		Profile.paymentinstruments()

		checkPaymentInstrumentName(name)
	}

	@When('^I access (.+) payment instrument from account$')
	static def accessAccountPaymentInstrumentByName(String name) {
		checkPaymentInstrumentName(name)
	}

	@When('^I access payment instrument with default name from purchase$')
	static def accessPurchasePaymentInstrumentByDefaultName() {
		Purchase.purchasepaymentinstruments()

		checkPaymentInstrumentName(DEFAULT_PAYMENT_CONFIGURATION_NAME)
	}

	private static void checkPaymentInstrumentName(String name) {
		boolean paymentInstrumentExists = false

		client.body.links.find {
			if (it.rel == "element") {
				String instrumentHref = it.href
				client.GET(instrumentHref)
				if (client["name"] == name) {
					paymentInstrumentExists = true
				}
			}
		}

		assertThat(paymentInstrumentExists)
				.as("Expected selectable payment instrument with the name " + name + " was not found.")
				.isTrue()
	}

	@Then('^I should see (.+) payment instrument created$')
	static def verifyPaymentInstruction(String name) {
		client.follow().stopIfFailure()
		assertThat(client["name"])
				.as("\"name\" is not found")
				.isEqualTo(name)
		assertThat(client.body['payment-instrument-identification-attributes'])
				.as("\"payment-instrument-identification-attributes\" property not found")
				.isNotNull()
	}

	@Then('^I should see the created payment instrument$')
	static def verifyPaymentInstrument() {
		client.follow().stopIfFailure()
		assertThat(client["name"])
				.as("\"name\" is not found")
				.isEqualTo(instrumentName)
		assertThat(client.body['payment-instrument-identification-attributes'])
				.as("\"payment-instrument-identification-attributes\" property not found")
				.isNotNull()
	}

	@Then('^I should see a payment instrument option with the following fields:$')
	static def verifyPaymentInstrumentOptionFields(DataTable paymentInstrumentFields) {
		Map paymentInstrumentDataMap = paymentInstrumentFields.asMap(String, String)
		client.body.links.find {
			if (it.rel == "choice" || it.rel == "chosen") {
				client.GET(it.href)
				client.description()
				assertThat(client.body['payment-instrument-identification-attributes'])
						.as("\"payment-instrument-identification-attributes\" property not found")
						.isNotNull()
				for (String key : paymentInstrumentDataMap.keySet()) {
					String expectedValue = paymentInstrumentDataMap.get(key)
					String actualValue = client.body[key]
					assertThat(actualValue)
							.as("The expected value for " + key + " is " + expectedValue + " but actual value is " + actualValue)
							.isEqualTo(expectedValue)
				}
			}
		}
	}

	@Then('^I should see a payment instrument (choice|chosen|default) with the following fields:$')
	static def verifyPaymentInstrumentChoiceFields(String paymentInstrumentStatus, DataTable paymentInstrumentFields) {
		Map paymentInstrumentDataMap = paymentInstrumentFields.asMap(String, String)

		Order.paymentinstrumentselector()

		client.body.links.find {
			if (it.rel == paymentInstrumentStatus) {
				client.GET(it.href)
				if (it.rel == "choice" || it.rel == "chosen") {
					client.description()
				}
				assertThat(client.body['payment-instrument-identification-attributes'])
						.as("\"payment-instrument-identification-attributes\" property not found")
						.isNotNull()
				for (String key : paymentInstrumentDataMap.keySet()) {
					String expectedValue = paymentInstrumentDataMap.get(key)
					String actualValue = client.body[key]
					assertThat(actualValue)
							.as("The expected value for " + key + " is " + expectedValue + " but actual value is " + actualValue)
							.isEqualTo(expectedValue)
				}
			}
		}
	}

	@Then('^I should see a payment instrument with the following fields:$')
	static def verifyPaymentInstrumentFields(DataTable paymentInstrumentFields) {
		Map paymentInstrumentDataMap = paymentInstrumentFields.asMap(String, String)
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				assertThat(client.body['payment-instrument-identification-attributes'])
						.as("\"payment-instrument-identification-attributes\" property not found")
						.isNotNull()
				for (String key : paymentInstrumentDataMap.keySet()) {
					String expectedValue = paymentInstrumentDataMap.get(key)
					String actualValue = client.body[key]
					assertThat(actualValue)
							.as("The expected value for " + key + " is " + expectedValue + " but actual value is " + actualValue)
							.isEqualTo(expectedValue)
				}
			}
		}
	}

	@Then('^payment instrument with name (.+) is selected for order')
	static def verifyPaymentInstrumentSelected(String name) {
		Order.paymentinstrumentselector()

		CommonSteps.verifyLinkExists("chosen")

		client.chosen()
				.description()
		assertThat(client["name"])
				.as("\"name\" is not found")
				.isEqualTo(name)

	}

	@Then('^The payment instrument form does not provide a (.+) field$')
	static def verifyPaymentInstrumentFormNoSave(String fieldName) {
		client.paymentinstrumentform()
		assertThat(client.body[fieldName])
				.as("The \"" + fieldName + "\" field appears when it should not appear.")
				.isNull()
	}

	@Then('^I should see (.+) payment (?:instrument|instruments) on my order$')
	static def verifyPaymentInstrumentCountOnOrder(int expectedPaymentInstrumentCount) {
		Order.paymentinstrumentselector()

		int actualInstrumentCount = 0
		client.body.links.findAll {
			if (it.rel == "choice" || it.rel == "chosen") {
				actualInstrumentCount++
			}
		}
		assertThat(actualInstrumentCount)
				.as("Expected number of payment instruments is " + expectedPaymentInstrumentCount + ", but actual value is " + actualInstrumentCount)
				.isEqualTo(expectedPaymentInstrumentCount)
	}

	@Then('^current profile has payment instrument with name (.+)')
	static def verifyPaymentInstrumentSelectedInProfile(String name) {
		def names = []
		client.GET("/")
				.defaultprofile()
				.paymentinstruments()

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				names.add(client["name"])
			}
		}
		assertThat(names).contains(name)
	}

	@Then('^the payments form contains the following fields$')
	static void verifyFormContainsTopLevelFields(DataTable dataTable) {
		def mapList = dataTable.asList(String)
		mapList.each {
			assertThat(client[it])
					.as(it + " field not found")
					.isNotNull()
		}
	}

	@Then('^the instrument creation form does not contain the following fields$')
	static void verifyFormDoesNotContainTopLevelFields(DataTable dataTable) {
		def mapList = dataTable.asList(String)
		mapList.each {
			assertThat(client[it])
					.as(it + " field found, but not expected")
					.isNull()
		}
	}
}
