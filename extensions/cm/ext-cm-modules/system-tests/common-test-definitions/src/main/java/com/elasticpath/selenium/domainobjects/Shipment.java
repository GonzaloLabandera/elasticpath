package com.elasticpath.selenium.domainobjects;

import java.util.List;

/**
 * Shipment class.
 */
public class Shipment {

	private String shipmentNumber;
	private String shippingAddress;
	private String shippingMethod;
	private List<String> productSkus;
	private String itemSubTotal;
	private String shippingCost;
	private String lessShipmentDiscount;
	private String totalBeforeTax;
	private String itemTaxes;
	private String shippingTaxes;
	private String shipmentTotal;
	private String status;

	public String getShipmentNumber() { return shipmentNumber; }

	public void setShipmentNumber(final String shipmentNumber) { this.shipmentNumber = shipmentNumber; }

	public String getShippingAddress() { return shippingAddress; }

	public void setShippingAddress(final String shippingAddress) { this.shippingAddress = shippingAddress; }

	public String getShippingMethod() { return shippingMethod; }

	public void setShippingMethod(final String shippingMethod) { this.shippingMethod = shippingMethod; }

	public List<String> getProductSkus() { return productSkus; }

	public void setProductSkus(final List<String> productSkus) { this.productSkus = productSkus; }

	public String getItemSubTotal() { return itemSubTotal; }

	public void setItemSubTotal(final String itemSubTotal) { this.itemSubTotal = itemSubTotal; }

	public String getShippingCost() { return shippingCost; }

	public void setShippingCost(final String shippingCost) { this.shippingCost = shippingCost; }

	public String getLessShipmentDiscount() { return lessShipmentDiscount; }

	public void setShipmentDiscount(final String lessShipmentDiscount) { this.lessShipmentDiscount = lessShipmentDiscount; }

	public String getTotalBeforeTax() { return totalBeforeTax; }

	public void setTotalBeforeTax(final String totalBeforeTax) { this.totalBeforeTax = totalBeforeTax; }

	public String getItemTaxes() { return itemTaxes; }

	public void setItemTaxes(final String itemTaxes) { this.itemTaxes = itemTaxes; }

	public String getShippingTaxes() { return shippingTaxes; }

	public void setShippingTaxes(final String shippingTaxes) { this.shippingTaxes = shippingTaxes; }

	public String getShipmentTotal() { return shipmentTotal; }

	public void setShipmentTotal(final String shipmentTotal) { this.shipmentTotal = shipmentTotal; }

	public String getStatus() { return status; }

	public void setStatus(final String status) { this.status = status; }
}
