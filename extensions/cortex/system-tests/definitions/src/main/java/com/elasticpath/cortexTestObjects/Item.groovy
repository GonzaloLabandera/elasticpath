package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Item.
 */
class Item extends CommonMethods {
	public static addToSpecificCartFormResponse

	static void getItem() {
		client.resume(CortexResponse.elementResponse)
	}

	static void appliedpromotions() {
		client.appliedpromotions()
				.stopIfFailure()
	}

	static void availability() {
		client.availability()
				.stopIfFailure()
	}

	static void definition() {
		client.definition()
				.stopIfFailure()
	}

	static void components() {
		client.components()
				.stopIfFailure()
	}

	static void definition_components() {
		definition()
		components()
	}

	static void standaloneitem() {
		client.standaloneitem()
				.stopIfFailure()
	}

	static void options() {
		client.options()
				.stopIfFailure()
	}

	static void value() {
		client.value()
				.stopIfFailure()
	}


	static void code() {
		client.code()
				.stopIfFailure()
	}

	static void price() {
		getItem()
		client.price()
				.stopIfFailure()
	}

	static void addtocartform() {
		client.addtocartform()
				.stopIfFailure()
	}

	static void addtocartforms() {
		client.addtocartforms()
				.stopIfFailure()
	}

	static void wishlistmemberships() {
		client.wishlistmemberships()
				.stopIfFailure()
	}

	static void recommendations() {
		client.recommendations()
				.stopIfFailure()
	}

	static void navigateToRecommendationType(final def recommendationType) {
		recommendations()
		client."$recommendationType"()
				.stopIfFailure()
	}

	static String getSkuCode() {
		code()
		return client["code"]
	}

	static String getItemName() {
		definition()
		return client["display-name"]
	}

	static void cartmemberships() {
		client.cartmemberships()
				.stopIfFailure()
	}

	static void selectSkuOption(final def option, final def value) {
		CortexResponse.elementResponse = null
		boolean optionExists = false;
		boolean valueExists = false;
		CortexResponse.elementResponse = client.save()
		client.definition()
				.options()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (client["display-name"] == option) {
					return optionExists = true
				}
			}
		}

		assertThat(optionExists)
				.as("Unable to find option $option")
				.isTrue()

		client.selector()
		client.body.links.find {
			if (it.rel == "chosen" || it.rel == "choice") {
				client.GET(it.uri)
				def choiceResponse = client.save()
				client.description()
				println("value.... " + client["display-name"])
				if (client["display-name"] == value) {
					if (it.rel == "choice") {
						client.resume(choiceResponse)
						client.selectaction()
								.follow()
								.stopIfFailure()
						CortexResponse.elementResponse = client.save()
					}
					client.resume(CortexResponse.elementResponse)
					return valueExists = true
				}
			}
		}
		assertThat(valueExists)
				.as("Unable to find option value $value")
				.isTrue()
	}

	static void addItemToCart(def quantity) {
		addItemToCartWithoutFollow(quantity)
		client.follow()
				.stopIfFailure()
	}

	static void addItemToCartWithoutFollow(def quantity) {
		getItem()
		client.addtocartform()
				.addtodefaultcartaction(quantity: quantity)
				.stopIfFailure()
	}

	static void addItemToCart(def quantity, def configurationFields) {
		addItemToCartWithoutFollow(quantity, configurationFields)
		client.follow()
				.stopIfFailure()
	}

	static void addItemToCartWithoutFollow(def quantity, def configurationFields) {
		client.resume(CortexResponse.elementResponse)
				.addtocartform()
				.addtodefaultcartaction(
				["quantity"   : quantity,
				 configuration: configurationFields
				])
				.stopIfFailure()
	}

	static void selectAddToCartFormByName(String cartName) {
		addtocartforms()
		def cartExist = false
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				addToSpecificCartFormResponse = client.save()
				client.target()
						.descriptor()
				if (client["name"] == cartName ) {
					return cartExist = true
				}
			}
		}
		assertThat(cartExist)
				.as("Unable to find the cart with name - $cartName")
				.isTrue()
	}

	static void getAddToSpecificCartForm(def cartName) {
		getItem()
		selectAddToCartFormByName(cartName)
	}

	static void addItemToSpecificCartWithoutFollow(def cart, def quantity, def configurationFields = null) {
		getAddToSpecificCartForm(cart)
		client.resume(addToSpecificCartFormResponse)
				.addtocartaction(["quantity"   : quantity,
								  configuration: configurationFields
				])
				.stopIfFailure()
	}

	static void addItemToSpecificCart(def cart, def quantity, def configurationFields = null) {
		addItemToSpecificCartWithoutFollow(cart, quantity, configurationFields)
	}

	static void addtowishlistform() {
		getItem()
		client.addtowishlistform()
				.stopIfFailure()
	}

	static void verifyPromotionByName(List<String> promoNameList, Boolean condition) {
		appliedpromotions()
		def appliedPromoResponse = client.save()
		boolean promoExists = false
		for (String promoName : promoNameList) {
			client.resume(appliedPromoResponse)
			client.body.links.find {
				if (it.rel == "element") {
					client.GET(it.uri)
					if (promoName == client["name"]) {
						return promoExists = true
					}
				}
			}

			assertThat(promoExists)
					.as("Unable to find promotion: $promoName")
					.isEqualTo(condition)
		}
	}

	static void verifyPromotionByName(String promoName, Boolean condition) {
		List<String> promoNameList = new ArrayList<>()
		promoNameList.add(promoName)
		verifyPromotionByName(promoNameList, condition)
	}

	static void addItemToWishListWithoutFollow() {
		addtowishlistform()
		client.addtodefaultwishlistaction()
				.stopIfFailure()
	}

	static void addItemToWishList() {
		addtowishlistform()
		client.addtodefaultwishlistaction()
				.follow()
				.stopIfFailure()
	}

	static void movetowishlistform() {
		client.movetowishlistform()
				.stopIfFailure()
	}

	static void moveItemToWishListWithoutFollow() {
		movetowishlistform()
		client.movetowishlistaction()
				.stopIfFailure()
	}

	static void moveItemToWishList() {
		movetowishlistform()
		client.movetowishlistaction()
				.follow()
				.stopIfFailure()
	}

	static void offer() {
		client.offer()
				.stopIfFailure()
	}

}