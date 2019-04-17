package com.elasticpath.cortex.dce.wishlists

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.CommonAssertion.assertItemConfiguration
import static com.elasticpath.cortex.dce.CommonMethods.getLineItemUriForItemCode
import static com.elasticpath.cortex.dce.CommonMethods.getLineItemUriForItemName
import static com.elasticpath.cortex.dce.CommonMethods.verifyLineitemsContainElementWithCode
import static com.elasticpath.cortex.dce.CommonMethods.verifyLineitemsContainElementWithDisplayName
import static com.elasticpath.cortex.dce.CommonMethods.verifyLineitemsNotContainElementWithCode
import static com.elasticpath.cortex.dce.CommonMethods.verifyLineitemsNotContainElementWithDisplayName
import static com.elasticpath.cortex.dce.CommonMethods.verifyNumberOfElements
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.SharedConstants.ELEMENT_LINK
import static WishlistsConstants.FAMILY_NAME
import static WishlistsConstants.GIVEN_NAME
import static WishlistsConstants.PASSWORD
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.Profile
import com.elasticpath.cortexTestObjects.WishList

class WishlistsSteps {

	static registeredShopperUsername
	static configurationFields

	static navigateToWishListLineItems = { WishList.getWishListLineItems() }

	@Given('^I (?:add item with name|have) (.+) (?:in|to) my default wishlist$') 
	static void addItemToWishlistByName(String itemDisplayName) { 
		FindItemBy.productName(itemDisplayName)
		Item.addItemToWishListWithoutFollow()
	}

	@Given('^I add item with code (.+) to my default wishlist$') 
	static void addItemToWishlistByCode(String itemCode) { 
		CommonMethods.lookup(itemCode)
		Item.addItemToWishList()
	}

	@When('^I navigate to add to wishlist form for item with code (.+)$')
	static void navigateToAddToWishlistFormByCode(String itemCode) {
		CommonMethods.lookup(itemCode)
		Item.addtowishlistform()
	}

	@When('^I add selected multisku item to the wishlist')
	static void addItemToWishlist() {
		Item.addItemToWishList()
	}

