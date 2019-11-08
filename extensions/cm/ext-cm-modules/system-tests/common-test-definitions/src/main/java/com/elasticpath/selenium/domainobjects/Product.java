package com.elasticpath.selenium.domainobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Product class.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyFields"})
public class Product {

	private String catalog;
	private String category;
	private String productName;
	private String productCode;
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
	private String skuCodeList;
	private String attributesCodeList;
	private String skuOption;
	private String bundlePricing;
	private String bundleProductSKUList;
	private String bundleSelectionRule;
	private String bundleSelectionRuleValue;
	private Date enableDateTime;
	private Date disableDateTime;
	private Date releaseDateTime;
	private String minimumOrderQuantity;
	private String shippingWeight;
	private String shippingWidth;
	private String shippingLength;
	private String shippingHeight;
	private String shippingUnitsWeight;
	private String shippingUnitsLength;
	private String notSoldSeparately;
	private String enableDateTimeDays;
	private String disableDateTimeDays;
	private String releaseDateTimeDays;
	private List<ProductSkuItem> skuOptionList = new ArrayList<>();

	/**
	 * Non-arguments constructor.
	 */
	public Product() {
		//Non-arguments constructor.
	}

	/**
	 * Copy constructor.
	 *
	 * @param product
	 */
	public Product(final Product product) {
		this.catalog = product.catalog;
		this.category = product.category;
		this.productName = product.productName;
		this.productCode = product.productCode;
		this.productType = product.productType;
		this.taxCode = product.taxCode;
		this.brand = product.brand;
		this.storeVisible = product.storeVisible;
		this.availability = product.availability;
		this.attrShortTextMulti = product.attrShortTextMulti;
		this.attrInteger = product.attrInteger;
		this.attrDecimal = product.attrDecimal;
		this.attrShortTextMultiValue = product.attrShortTextMultiValue;
		this.attrIntegerValue = product.attrIntegerValue;
		this.attrDecimalValue = product.attrDecimalValue;
		this.shippableType = product.shippableType;
		this.priceList = product.priceList;
		this.listPrice = product.listPrice;
		this.skuCodeList = product.skuCodeList;
		this.attributesCodeList = product.attributesCodeList;
		this.skuOption = product.skuOption;
		this.bundlePricing = product.bundlePricing;
		this.bundleProductSKUList = product.bundleProductSKUList;
		this.bundleSelectionRule = product.bundleSelectionRule;
		this.bundleSelectionRuleValue = product.bundleSelectionRuleValue;
		this.enableDateTime = product.enableDateTime;
		this.disableDateTime = product.disableDateTime;
		this.releaseDateTime = product.releaseDateTime;
		this.minimumOrderQuantity = product.minimumOrderQuantity;
		this.shippingWeight = product.shippingWeight;
		this.shippingWidth = product.shippingWidth;
		this.shippingLength = product.shippingLength;
		this.shippingHeight = product.shippingHeight;
		this.shippingUnitsWeight = product.shippingUnitsWeight;
		this.shippingUnitsLength = product.shippingUnitsLength;
		this.notSoldSeparately = product.notSoldSeparately;
		this.enableDateTimeDays = product.enableDateTimeDays;
		this.disableDateTimeDays = product.disableDateTimeDays;
		this.releaseDateTimeDays = product.releaseDateTimeDays;
		this.skuOptionList = product.skuOptionList;
		this.skuCode = product.skuCode;
	}

	public String getProductSkuOptionItemByPartialCode(final String code) {
		String resultCode = "";
		for (int i = 0; i < skuOptionList.size(); i++) {
			if (skuOptionList.get(i).getItemCode().startsWith(code)) {
				resultCode = skuOptionList.get(i).getItemCode();
			}
		}
		return resultCode;
	}

	public Date getReleaseDateTime() {
		return releaseDateTime;
	}

	public List<ProductSkuItem> getSkuOptionList() {
		return skuOptionList;
	}

	public void setSkuOption(final ProductSkuItem item) {
		this.skuOptionList.add(item);
	}

	public String getSkuOptionCodeByPartialCode(final String code) {
		return skuOptionList
				.stream()
				.filter(sku -> sku.getItemCode().startsWith(code))
				.map(ProductSkuItem::getItemCode)
				.findFirst()
				.orElse(null);
	}

	public ProductSkuItem findProductSkuOptionItemByCode(final String productSkuCode) {
		return skuOptionList
				.stream()
				.filter(item -> item.getItemCode().equals(productSkuCode))
				.findFirst()
				.orElse(new ProductSkuItem());
	}

	public void setReleaseDateTime(final Date releaseDateTime) {
		this.releaseDateTime = releaseDateTime;
	}

	public void setAttributesCodeList(final String attributesCodeList) {
		this.attributesCodeList = attributesCodeList;
	}

	public String getReleaseDateTimeDays() {
		return releaseDateTimeDays;
	}

	public void setReleaseDateTimeDays(final String releaseDateTimeDays) {
		this.releaseDateTimeDays = releaseDateTimeDays;
	}

