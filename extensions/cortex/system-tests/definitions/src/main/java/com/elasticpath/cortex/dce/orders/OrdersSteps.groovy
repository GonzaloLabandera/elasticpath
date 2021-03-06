package com.elasticpath.cortex.dce.orders

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.TEST_EMAIL_VALUE
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.DBConnector
import com.elasticpath.cortex.dce.SharedConstants
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

class OrdersSteps {

	private static final String DEBUG_MESSAGE_KEY = "debug-message"
	private static final String ID_KEY = "id"

	private static final DBConnector dbConnector = new DBConnector();

	@When('^I submit the order$')
	static void submitOrder() {
		Order.submitPurchaseWithoutFollow()
	}

	@When('^I create an email for my order$')
	static void enterOrderEmail() {
		Profile.addEmail(TEST_EMAIL_VALUE)
	}

	@When('^I select only the billing address$')
	static void selectRandomBillingAddress() {
		Profile.createUniqueAddress()
		Profile.createUniqueAddress()
		Order.selectAnyBillingAddress()
		removeShippingAddressOnOrderIfItExists()
	}

	@When('^I also select the shipping address')
	static void selectShippingAddressAndLevel() {
		Order.selectAnyShippingAddress()
		Order.selectShippingServiceLevel("CanadaPostExpress")
	}

	@When('^I select only the shipping address$')
	static void selectRandomShippingAddress() {
		configureOnlyOneSelectedShippingAddressForUser()
	}

	@Then('^my order fails with status (.+)$')
	static void verifyResponseStatus(String responseStatus) {
		assertThat(client.response.status.toString())
				.as("The http status is not as expected")
				.isEqualTo(responseStatus)
	}

	@Then('^I am not able to submit my order$')
	static void verifyCannotSubmitOrder() {
		Order.purchaseform()
		assertLinkDoesNotExist(client, "submitorderaction")
	}

	@When('^I add an address with country (.+) and region (.+)$')
	static void createNewAddressWithCountryRegion(String country, String subcountry) {
		addNewAddress(country, subcountry)
	}

	@When('^I add a (.+) address$')
	static void createNewAddressWithCountry(String country) {
		addNewAddress(country, "")
	}

	@And('^I retrieve the order taxes$')
	static void getOrderTax() {
		Order.tax()
	}