	@Given('^I add (.+) to my default cart with quantity (.+)$')
	static void addItemToCartWithQty(String itemDisplayName, String itemQuantity) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)
		Item.addItemToCart(itemQuantity)
	}

	@Given('^(.+) is not purchaseable$')
	static void verifyItemNotPurchasable(String itemDisplayName) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)
		Item.addtocartform()
		assertLinkDoesNotExist(client, "addtodefaultcartaction")
	}

	@And('^item with name (.+) already exists in my cart with quantity (.+)$')
	static void verifyItemIsInCartByNameAndQty(String itemDisplayName, String quantity) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)
		Item.addItemToCartWithoutFollow(quantity)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)

		client.follow()
				.stopIfFailure()

		assertThat(client.body.quantity.toString())
				.as("Expected quantity does not match")
				.isEqualTo(quantity)
	}

	@And('^item with code (.+) already exists in my cart with quantity (.+)$')
	static void verifyItemIsInCartByCodeAndQty(String itemCode, String quantity) {
		CommonMethods.lookup(itemCode)
		Item.addItemToCartWithoutFollow(quantity)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)

		client.follow()
				.stopIfFailure()

		assertThat(client.body.quantity.toString())
				.as("Expected quantity does not match")
				.isEqualTo(quantity)
	}

	@Given('^(.+) is in my registered shopper\'s default wishlist$')
	static void verifyItemInRegisteredShopperWishlist(String itemDisplayName) {
		registeredShopperUsername = UUID.randomUUID().toString() + "@elasticpath.com"
		registerShopper(DEFAULT_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, registeredShopperUsername)
		client.authRegisteredUserByName(DEFAULT_SCOPE, registeredShopperUsername)

		searchForAndAddToWishlist(itemDisplayName)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)
	}

	@And('^(.+) is in my anonymous shopper\'s default wishlist$')
	static void verifyItemInAnonymousShopperWishlist(String itemDisplayName) {
		client.authAsAPublicUser(DEFAULT_SCOPE)

		searchForAndAddToWishlist(itemDisplayName)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)
	}

	@When('^I view my default wishlist$')
	static void viewWishlist() {
		WishList.getWishListLineItems()
	}

	@When('^I delete item with name (.+) from my default wishlist$')
	static void deleteItemFromWishlistByName(String itemDisplayName) {
		navigateToWishListLineItems()
		client.DELETE(getLineItemUriForItemName(itemDisplayName))
				.stopIfFailure()
	}

	@When('^I delete item with code (.+) from my default wishlist$')
	static void deleteItemFromWishlistByCode(String itemCode) {
		navigateToWishListLineItems()
		client.DELETE(getLineItemUriForItemCode(itemCode))
				.stopIfFailure()
	}

	@When('^I move item with name (.+) to my cart with quantity (.+)$')
	static void moveItemFromWishlistToCartByName(String itemDisplayName, String itemQuantity) {
		navigateToWishListLineItems()

		getLineItemUriForItemName(itemDisplayName)
		client.movetocartform()
				.movetocartaction(quantity: itemQuantity)
				.stopIfFailure()
	}

	@When('^I navigate to move to cart form for item with code (.+)$')
	static void navigateToMoveToCartFormByCode(String itemCode) {
		navigateToWishListLineItems()

		getLineItemUriForItemCode(itemCode)
		client.movetocartform()
	}

	@When('^I move item with code (.+) to my cart with quantity (.+)$')
	static void moveItemFromWishlistToCartByCode(String itemCode, String itemQuantity) {
		navigateToWishListLineItems()

		getLineItemUriForItemCode(itemCode)
		client.movetocartform()
				.movetocartaction(quantity: itemQuantity)
				.stopIfFailure()
	}

	@Then('^I can move configurable itemcode (.+) from wishlist to cart with quantity (.+) and preserved data values:$')
	static void verifyItemDataPreservedWhenMovedToCart(String itemCode, String itemQty, DataTable dataTable) {
		navigateToWishListLineItems()
		getLineItemUriForItemCode(itemCode)
		WishList.movetocartform()
		assertItemConfiguration(dataTable)
		WishList.movetocartaction(itemQty, client.body.configuration)
	}

	@Then('^I move configurable itemcode (.+) from wishlist to my cart with quantity (.+)$')
	static void moveConfiguredItemToCart(String itemCode, String itemQty, DataTable dataTable) {
		configurationFields = dataTable.asMap(String, String)
		navigateToWishListLineItems()
		getLineItemUriForItemCode(itemCode)
		WishList.moveItemToCartWithoutFollow(itemQty, configurationFields)
	}

	@When('^I view my profile$')
	static void viewProfile() {
		Profile.getProfile()
	}

	@When('^I move an item with name (.+) from my cart to my default wishlist$')
	static void moveItemToWishlist(String itemDisplayName) {
		Cart.lineitems()
		getLineItemUriForItemName(itemDisplayName)
		Item.moveItemToWishListWithoutFollow()
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)
	}

	@When('^I navigate to move to wishlist form for item with code (.+)$')
	static void navigateToMoveToWishlistFormByCode(String itemCode) {
		Cart.lineitems()
		getLineItemUriForItemCode(itemCode)
		Item.movetowishlistform()
	}

	@When('^I move an item with code (.+) from my cart to my default wishlist$')
	static void moveToWishlistByCode(String itemCode) {
		Cart.lineitems()
		getLineItemUriForItemCode(itemCode)
		Item.moveItemToWishListWithoutFollow()
	}

	@When('^I delete the list of wishlist items$')
	static void deleteFromWishlist() {
		navigateToWishListLineItems()

		def lineitemsUri = client.body.self.uri

		client.DELETE(lineitemsUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I retrieve the item details for (.+)$')
	static void getItemDetails(String itemDisplayName) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)

		// Validate that you found the item
		def itemUri = client.save();
		assertThat(client.definition()["display-name"])
				.as("Expected item display does not match")
				.isEqualTo(itemDisplayName)
		client.resume(itemUri)
	}

	@Then('^item with name (.+) is in my default wishlist$')
	static void verifyItemInWishlistByName(String itemDisplayName) {
		navigateToWishListLineItems()
		//	Saving response at lineitems uri
		def response = client.save()

		verifyLineitemsContainElementWithDisplayName(itemDisplayName)

		//	Resume response at linteitems uri
		client.resume(response)
	}

	@Then('^item with code (.+) is in my default wishlist$')
	static void verifyItemInWishlistByCode(String itemCode) {
		navigateToWishListLineItems()
		//	Saving response at lineitems uri
		def response = client.save()

		verifyLineitemsContainElementWithCode(itemCode)

		//	Resume response at linteitems uri
		client.resume(response)
	}

	@Then('^my default wishlist has (.+) (?:lineitems|lineitem)?$')
	static void verifyWishlistItemsQty(int numItems) {
		navigateToWishListLineItems()

		verifyNumberOfElements(numItems)
	}

	@Then('^(.+)\'s wishlist membership does not contain the default wishlist$')
	static void verifyItermNotInWishlist(String itemDisplayName) {
		FindItemBy.productName(itemDisplayName)
		Item.wishlistmemberships()
		verifyNumberOfElements(0)
	}

	@Then('^(.+)\'s wishlist membership contains the default wishlist$')
	static void verifyItemInWishlist(String itemDisplayName) {
		FindItemBy.productName(itemDisplayName)
		Item.wishlistmemberships()

		verifyNumberOfElements(1)
	}

	@And('^the list of wishlistmemberships has (.+) elements?$')
	static void verifyWishlistMembershipItemQty(int numElements) {
		client.wishlistmemberships()
				.stopIfFailure()

		verifyNumberOfElements(numElements)
	}

	@And('^the list of cartmemberships has (.+) elements?$')
	static void verifyCartMembershipItemQty(int numElements) {
		client.cartmemberships()
				.stopIfFailure()

		verifyNumberOfElements(numElements)
	}

	@Then('^item with name (.+) is not (?:found in|in) my default wishlist$')
	static void verifyItemNotInWishlistByName(String itemDisplayName) {
		WishList.getDefaultWishList()
		assertThat(CommonMethods.isLineitemsContainElementWithDisplayName(itemDisplayName))
				.as(itemDisplayName + " should not be present")
				.isFalse()
	}

	@Then('^item with code (.+) is not (?:found in|in) my default wishlist$')
	static void verifyItemNotInWishlistByCode(String itemCode) {
		navigateToWishListLineItems()
		verifyLineitemsNotContainElementWithCode(itemCode)
	}

	@And('^I cannot move item (.+) to my cart$')
	static void verifyItemCannotBeMovedToCart(String itemDisplayName) {
		getLineItemUriForItemName(itemDisplayName)
		client.movetocartform()
				.stopIfFailure()

		assertLinkDoesNotExist(client, "movetocartaction")
	}

	@When('^I transition to the registered shopper$')
	static void transitionToRegisteredShopper() {
		client.roleTransitionToRegisteredUserByName(DEFAULT_SCOPE, registeredShopperUsername)
				.stopIfFailure()
	}

	@Then('^item with name (.+) is in my cart with quantity (.+)$')
	static void verifyLineitemNameAndQty(String itemDisplayName, String itemQuantity) {
		Cart.lineitems()
		getLineItemUriForItemName(itemDisplayName)
		assertThat(client.body.'quantity'.toString())
				.as("The expected quantity does not match")
				.isEqualTo(itemQuantity)
	}

	@Then('^item with code (.+) is in my cart with quantity (.+)$')
	static void verifyLineitemCodeAndQty(String itemCode, String itemQuantity) {
		Cart.lineitems()
		getLineItemUriForItemCode(itemCode)
		assertThat(client.body.'quantity'.toString())
				.as("The expected quantity does not match")
				.isEqualTo(itemQuantity)
	}

	@And('^my list of wishlists contains (.+) wishlist$')
	static void verifyNumberOfWishlists(int numWishlists) {
		Profile.wishlists()

		def elements = client.body.links.findAll { link ->
			link.rel == ELEMENT_LINK
		}
		assertThat(elements)
				.as("Expected wishllist size does not match")
				.hasSize(numWishlists)
	}

	@Then('^I cannot delete my default wishlist$')
	static void verifyDefaultWishlistCannotBeDeleted() {
		WishList.getDefaultWishList()
		def wishlistUri = client.body.self.uri

		client.DELETE(wishlistUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(405)
	}

	@And('^item with name (.+) is not found in my cart$')
	static void verifyItemNotInCartByName(String itemDisplayName) {
		Cart.lineitems()
		verifyLineitemsNotContainElementWithDisplayName(itemDisplayName)
	}

	@And('^item with code (.+) is not found in my cart$')
	static void verifyItemNotInCartByCode(String itemCode) {
		Cart.lineitems()
		verifyLineitemsNotContainElementWithCode(itemCode)
	}

	@Then('^I cannot add the item to my wishlist$')
	static void clickWishlistFormLink() {
		client.addtowishlistform()
				.stopIfFailure()

		assertLinkDoesNotExist(client, "addtodefaultwishlistaction")
	}

	@Then('^I cannot move the item to my wishlist$')
	static void verifyItemCannotBeMovedToWishlist() {
		Item.movetowishlistform()
		assertLinkDoesNotExist(client, "movetowishlistaction")
	}

	@When('^I navigate to root\'s default wishlist$')
	static void clickDefaultWishlistLink() {
		client.GET("/")
				.defaultwishlist()
				.stopIfFailure()
		def CART_URI = client.body.self.uri
	}

	@Then('^(.+)\'s cart membership contains the default cart$')
	static void verifyItemCartMembershipExists(def itemDisplayName) {
		assertProductCartMembershipCount(itemDisplayName, 1)
	}

	@Then('^(.+)\'s cart membership does not contain the default cart$')
	static void verifyItemCartMembershipDoesNotExist(def itemDisplayName) {
		assertProductCartMembershipCount(itemDisplayName, 0)
	}

	@When('^I navigate to the movetocartform for wishlist item with code (.+)$')
	static void navigateToItemMovetocartform(String itemCode) {
		navigateToWishListLineItems()

		getLineItemUriForItemCode(itemCode)
		client.movetocartform()
				.stopIfFailure()
	}

	private static void assertProductCartMembershipCount(def itemDisplayName, def count) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)

		// Validate that you found the item
		def itemUri = client.save();
		assertThat(client.definition()["display-name"])
				.as("Expected item display does not match")
				.isEqualTo(itemDisplayName)
		client.resume(itemUri)

		client.cartmemberships()
				.stopIfFailure()

		verifyNumberOfElements(count)
	}

	private static void searchForAndAddToWishlist(String itemDisplayName) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)
		Item.addItemToWishListWithoutFollow()
	}

	static void registerShopper(registrationScope, familyName, givenName, password, username) {
		client.authAsAPublicUser(registrationScope)

		client.GET("registrations/$registrationScope/newaccount/form")
				.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}
}
