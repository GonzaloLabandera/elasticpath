/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.cucumber.order;

import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.cucumber.customer.CustomerStepDefinitionsHelper;

/**
 * Order test step definitions class.
 * 
 */
@ContextConfiguration("/cucumber.xml")
public class OrderStepDefinitions {
	
	@Autowired
	private OrderStepDefinitionsHelper orderStepDefinitionsHelper;
	
	@Autowired
	private CustomerStepDefinitionsHelper customerStepDefinitionsHelper;
	
	/**
	 * Releases order physical shipments, and complete order of the current test context. 
	 */
	@When("^the order is completed$")
	public void completeOrder() {
		
		orderStepDefinitionsHelper.completeOrder();
	}
	
	/**
	 * Cancels  the order shipments.
	 */
	@When("^the order is canceled$")
	public void cancelOrder() {
		
		orderStepDefinitionsHelper.cancelOrder();
	}
	
	/**
	 * Cancels  the order physical shipments.
	 */
	@When("^the order physical shipment is canceled$")
	public void cancelOrderShipment() {
		
		orderStepDefinitionsHelper.cancelOrderShipments();
	}
	
	/**
	 * Adds items to a physical order shipment.
	 *
	 * @param itemDtos items
	 */
	@When("^the order physical shipment is modified by adding these items$")
	public void addItemsToOrderPhysicalShipment(final List<ShoppingItemDto> itemDtos) {
		orderStepDefinitionsHelper.addItemsToPhysicalShipment(itemDtos);
	}
	
	/**
	 * Removes items from a physical order shipment.
	 *
	 * @param itemDtos items
	 */
	@When("^the order physical shipment is modified by removing these items$")
	public void removeItemsFromOrderPhysicalShipment(final List<ShoppingItemDto> itemDtos) {
		orderStepDefinitionsHelper.removeItemsFromPhysicalShipment(itemDtos);
	}
	
	/**
	 * Changes item quantities of a physical order shipment.
	 *
	 * @param itemDtos items
	 */
	@When("^the order physical shipment is modified by changing these items quantities$")
	public void changeItemQuantitiesOfOrderPhysicalShipment(final List<ShoppingItemDto> itemDtos) {
		orderStepDefinitionsHelper.changeItemQuantitiesOfPhysicalShipment(itemDtos);
	}
	
	/**
	 * Changes item prices of a physical order shipment.
	 *
	 * @param dataTable data of items
	 */
	@When("^the order physical shipment is modified by changing these items prices$")
	public void changeItemPricesOfOrderPhysicalShipment(final DataTable dataTable) {
		orderStepDefinitionsHelper.changeItemPricesOfPhysicalShipment(dataTable.asMaps(String.class, String.class));
	}
	
	/**
	 * Changes item discounts of a physical order shipment.
	 *
	 * @param dataTable data of items
	 */
	@When("^the order physical shipment is modified by changing these items discounts$")
	public void changeItemDiscountsOfOrderPhysicalShipment(final DataTable dataTable) {
		orderStepDefinitionsHelper.changeItemDiscountsOfPhysicalShipment(dataTable.asMaps(String.class, String.class));
	}
	
	/**
	 * Changes shipping address of a physical order shipment.
	 *
	 * @param addressDtos shipping address
	 */
	@When("^the order physical shipment is modified by changing its shipping address to$")
	public void changeShippingAddressOfOrderPhysicalShipment(final List<AddressDTO> addressDtos) {
		customerStepDefinitionsHelper.addAddresses(addressDtos);
		
		orderStepDefinitionsHelper.changeShippingAddressOfPhysicalShipment(
								customerStepDefinitionsHelper.getAddress(addressDtos.get(0).getSubCountry(), 
																addressDtos.get(0).getCountry()));
	}
	
	/**
	 * Changes delivery option of a physical order shipment.
	 *
	 * @param deliveryOption the delivery option
	 */
	@When("^the order physical shipment is modified by changing its shipping method to \\[(.+)\\]$")
	public void changeShippingMethodOfOrderPhysicalShipment(final String deliveryOption) {
		
		orderStepDefinitionsHelper.changeShippingMethodOfOrderPhysicalShipment(deliveryOption);
	}
	
	/**
	 * Moves items from an existing physical shipment to a new physical shipment.
	 *
	 * @param itemDtos item dtos
	 */
	@When("^the order physical shipment is modified by moving these items into a new shipment$")
	public void moveItemsToNewShipmentFromOrderPhysicalShipment(final List<ShoppingItemDto> itemDtos) {
		orderStepDefinitionsHelper.moveItemsToNewShipmentFromOrderPhysicalShipment(itemDtos);
	}
}
