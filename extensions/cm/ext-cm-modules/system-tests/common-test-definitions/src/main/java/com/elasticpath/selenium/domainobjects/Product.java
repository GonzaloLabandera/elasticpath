package com.elasticpath.selenium.domainobjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Product class.
 */
@SuppressWarnings("PMD.TooManyFields")
public class Product {

	private String catalog;
	private String category;
	private String productName;
	private String productType;
	private String taxCode;
	private String brand;
	private String storeVisible;
	private String availability;
	private String attrShortTextMulti;
	private String attrInteger;
	private String attrDecimal;
	private String attrShortTextMultiValue;
	private String attrIntegerValue;
	private String attrDecimalValue;
	private String shippableType;
	private String priceList;
	private String listPrice;
	private String bundlePricing;
	private String codesList;
	private String skuOption;


	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(final String skuCode) {
		this.skuCode = skuCode;
	}

	private String skuCode;

	public String getProductCodes() {
		return codesList;
	}

	public void setProductCodes(final String codesList) {
		this.codesList = codesList;
	}

	/**
	 * Gets Product Codes List.
	 *
	 * @return codesList.
	 */
	public List<String> getProductCodeList() {
		List<String> productSkuCodeList = new ArrayList<>();
		if (codesList == null) {
			return null;
		} else {
			String[] skuCodes = codesList.split(",");
			for (String product : skuCodes) {
				productSkuCodeList.add(product);
			}
			return productSkuCodeList;
		}
	}

	public String getStoreVisible() {
		return storeVisible;
	}

	public void setStoreVisible(final String storeVisible) {
		this.storeVisible = storeVisible;
	}

	public String getAttrShortTextMulti() {
		return attrShortTextMulti;
	}

	public void setAttrShortTextMulti(final String attrShortTextMulti) {
		this.attrShortTextMulti = attrShortTextMulti;
	}

	public String getAttrInteger() {
		return attrInteger;
	}

	public void setAttrInteger(final String attrInteger) {
		this.attrInteger = attrInteger;
	}

	public String getAttrDecimal() {
		return attrDecimal;
	}

	public void setAttrDecimal(final String attrDecimal) {
		this.attrDecimal = attrDecimal;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(final String catalog) {
		this.catalog = catalog;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(final String productType) {
		this.productType = productType;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(final String taxCode) {
		this.taxCode = taxCode;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(final String brand) {
		this.brand = brand;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(final String availability) {
		this.availability = availability;
	}

	public String getAttrShortTextMultiValue() {
		return attrShortTextMultiValue;
	}

	public void setAttrShortTextMultiValue(final String attrShortTextMultiValue) {
		this.attrShortTextMultiValue = attrShortTextMultiValue;
	}

	public String getAttrIntegerValue() {
		return attrIntegerValue;
	}

	public void setAttrIntegerValue(final String attrIntegerValue) {
		this.attrIntegerValue = attrIntegerValue;
	}

	public String getAttrDecimalValue() {
		return attrDecimalValue;
	}

	public void setAttrDecimalValue(final String attrDecimalValue) {
		this.attrDecimalValue = attrDecimalValue;
	}

	public String getShippableType() {
		return shippableType;
	}

	public void setShippableType(final String shippableType) {
		this.shippableType = shippableType;
	}

	public String getPriceList() {
		return priceList;
	}

	public void setPriceList(final String priceList) {
		this.priceList = priceList;
	}

	public String getListPrice() {
		return listPrice;
	}

	public void setListPrice(final String listPrice) {
		this.listPrice = listPrice;
	}

	public String getBundlePricing() {
		return bundlePricing;
	}

	public void setBundlePricing(final String bundlePricing) {
		this.bundlePricing = bundlePricing;
	}

	public String getSKUOption() {
		return skuOption;
	}

	public void setSKUOption(final String skuOption) {
		this.skuOption = skuOption;
	}
}
