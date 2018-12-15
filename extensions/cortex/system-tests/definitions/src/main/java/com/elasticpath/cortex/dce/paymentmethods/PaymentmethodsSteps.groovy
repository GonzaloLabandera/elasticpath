package com.elasticpath.cortex.dce.paymentmethods

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE_TEST_USER
import static com.elasticpath.cortex.dce.SharedConstants.FAMILY_NAME
import static com.elasticpath.cortex.dce.SharedConstants.GIVEN_NAME
import static com.elasticpath.cortex.dce.SharedConstants.PASSWORD
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN_DISPLAY_NAME
import static com.elasticpath.cortex.dce.SharedConstants.TOKEN_SCOPE
import static PaymentmethodsConstants.DEFAULT_PAYMENT_TOKEN_DISPLAY_VALUE
import static PaymentmethodsConstants.SECOND_PAYMENT_TOKEN_DISPLAY_VALUE
import static PaymentmethodsConstants.THIRD_PAYMENT_TOKEN_DISPLAY_VALUE
import static PaymentmethodsConstants.PURCHASEABLE_NON_SHIPPABLE_ITEM
import static PaymentmethodsConstants.TEST_TOKEN_DISPLAY_VALUE_X
import static PaymentmethodsConstants.TEST_TOKEN_DISPLAY_VALUE_Y
import static PaymentmethodsConstants.TEST_TOKEN_VALUE_X
import static PaymentmethodsConstants.TEST_TOKEN_VALUE_Y
import static PaymentmethodsConstants.TEST_USER_WITH_TOKENS
import static PaymentmethodsConstants.TOKEN_STORE_PURCHASEABLE_ITEM
import static PaymentmethodsConstants.ZERO_DOLLAR_ITEM
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile
import com.elasticpath.cortexTestObjects.Purchase

class PaymentmethodsSteps {

	static String defaultPaymentMethodDisplayName
	static String preChosenPaymentMethod
	static String newlyChosenPaymentMethod
	static String token

	@Given('^I authenticate as a registered shopper who has a token as their default payment method$')
	static void authenticateWithDefaultPaymentToken() {
	//  default test user has tokens as payment method by default
		client.authRegisteredUserByName(DEFAULT_SCOPE, DEFAULT_SCOPE_TEST_USER)
	}

	@Given('^a registered shopper has payment methods saved to his profile$')
	static void addPaymentTokenToProfile() {
		registerNewShopperAndAuthenticate()
		Profile.addToken(TEST_TOKEN_DISPLAY_VALUE_X, TEST_TOKEN_VALUE_X)
		Profile.addToken(TEST_TOKEN_DISPLAY_VALUE_Y, TEST_TOKEN_VALUE_Y)
	}

	@Given('^I authenticate as a registered shopper with a saved payment method on my profile$')
	static void authenticateWithSavedPaymentToken() {
		registerNewShopperAndAuthenticate()
		Profile.addToken(TEST_TOKEN_DISPLAY_VALUE_X, TEST_TOKEN_VALUE_X)
	}

	@Given('^a purchase was made with payment token$')
	static void createOrderWithPaymentToken() {
		client.authRegisteredUserByName(DEFAULT_SCOPE, DEFAULT_SCOPE_TEST_USER)
		CommonMethods.searchAndOpenItemWithKeyword(PURCHASEABLE_NON_SHIPPABLE_ITEM)
		Item.addItemToCart(1)
		Order.addDefaultToken()
		saveTokenDetails()
		Order.submitPurchase()
	}

	@And('^my order does not have a payment method applied$')
	static void verifyOrderHasNoPaymentMethod() {
		Order.paymentmethodinfo()
		assertLinkDoesNotExist(client, "paymentmethod")
	}

	@Given('^a free product was purchased without payment$')
	static void purchaseFreeProduct() {
		client.authRegisteredUserByName(DEFAULT_SCOPE, DEFAULT_SCOPE_TEST_USER)
		CommonMethods.searchAndOpenItemWithKeyword(ZERO_DOLLAR_ITEM)
		Item.addItemToCart(1)
		Order.submitPurchase()
	}

	@Given('^I authenticate as a shopper with payment tokens X Y and Z and X as the default$')
	static void authenticateWithSelectedPaymentToken() {
		client.authRegisteredUserByName(TOKEN_SCOPE, TEST_USER_WITH_TOKENS)
		defaultPaymentMethodDisplayName = Profile.getDefaultPaymentMethodDisplayName()
	}

	@Given('^I authenticate as a shopper with payment method X as the chosen payment method for their order$')
	static void authenticateWithChosenPaymentMethod() {
		client.authRegisteredUserByName(TOKEN_SCOPE, TEST_USER_WITH_TOKENS)
	}

