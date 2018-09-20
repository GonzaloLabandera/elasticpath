/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.order;

import java.math.BigDecimal;
import java.util.Set;

import com.elasticpath.money.Money;

/**
 * <code>PhysicalOrderShipment</code> represents a customer's order shipment containing physical (shippable) goods.
 */
public interface PhysicalOrderShipment extends OrderShipment {

	/**
	 * Get the shipping cost in <code>BigDecimal</code>.
	 *
	 * @return the  shipping cost in <code>BigDecimal</code>.
	 */
	BigDecimal getShippingCost();

	/**
	 * Get the shipping cost in <code>Money</code>.
	 * 
	 * @return the shipping cost in <code>Money</code>.
	 */
	Money getShippingCostMoney();
		
	/**
	 * Get the shipping cost before tax in <code>Money</code>.
	 * 
	 * @return the shipping cost in <code>Money</code>.
	 */
	Money getBeforeTaxShippingCostMoney();

	/**
	 * Set the shipping cost in <code>BigDecimal</code>.
	 *
	 * @param shippingCost the shipping cost
	 */
	void setShippingCost(BigDecimal shippingCost);

	/**
	 * Sets the shipping subtotal amount.
	 * 
	 * @param shippingSubtotal BigDecimal
	 */
	void setShippingSubtotal(BigDecimal shippingSubtotal);
	
	/**
	 * Gets the shipping subtotal for this order shipment.
	 * 
	 * @return BigDecimal
	 */
	BigDecimal getShippingSubtotal();
	

	/**
	 * Get the before-tax shipping cost in <code>BigDecimal</code>.
	 *
	 * @return the before-tax shipping cost in <code>BigDecimal</code>.
	 */
	BigDecimal getBeforeTaxShippingCost();

	/**
	 * Set the before-tax shipping cost in <code>BigDecimal</code>.
	 *
	 * @param beforeTaxShippingCost the before-tax shipping cost
	 */
	void setBeforeTaxShippingCost(BigDecimal beforeTaxShippingCost);

	/**
	 * Get the shipping tax in <code>BigDecimal</code>.
	 *
	 * @return the shipping tax in <code>BigDecimal</code>.
	 */
	BigDecimal getShippingTax();

	/**
	 * Get the shipping tax in <code>Money</code>.
	 *
	 * @return the shipping tax in <code>Money</code>.
	 */
	Money getShippingTaxMoney();

	/**
	 * Get the shipment address corresponding to this shipment.
	 *
	 * @return the shipment address Uid
	 */
	OrderAddress getShipmentAddress();

	/**
	 * Set the shipping address corresponding to this shipment.
	 *
	 * @param shipmentAddress the Uid of the corresponding shipment address.
	 */
	void setShipmentAddress(OrderAddress shipmentAddress);

	/**
	 * @return Returns the carrier.
	 */
	String getCarrier();

	/**
	 * @param carrier The carrier to set.
	 */
	void setCarrier(String carrier);

	/**
	 * @return Returns the serviceLevel.
	 */
	String getServiceLevel();

	/**
	 * @param serviceLevel The serviceLevel to set.
	 */
	void setServiceLevel(String serviceLevel);

	/**
	 * @return Returns the trackingCode.
	 */
	String getTrackingCode();

	/**
	 * @param trackingCode The trackingCode to set.
	 */
	void setTrackingCode(String trackingCode);

	/**
	 * Gets the shipping service level guid.
	 *
	 * @return the shipping service level's guid
	 */
	String getShippingServiceLevelGuid();

	/**
	 * Sets the shipping service level.
	 *
	 * @param shippingServiceLevelGuid the shipping service level's guid
	 */
	void setShippingServiceLevelGuid(String shippingServiceLevelGuid);

	/**
	 * Get shipmentRemovedOrderSku.
	 *
	 * @return <CODE>this</CODE> shipmentRemovedOrderSku
	 */
	Set<OrderSku> getShipmentRemovedOrderSku();
	
}
