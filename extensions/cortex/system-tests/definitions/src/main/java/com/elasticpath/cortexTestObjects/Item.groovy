package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Item.
 */
class Item extends CommonMethods {

	static void getItem() {
		client.resume(CortexResponse.elementResponse)
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

	static void addtowishlistform() {
		getItem()
		client.addtowishlistform()
				.stopIfFailure()
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