	@Given('^I authenticate as a shopper with saved payment methods X Y and Z and payment method X is chosen on their order$')
	static void authenticateWithSavedPaymentMethod() {
		client.authRegisteredUserByName(TOKEN_SCOPE, TEST_USER_WITH_TOKENS)
		CommonMethods.searchAndAddProductToCart(TOKEN_STORE_PURCHASEABLE_ITEM)
		preChosenPaymentMethod = Order.getChosenPaymentMethodDisplayName()
	}

	@When('^I retrieve the default payment method on my profile$')
	static void getProfilePaymentMethod() {
		Profile.defaultPaymentMethod()
	}

	@When('^I create a payment method for my profile$')
	static void createProfilePaymentMethod() {
		Profile.addToken(TEST_TOKEN_DISPLAY_NAME, TEST_TOKEN)
	}

	@When('^a payment method is deleted from the profile$')
	static void deleteProfilePaymentMethod() {
		Profile.deleteToken(TEST_TOKEN_DISPLAY_VALUE_X)
	}

	@When('^the registered shopper attempts to edit payment method$')
	static void editProfilePaymentMethod() {
		Profile.findToken(TEST_TOKEN_DISPLAY_NAME)
		CommonMethods.edit(CommonMethods.getSelfUri(), ["display-name": ""])
	}

	@When('^I view the payment methods available to be selected for my order$')
	static void getOrderPaymentMethods() {
		Order.paymentMethodSelector()
	}

	@When('^I create a payment method for my order$')
	static void addOrderPaymentMethod() {
		Order.addTokenToOrder(TEST_TOKEN_DISPLAY_NAME, TEST_TOKEN)
	}

	@And('^I retrieve my order$')
	static void getOrder() {
		Order.getOrder()
	}

	@When('^I view the purchase$')
	static void getPurchases() {
		Purchase.resume()
	}

	@When('^I get the list of payment methods from my profile$')
	static void getProfilePaymentMethods() {
		Profile.paymentmethods()
	}

	@When('^I get the chosen payment method X$')
	static void getOrderSelectedPaymentMethod() {
		Order.chosenPaymentMethod()
	}

	@When('^I get the payment method details for the chosen payment method X$')
	static void getPaymentMethodDetails() {
		Order.chosenPaymentMethodDescription()
	}

	@When('^I get the payment method details for payment method choice Y or Z$')
	static void getPaymentChoiceDescription() {
		Order.paymentMethodChoiceDescription()
	}

