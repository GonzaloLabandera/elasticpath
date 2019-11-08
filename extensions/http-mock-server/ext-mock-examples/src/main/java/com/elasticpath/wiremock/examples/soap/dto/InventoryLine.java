/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.wiremock.examples.soap.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A DTO that models a record level element in the request and response for
 * {@link com.elasticpath.wiremock.examples.soap.DynamicSoapResponseDefinitionTransformer}.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InventoryLine", propOrder = {
		"sku",
		"dcId",
		"quantity"
})
public class InventoryLine {

	@XmlElement(name = "sku")
	private String sku;

	@XmlElement(name = "dcId")
	private Integer dcId;

	@XmlElement(name = "quantity")
	private Double quantity;

	public String getSku() {
		return sku;
	}

	public void setSku(final String sku) {
		this.sku = sku;
	}

	public Integer getDcId() {
		return dcId;
	}

	public void setDcId(final Integer dcId) {
		this.dcId = dcId;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(final Double quantity) {
		this.quantity = quantity;
	}

}