	@Then('^my order succeeds$')
	static void verifyOrderSubmitSuccess() {
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)
	}

	@Then('^the email is created and selected for my order$')
	static void verifyOrderHasEmail() {
		Order.email()
		assertThat(client["email"])
				.as("Email on the order is not as expected")
				.isEqualTo(TEST_EMAIL_VALUE)
	}

	@And('^the tax total on the order is (.+)$')
	static void verifyOrderTaxTotal(taxAmount) {
		assertThat(client["total"]["display"])
				.as("The tax total is not as expected")
				.isEqualTo(taxAmount)
	}

	@Then('^the (.+) cost is (.+)$')
	static void verifyTaxAmount(taxType, amount) {
		assertThat(client["cost"].find {
			it ->
				it.title == taxType
		}.display)
				.as("The cost is not as expected")
				.isEqualTo(amount)
	}

	@Then('^resolve the shipping-option-info needinfo$')
	static void selectShippingAddress() {
		Order.selectAnyShippingAddress()
	}

	@Then('^I select the (new|valid) shipping address$')
	static void selectShippingAddress(def ignore) {
		Order.selectAnyShippingAddress()
	}

	@Then('^post to a created submitorderaction uri$')
	static void postToSubmitorderactionURI() {
		client.POST(client.body.self.uri.toString(), [:])
	}

	@Then('^post to a created addtodefaultcartaction uri$')
	static void postToAddtodefaultcartactionURI() {
		client.POST(client.body.self.uri, [quantity: 1])
	}

	@When('^I retrieve the purchase form$')
	static void getPurchaseform() {
		Order.purchaseform()
	}

	@Then('^there is a needinfo link to (.+)$')
	static void verifyNeedinfoLinkExists(def resourceName) {
		assertThat(needInfoExistsWithName(resourceName))
				.as("Needinfo link for $resourceName not found")
				.isTrue()
	}

	@Then('^there is no needinfo link to (.+)$')
	static void verifyNeedinfoLinkNotExists(def resourceName) {
		assertThat(needInfoExistsWithName(resourceName))
				.as("Needinfo link for $resourceName was found")
				.isFalse()
	}

	@Then('^there is needinfo with id (.+) and debug message (.+)$')
	static void verifyNeedinfoIdAndMessageIdExists(def id, def message) {
		assertThat(needInfoExistsAndContainsData(id, ID_KEY))
				.as("Needinfo id $id was found")
				.isTrue()

		assertThat(needInfoExistsAndContainsData(message, DEBUG_MESSAGE_KEY))
				.as("Needinfo debug message $message was found")
				.isTrue()
	}

	@Then('^there is no needinfo with id (.+)$')
	static void verifyNeedinfoIdNotExists(def id) {
		assertThat(needInfoExistsAndContainsData(id, ID_KEY))
				.as("Needinfo id $id was found")
				.isFalse()
	}

	@Then('^there are no needinfo messages$')
	static void verifyNeedinfoNotExists() {
		assertThat(needInfoNotExist())
				.as("Needinfo should not exist")
				.isTrue()
	}

	@Then('^there is an advisor linked to (.+)$')
	static void verifyAdvisorLinkExists(def resourceName) {
		assertThat(advisorExistsWithLinkedToName(resourceName))
				.as("Advisor with link for $resourceName not found")
				.isTrue()
	}

	@Then('^there is no advisor linked to (.+)$')
	static void verifyAdvisorLinkNotExists(resourceName) {
		assertThat(advisorExistsWithLinkedToName(resourceName))
				.as("Needinfo link for $resourceName was found")
				.isFalse()
	}

	@And('^I delete the chosen billing address$')
	static void deleteSelectedBillingAddress() {
		deleteChosenBillingAddress()
	}

	@And('^there is no (.+) link found$')
	static void verifyLinkNotExists(def linkName) {
		assertLinkDoesNotExist(client, linkName)
	}

	@And('^I submit the order multiple times concurrently$')
	static void submitPurchaseMultipleTimes() {
		Order.purchaseform()
		def baseUrl = client.baseUrl.toString()
		def submitOrderActionUrl = client.body.self.uri.toString()

		CommonMethods.submitPostNTimesConcurrently(baseUrl.subSequence(0, baseUrl.length() - 1) + submitOrderActionUrl, 10);
	}

	@Before(value = "@enableOrderHold")
	static void enableHoldProcessingForDefaultScope() {
		dbConnector.executeUpdateQuery("INSERT INTO TSETTINGVALUE VALUES (" +
				"-1," +
				"(SELECT UIDPK FROM TSETTINGDEFINITION WHERE PATH = 'COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStore')," +
				"UPPER('$SharedConstants.DEFAULT_SCOPE')," +
				"CURRENT_TIMESTAMP," +
				"'true'" +
				")"
		)
	}

	@After(value = "@enableOrderHold")
	static void disableHoldProcessing() {
		dbConnector.executeUpdateQuery("DELETE FROM TSETTINGVALUE WHERE UIDPK=-1")
	}

	private static void configureOnlyOneSelectedShippingAddressForUser() {
		Profile.createUniqueAddress()
		Profile.createUniqueAddress()
		Order.selectAnyShippingAddress()
		Order.selectShippingServiceLevel("CanadaPostExpress")

		// Delete the billing address on order since it was automatically set
		// when an address was created above.
		deleteChosenBillingAddress()
	}

	private static void deleteChosenBillingAddress() {
		Order.billingAddress()
		def billingAddressUri = client.body.self.uri
		client.DELETE(billingAddressUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	private static void removeShippingAddressOnOrderIfItExists() {
		Order.deliveries()

		def link = client.body.links.find { link ->
			link.rel == "element"
		}
		if (link != null) {
			Order.destination()
			client.DELETE(client.body.self.uri)
		}
	}

	static needInfoExistsWithName(def name) {
		def found = false
		def startingPointUri = client.body.self.uri
		def listoflinks = client.body.links.findAll {
			link ->
				link.rel == "needinfo"
		}
		listoflinks.findResult {
			link ->
				client.GET(link.href).response

				//checks if current representation contains the info name being queried for
				if (client["name"] == name) {
					client.GET(startingPointUri)
					found = true
				}
		}
		client.GET(startingPointUri)
		return found
	}

	static needInfoExistsAndContainsData(def data, def key) {
		client.body.messages.stream().anyMatch { message -> Objects.equals(data, message.get(key)) }
	}

	static needInfoNotExist() {
		client.body.messages.isEmpty()
	}

	static advisorExistsWithLinkedToName(def name) {
		def found = false
		def startingPointUri = client.body.self.uri
		def listOfLinkedMessages = client.body.messages.findAll { message ->
			message.containsKey('linked-to')
		}
		listOfLinkedMessages.findResult { message ->
			client.GET(message.'linked-to'.href).response

			//checks if current representation contains the info name being queried for
			if (client["name"] == name) {
				client.GET(startingPointUri)
				found = true
			}
		}
		client.GET(startingPointUri)
		return found
	}

	private static void addNewAddress(String country, String subCountry) {
		Profile.createAddress(country, "", "Vancouver", "", "", "555555", subCountry,
				"123 Somestreet", "testFamilyName", "testGivenName")
		assert client.response.status == 201
	}
}
