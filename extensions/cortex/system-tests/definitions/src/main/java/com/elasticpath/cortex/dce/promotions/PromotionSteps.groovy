package com.elasticpath.cortex.dce.promotions

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.COST_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_CART_URL
import static com.elasticpath.cortex.dce.SharedConstants.DISPLAY_NAME_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.LIST_PRICE_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.NAME_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.PURCHASE_PRICE_FIELD
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

class PromotionSteps {

	static final ZOOM_URI = "?zoom="
	static final DISPLAY_DESCRIPTION_FIELD = "display-description"
	static final APPLIED_PROMOTIONS_LINK = "appliedpromotions"

	@When('^the (.+) promotion link is followed from the coupon$')
	static void verifyLinkToPromoExists(String promoName) {
		client.follow()
				.appliedpromotions()
				.findElement { promotion ->
			promotion[NAME_FIELD] == promoName
		}
		.stopIfFailure()
	}

	@Then('^the applied promotion shows (.+?)$')
	static void verifyPromoNameDisplayed(String promoName) {
		client.GET("/")
				.defaultcart()
				.stopIfFailure();

		client.appliedpromotions()
				.stopIfFailure();

		def numberOfPromotions = client.body.links.findAll { link ->
			link.rel == "element"
		}.size()

		assertThat(numberOfPromotions)
				.as("Number of promotions is not as expected")
				.isEqualTo(1)

		client.element()
				.stopIfFailure()

		assertThat(client["display-name"])
				.as("Display name is not as expected")
				.isEqualTo(promoName)
	}

	@Then('^the following promotion details are displayed$')
	static void verifyPromotionDetails(table) {
		Map expectedValues = table.asMap(String.class, String.class)

		assertThat(client[DISPLAY_DESCRIPTION_FIELD])
				.as("Display description is not as expected")
				.isEqualTo(expectedValues.get(DISPLAY_DESCRIPTION_FIELD))
		assertThat(client[DISPLAY_NAME_FIELD])
				.as("Display name is not as expected")
				.isEqualTo(expectedValues.get(DISPLAY_NAME_FIELD))
		assertThat(client[NAME_FIELD])
				.as("Name is not as expected")
				.isEqualTo(expectedValues.get(NAME_FIELD))
	}

	@When('^I (?:have|add) item (.+) (?:in|to) the (?:default cart|cart)$')
	static void searchAndAddItemToCart(String productName) {
		CommonMethods.searchAndAddProductToCart(productName)
	}

	@When('^a purchase is created with promotion (.+)$')
	static void verifyPurchaseCreatedWithPromotion(String promoItemName) {
		CommonMethods.searchAndAddProductToCart(promoItemName)
		Order.submitPurchase()
	}

	@Then('^the list of applied promotions contains promotion (.+)$')
	static void verifyListContainsPromotion(String promotionName) {
		Cart.verifyPromotionByName(promotionName)
	}

	@When('^I go to my default cart$')
	static void gotoCart() {
		Cart.getCart()
	}

	@When('^I navigate to apply coupon form$')
	static void navigateToCouponForm() {
		Order.navigateToCouponForm()
	}

	@When('^I apply a coupon code (.+) to my order$')
	static void applyCouponToOrder(String couponCode) {
		Order.applyCoupon(couponCode)
	}

	@Then('^the corresponding promotion of the coupon is inactive$')
	static void verifyPromotionInactive() {
		//do nothing - the promotion will only active if the trigger product is added to cart.
	}

	@Then('^there is a list of applied promotions on the (?:coupon details|purchase|cart|cart line item|shipping option)$')
	static void verifyPromotionLinkExists() {
		assertLinkExists(client, APPLIED_PROMOTIONS_LINK)
	}

	@Then('^the applied promotion (.+) matches the coupons$')
	static void verifyPromotionMatchesCoupon(String couponPromoName) {
		client.appliedpromotions()
				.stopIfFailure();

		def numberOfPromotions = client.body.links.findAll { link ->
			link.rel == "element"
		}.size()

		assertThat(numberOfPromotions)
				.as("Number of promotions is not as expected")
				.isEqualTo(1)

		client.element()
				.stopIfFailure()

		assertThat(client["name"])
				.as("Promotion name is not as expected")
				.isEqualTo(couponPromoName)
	}

	@Then('^the list of applied promotions is empty$')
	static void verifyPromotionListEmpty() {
		Cart.appliedpromotions()
		assertThat(client.body.links.findAll { it.rel == "element" })
				.as("Applied promotion list should be empty")
				.isEmpty()
	}

	@When('^I view an item (.+?) that (?:has a discount triggered by a |does not have )*promotion$')
	static void viewItem(String productName) {
		CommonMethods.searchAndOpenItemWithKeyword(productName)
	}

	@Given('^I fill in address for Canadian Shipping$')
	static void createCanadianShippingAddress() {
		createDefaultAddressForCanadianShipping()
		// NOTE: Destination is already selected because this was a new user and
		// the first address gets selected as default.
	}

