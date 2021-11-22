/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.importexport.common.dto.xpf.setting;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * DTO for XPFSettingValueDTO.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class XPFSettingValueDTO {

	/**
	 * The name of root element in XML representation of the setting.
	 */
	public static final String ROOT_ELEMENT = "value";

	@XmlAttribute(name = "key")
	private String key;

	@XmlValue
	private String value;

	/**
	 * Get the setting value key.
	 *
	 * @return the setting value key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the setting value key.
	 *
	 * @param key the setting value key
	 */
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Get the setting value.
	 *
	 * @return the setting value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * Set the setting value.
	 *
	 * @param value the setting value
	 */
	public void setValue(final String value) {
		this.value = value;
	}
}
