package com.elasticpath.selenium.domainobjects;

/**
 * ShipmentTableRecord class
 */
public class ShipmentTableRecord {

	private String shipmentType;
	private String shipmentNumber;
	private String bundleName;
	private String inventory;
	private String skuCode;
	private String productName;
	private String skuOptions;
	private String listPrice;
	private String salePrice;
	private String quantity;
	private String discount;
	private String totalPrice;
	private String paymentSchedule;

	public String getShipmentType() { return shipmentType; }

	public void setShipmentType(final String shipmentType) { this.shipmentType = shipmentType; }

	public String getShipmentNumber() { return shipmentNumber; }

	public void setShipmentNumber(final String shipmentNumber) { this.shipmentNumber = shipmentNumber; }

	public String getBundleName() { return bundleName; }

	public void setBundleName(final String bundleName) { this.bundleName = bundleName; }

	public String getInventory() { return inventory; }

	public void setInventory(final String inventory) { this.inventory = inventory; }

	public String getSkuCode() { return skuCode; }

	public void setSkuCode(final String skuCode) { this.skuCode = skuCode; }

	public String getProductName() { return productName; }

	public void setProductName(final String productName) { this.productName = productName; }

	public String getSkuOptions() { return skuOptions; }

	public void setSkuOptions(final String skuOptions) { this.skuOptions = skuOptions; }

	public String getListPrice() { return listPrice; }

	public void setListPrice(final String listPrice) { this.listPrice = listPrice; }

	public String getSalePrice() { return salePrice; }

	public void setSalePrice(final String salePrice) { this.salePrice = salePrice; }

	public String getQuantity() { return quantity; }

	public void setQuantity(final String quantity) { this.quantity = quantity; }

	public String getDiscount() { return discount; }

	public void setDiscount(final String discount) { this.discount = discount; }

	public String getTotalPrice() { return totalPrice; }

	public void setTotalPrice(final String totalPrice) { this.totalPrice = totalPrice; }

	public String getPaymentSchedule() { return paymentSchedule; }

	public void setPaymentSchedule(final String paymentSchedule) { this.paymentSchedule = paymentSchedule; }
}