	@When('^payment method X is now a choice as the payment method on the order$')
	static void verifyPaymentBecomesChoiceOnOrder() {
		Order.getChoicePaymentMethodDisplayName()
		assertThat(client["display-name"])
				.as("The paymentmethod display-name is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_TOKEN_DISPLAY_VALUE)
	}

	@When('^I select payment method Y on the order$')
	static void selectPaymentOnOrder() {
		Order.chooseAnyPaymentMethod()
		newlyChosenPaymentMethod = Order.getChosenPaymentMethodDisplayName()
	}

	@When('^I complete the purchase for the order$')
	static void submitPurchase() {
		Order.submitPurchase()
	}

	@Then('^I get the default token (.+)$')
	static void verifyTokenDisplayName(String tokenValue) {
		assertThat(client["display-name"])
				.as("Default token display-name is not as expected")
				.isEqualTo(tokenValue)
	}

	@Then('^the payment method (?:is available from|has been added to) their profile$')
	static void verifyTokenAddedToProfile() {
		Profile.verifyToken(TEST_TOKEN_DISPLAY_NAME)
	}

	@Then('^it no longer shows up in his list of saved payment methods on his profile$')
	static void verifyTokenDeleted() {
		Profile.paymentmethods()

		boolean deletedTokenFound = client.body.links.findAll {
			link -> link.rel == "element"
		}.any {
			link -> client.GET(link.href)["display-name"] == TEST_TOKEN_DISPLAY_VALUE_X
		}

		assertThat(deletedTokenFound)
				.as("The deleted payment method should have not been found")
				.isFalse()
}

	@Then('^I see only the payment method available from my profile$')
	static void verifyOnlyProfileMethodsAvailable() {
		def displayName = Order.getChosenPaymentMethodDisplayName()

		assertThat(displayName)
				.as("The chosen payment method on the order is not as expected")
				.isEqualTo(TEST_TOKEN_DISPLAY_VALUE_X)

		Order.paymentMethodSelector()
		assertLinkDoesNotExist(client, "choice")
	}

	@Then('^the new payment method will be set for their order')
	static void verifyNewPaymentSetOnOrder() {
		assertThat(Order.getPaymentMethodDisplayName())
				.as("The chosen payment method on the order is not as expected")
				.isEqualTo(TEST_TOKEN_DISPLAY_NAME)
}

	@And('^the new payment method is not available from their profile')
	static void verifyPaymentMethodNotInProfile() {
	Profile.paymentmethods()
	assertLinkDoesNotExist(client, "element")
}

	@Then('^the default payment method is automatically applied to the order$')
	static void verifyDefaultPaymentMethodAppliedToOrder() {
		Order.paymentMethodSelector()
		assertLinkExists(client, "chosen")

		def displayName = client.chosen().description()["display-name"]

		assertThat(displayName)
				.as("The chosen payment method on the order is not as expected")
				.isEqualTo(TEST_TOKEN_DISPLAY_NAME)

		Order.paymentMethodSelector()
		assertLinkDoesNotExist(client, "choice")
	}

	@Then('^the paymentmeans is a paymenttoken type$')
	static void paymentTokenType() {
		assertThat(client.body.self.type)
				.as("The paymentmeans is not as expected")
				.isEqualTo("purchases.purchase-paymentmean")
	}

	@And('^the token display-name matches the token used to create the purchase$')
	static void verifyPurchasePaymentMethod() {
		assertThat(Purchase.getPaymentDisplayName())
				.as("The token display-name is not as expected")
				.isEqualTo(token)
	}

	@Then('^the paymentmeans is empty$')
	static void verifyPaymentmeansEmpty() {
		Purchase.paymentmeans()
		assertLinkDoesNotExist(client, "element")
	}

	@Then('^the paymentmeans is a credit card type$')
	static void verifyPaymentmeansIsCreditCardType() {
		client.paymentmeans()
				.element()
				.stopIfFailure()
		assertThat(client.body.self.type)
				.as("The paymentmeans is not as expected")
				.isEqualTo("purchases.purchase-paymentmean")
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

	@Then('^the list contains payment tokens X Y and Z and X is displayed as the default$')
	static void verifyListContainsTokens() {
		FindPaymentToken(DEFAULT_PAYMENT_TOKEN_DISPLAY_VALUE)
		FindPaymentToken(SECOND_PAYMENT_TOKEN_DISPLAY_VALUE)
		FindPaymentToken(THIRD_PAYMENT_TOKEN_DISPLAY_VALUE)
		client.default()
		assertThat(client["display-name"])
				.as("The default display name is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_TOKEN_DISPLAY_VALUE)
	}

	@Then('^there is no way to select the payment method for my order$')
	static void verifyPaymentCannotBeSelected() {
		assertLinkDoesNotExist(client, "selectaction")
	}

	@Then('^the payment method details display the correct values$')
	static void verifyPaymentMethodDetails() {
		assertThat(client["display-name"])
				.as("The chosen payment method was not the default payment method")
				.isEqualTo(DEFAULT_PAYMENT_TOKEN_DISPLAY_VALUE)
	}

	@Then('^the payment method details display the correct values for that choice$')
	static void verifyPaymentMethodDetailsForChoice() {
		def paymentMethodDisplayValue = Order.getDisplayName()

		assertThat(paymentMethodDisplayValue == SECOND_PAYMENT_TOKEN_DISPLAY_VALUE || paymentMethodDisplayValue == THIRD_PAYMENT_TOKEN_DISPLAY_VALUE)
				.as("The chosen payment method was not the default payment method")
				.isTrue()
	}

	@Then('^payment method Y is now chosen as the payment method on the order$')
	static void verifyPaymentIsSelectedOnOrder() {
		//Clean up test by completing the purchase
		Order.submitPurchase()
		assertThat(newlyChosenPaymentMethod != preChosenPaymentMethod)
				.as("The newly selected payment method should now be chosen for the order")
				.isTrue()
	}

	@Then('^payment method X is chosen as the payment method for my purchase$')
	static void verifyChosenPaymentMethod() {
		assertThat(Order.getChosenPaymentMethodDisplayName())
				.as("The payment method selected was not the default payment method")
				.isEqualTo(defaultPaymentMethodDisplayName)
	}

	@Then('^the chosen payment method is displayed correctly as the payment mean for the purchase$')
	static void verifyPurchasePaymentMeans() {
		Purchase.selectPaymentMeans(preChosenPaymentMethod)
		assertThat(client["display-name"])
				.as("The payment method used for purchase should be the same as the chosen payment method")
				.isEqualTo(preChosenPaymentMethod)
	}

	private static void registerNewShopperAndAuthenticate() {
		def USERNAME = UUID.randomUUID().toString() + "@elasticpath.com"

		client.authAsAPublicUser(DEFAULT_SCOPE)
				.stopIfFailure()

		registerShopper(TOKEN_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, USERNAME)

		client.authRegisteredUserByName(TOKEN_SCOPE, USERNAME)
	}

	private static void saveTokenDetails() {
		token = Order.getPaymentMethodDisplayName()
	}

	private static void FindPaymentToken(def tokenDisplayName) {
		client.findElement {
			paymentmethod ->
				paymentmethod["display-name"] == tokenDisplayName
		}.list()
	}

	static void registerShopper(registrationScope, familyName, givenName, password, username) {
		client.authAsAPublicUser(registrationScope)

		client.GET("registrations/$registrationScope/newaccount/form")
				.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}
}