	public String getShippingUnitsWeight() {
		return shippingUnitsWeight;
	}

	public void setShippingUnitsWeight(final String shippingUnitsWeight) {
		this.shippingUnitsWeight = shippingUnitsWeight;
	}

	public String getShippingUnitsLength() {
		return shippingUnitsLength;
	}

	public void setShippingUnitsLength(final String shippingUnitsLength) {
		this.shippingUnitsLength = shippingUnitsLength;
	}

	public Date getDisableDateTime() {
		return disableDateTime;
	}

	public void setDisableDateTime(final Date disableDateTime) {
		this.disableDateTime = disableDateTime;
	}

	public String getEnableDateTimeDays() {
		return enableDateTimeDays;
	}

	public void setEnableDateTimeDays(final String enableDateTimeDays) {
		this.enableDateTimeDays = enableDateTimeDays;
	}

	public String getDisableDateTimeDays() {
		return disableDateTimeDays;
	}

	public void setDisableDateTimeDays(final String disableDateTimeDays) {
		this.disableDateTimeDays = disableDateTimeDays;
	}

	public String getNotSoldSeparately() {
		return notSoldSeparately;
	}

	public void setNotSoldSeparately(final String notSoldSeparately) {
		this.notSoldSeparately = notSoldSeparately;
	}

	public String getShippingWidth() {
		return shippingWidth;
	}

	public void setShippingWidth(final String shippingWidth) {
		this.shippingWidth = shippingWidth;
	}

	public String getShippingLength() {
		return shippingLength;
	}

	public void setShippingLength(final String shippingLength) {
		this.shippingLength = shippingLength;
	}

	public String getShippingHeight() {
		return shippingHeight;
	}

	public void setShippingHeight(final String shippingHeight) {
		this.shippingHeight = shippingHeight;
	}

	public String getShippingWeight() {
		return shippingWeight;
	}

	public void setShippingWeight(final String weight) {
		this.shippingWeight = weight;
	}

	public String getMinimumOrderQuantity() {
		return minimumOrderQuantity;
	}

	public void setMinimumOrderQuantity(final String minimumOrderQuantity) {
		this.minimumOrderQuantity = minimumOrderQuantity;
	}

	public Date getEnableDateTime() {
		return this.enableDateTime;
	}

	public void setEnableDateTime(final Date enableDateTime) {
		this.enableDateTime = enableDateTime;
	}

	public String getBundleSelectionRule() {
		return bundleSelectionRule;
	}

	public void setBundleSelectionRule(final String bundleSelectionRule) {
		this.bundleSelectionRule = bundleSelectionRule;
	}

	public String getBundleSelectionRuleValue() {
		return bundleSelectionRuleValue;
	}

	public void setBundleSelectionRuleValue(final String bundleSelectionRuleValue) {
		this.bundleSelectionRuleValue = bundleSelectionRuleValue;
	}

	public String getBundleProductSKUCodes() {
		return bundleProductSKUList;
	}

	public void setBundleProductSKUCodes(final String bundleProductSKUList) {
		this.bundleProductSKUList = bundleProductSKUList;
	}

	/**
	 * Gets Bundle Product Codes List.
	 *
	 * @return productSkuCodeList.
	 */
	public List<String> getBundleProductSKUList() {
		List<String> productSkuCodeList = new ArrayList<>();
		if (bundleProductSKUList == null) {
			return null;
		} else {
			String[] skuCodes = bundleProductSKUList.split(",");
			for (String product : skuCodes) {
				productSkuCodeList.add(product);
			}
			return productSkuCodeList;
		}
	}

	public String getBundlePricing() {
		return bundlePricing;
	}

	public void setBundlePricing(final String bundlePricing) {
		this.bundlePricing = bundlePricing;
	}


	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(final String skuCode) {
		this.skuCode = skuCode;
	}

	private String skuCode;

	public String getProductSKUCodes() {
		return skuCodeList;
	}

	public void setProductSKUCodes(final String codesList) {
		this.skuCodeList = codesList;
	}

	/**
	 * Gets SKU Codes List.
	 *
	 * @return productSkuCodeList.
	 */
	public List<String> getSKUCodeList() {
		List<String> productSkuCodeList = new ArrayList<>();
		if (skuCodeList == null) {
			return null;
		} else {
			String[] skuCodes = skuCodeList.split(",");
			for (String product : skuCodes) {
				productSkuCodeList.add(product);
			}
			return productSkuCodeList;
		}
	}

	/**
	 * Gets SKU Codes List.
	 *
	 * @return productSkuCodeList.
	 */
	public List<String> getAttributesCodeList() {
		List<String> productAttributesCodeList = new ArrayList<>();
		if (attributesCodeList == null) {
			return null;
		} else {
			String[] skuCodes = attributesCodeList.split(",");
			productAttributesCodeList.addAll(Arrays.asList(skuCodes));
			return productAttributesCodeList;
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

	public String getSKUOption() {
		return skuOption;
	}

	public void setSKUOption(final String skuOption) {
		this.skuOption = skuOption;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(final String productCode) {
		this.productCode = productCode;
	}
}