	@Given('^I select shipping option (.+)$')
	static void selectShippingOption(String shippingOption) {
		// NOTE: Destination is already selected because this was a new user and
		// the first address gets selected as default.
		Order.selectShippingServiceLevel(shippingOption) //"Fixed Price With 100% Off Promotion Shipping Option")
	}

	@When('^I view a selected shipping option with promotions$')
	static void viewShippingOptionDescription() {
		// Assume that we start on the shipping option selector.
		Order.chosenShippingOptionDescription()
	}

	@When('^I view an unselected shipping option (.+)$')
	static void viewShippingOption(String shippingOptionWithWithOutPromo) {
		// Assume that we start on the shipping option selector.
		Order.shippingOptionSelector()
		client.findChoice {
			shippingoption ->
				def description = shippingoption.description()
				description["name"] == shippingOptionWithWithOutPromo
		}
		.description()
				.stopIfFailure()
	}

	@And('^a personalisation header triggers a cart promotion$')
	static void addPersonalizationHeader() {
		// This is set up in the test data.  Product triggerprodforpersonalisedcartdiscountpromo in cart will
		// trigger promo PersonalisedCartDiscountPromo (15% cart sub-total discount) for shoppers with age > 45
		CommonMethods.addPersonalisationHeader("CUSTOMER_AGE_YEARS", "50")
	}

	@When('^I view the unselected shipping option (.+) with personalised shipping promotions$')
	static void viewShippingOptionWithPersonalizationHeader(String shippingOption) {
		CommonMethods.addPersonalisationHeader("REFERRING_URL", "personalisedshippingpromos.elasticpath.net")
		// NOTE: Destination is already selected because this was a new user and
		// the first address gets selected as default.
		CommonMethods.findShippingOptionChoiceOrChosen(shippingOption)
		client.description()
				.stopIfFailure()
	}

	@And('^I zoom (.+) to the line item (.+) that contains the promotion (.+)$')
	static void zoomLineitemWithPromotion(String zoomParamValue, String expectedProductName, String expectedPromotionName) {
		client.GET(DEFAULT_CART_URL + ZOOM_URI + zoomParamValue)
		def bodyElements = client.body._lineitems._element[0]
		def found = false
		for (def element : bodyElements) {
			if (hasProductInLineItemElement(element, expectedProductName)
					&& hasPromotionInLineItemElement(element, expectedPromotionName)) {
				found = true
				break
			}
		}
		assertThat(found)
				.as("Product not found")
				.isTrue()
	}

	@And('^I zoom (.+) into the cart lineitem price and total$')
	static void zoomLineitem(String zoomParamValue) {
		client.GET(DEFAULT_CART_URL + ZOOM_URI + zoomParamValue)
	}

	@Then('^I see the lineitiem list-price display is (.+)$')
	static void verifyLineitemListPriceValue(String expectedListPriceDisplay) {
		def actualListPriceDisplay = client.body._lineitems._element[0]._price[LIST_PRICE_FIELD][0][0][0].display
		assertThat(actualListPriceDisplay)
				.as("The lineitem list-price display is not as expected")
				.isEqualTo(expectedListPriceDisplay)
	}

	@And('^I see the lineitem purchase-price display is (.+)$')
	static void verifyLineitemPurchasePriceValue(String expectedPurchasePriceDisplay) {
		def actualPurchasePriceDisplay = client.body._lineitems._element[0]._price[PURCHASE_PRICE_FIELD][0][0][0].display
		assertThat(actualPurchasePriceDisplay)
				.as("The lineitem purchase-price display is not as expected")
				.isEqualTo(expectedPurchasePriceDisplay)
	}

	@And('^I see the lineitem total cost display is (.+)$')
	static void verifyLineitemTotalValue(String expectedTotalCostDisplay) {
		def actualTotalCostDisplay = client.body._lineitems._element[0]._total[COST_FIELD][0][0][0].display
		assertThat(actualTotalCostDisplay)
				.as("The lineitem total cost display is not as expected")
				.isEqualTo(expectedTotalCostDisplay)
	}

	private static boolean hasProductInLineItemElement(def element, String expectedProductName) {
		def productName = element._item._definition[DISPLAY_NAME_FIELD][0][0]
		return expectedProductName.equals(productName);
	}

	private static boolean hasPromotionInLineItemElement(def element, String expectedPromotionName) {
		def promotionName = element._appliedpromotions._element[NAME_FIELD][0][0]
		return expectedPromotionName.equals(promotionName);
	}

	private static void createDefaultAddressForCanadianShipping() {
		def randomAddress = UUID.randomUUID().toString() + "random street"
		Profile.createAddress("CA", "", "Vancouver", "", "", "V7V7V7", "BC",
				randomAddress, "itest", "generated")
	}

}
