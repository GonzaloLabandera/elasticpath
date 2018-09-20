/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.orderreturn;

import java.util.List;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.common.dto.ShoppingItemDto;

/**
 * OrderReturn test scenario step definitions class.
 * 
 */
@ContextConfiguration("/cucumber.xml")
public class OrderReturnStepDefinitions {
		
	@Autowired
	private OrderReturnStepDefinitionsHelper orderReturnStepDefinitionsHelper;
	
	/**
	 * Creates an order return and save it to the current test context. 
	 *
	 * @param itemDtos return items
	 */
	@When("^a return is created with following items$")
	public void createReturn(final List<ShoppingItemDto> itemDtos) {
		orderReturnStepDefinitionsHelper.createShipmentReturn(itemDtos);
	}
	
	/**
	 * Creates order return with a given collection of items.
	 *
	 * @param itemDtos item dtos
	 */
	@When("^an exchange is created with returning following items$")
	public void createExchangeReturning(final List<ShoppingItemDto> itemDtos) {
		orderReturnStepDefinitionsHelper.createExchangeReturningItems(itemDtos);
	}
	
	/**
	 * Creates order return with exchange order. 
	 *
	 * @param itemDtos item exchange items
	 */
	@When("^exchanging with following items$")
	public void createExchange(final List<ShoppingItemDto> itemDtos) {
		orderReturnStepDefinitionsHelper.createExchange(itemDtos);
	}
	
	/**
	 * Cancels the order return of the current test context.
	 */
	@And("^the return is canceled$")
	public void cancelReturn() {
		
		orderReturnStepDefinitionsHelper.cancelReturn();
	}
	
	/**
	 * Modifies order return items.
	 * 
	 * @param itemDtos order return items
	 */
	@And("^the return is modified with following items$")
	public void editReturnItems(final List<ShoppingItemDto> itemDtos) {
		orderReturnStepDefinitionsHelper.editReturnItems(itemDtos);
	}
}
