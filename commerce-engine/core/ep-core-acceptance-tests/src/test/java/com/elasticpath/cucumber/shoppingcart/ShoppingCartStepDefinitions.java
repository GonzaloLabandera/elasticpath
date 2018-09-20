/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.shoppingcart;

import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.cucumber.customer.CustomerStepDefinitionsHelper;

/**
 * Shopping cart test step definitions class.
 * 
 */
@ContextConfiguration("/cucumber.xml")
public class ShoppingCartStepDefinitions {
	
	@Autowired
	private ShoppingCartStepDefinitionsHelper shoppingCartStepDefinitionsHelper;
	
	@Autowired
	private CustomerStepDefinitionsHelper customerStepDefinitionsHelper;
	
	/**
	 * Adds addresses to customer of the current test context, and selects one address for the shopping cart's shipping address.
	 *
	 * @param addressDtos addresses
	 */
	@Given("^the customer's shipping address is in$")
	public void addShippingAddresses(final List<AddressDTO> addressDtos) {
		customerStepDefinitionsHelper.addAddresses(addressDtos);
		shoppingCartStepDefinitionsHelper.setShippingAddress(customerStepDefinitionsHelper.getAddress(
														addressDtos.get(0).getSubCountry(), 
														addressDtos.get(0).getCountry()));
	}

	/**
	 * Clears any shipping address that has been set on the shopping cart.
	 */
	@Given("^the customer's shipping address is not set$")
	public void clearShippingAddress() {
		shoppingCartStepDefinitionsHelper.setShippingAddress(null);
	}

	/**
	 * Adds the given shopping items to the shopping cart.
	 *
	 * @param shoppingItems shopping items
	 */
	@Given("^the customer adds these items to the shopping cart$")
	public void addItems(final List<ShoppingItemDto> shoppingItems) {
		shoppingCartStepDefinitionsHelper.addItems(shoppingItems);
	}
	
	/**
	 * Adds items to the shopping cart of the current test context, and checkout out an order from the shopping cart. 
	 *
	 * @param itemDtos items
	 */
	@Given("^the customer purchases these items$")
	public void purchasesItems(final List<ShoppingItemDto> itemDtos) {
		shoppingCartStepDefinitionsHelper.purchaseItems(itemDtos);
	}
	
	/**
	 * Selects delivery option for the shopping cart of the current test context.
	 *
	 * @param deliveryOption the delivery option
	 */
	@And("^the customer shipping method is \\[(.+)\\]$")
	public void selectDeliveryOption(final String deliveryOption) {
		shoppingCartStepDefinitionsHelper.setDeliveryOption(deliveryOption);
	}
}
