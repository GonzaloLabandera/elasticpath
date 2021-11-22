/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.importexport.common.dto.xpf.setting;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.elasticpath.common.dto.Dto;

/**
 * DTO for XPFSettingDTO.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class XPFSettingDTO implements Dto {
	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in XML representation of the setting.
	 */
	public static final String ROOT_ELEMENT = "setting";

	@XmlElement(name = "key")
	private String settingKey;

	@XmlElement(name = "data_type")
	private String dataType;

	@XmlElement(name = "collection_type")
	private String collectionType;

	@XmlElementWrapper(name = "values")
	@XmlElement(name = "value")
	private List<XPFSettingValueDTO> settingValues;


	public String getSettingKey() {
		return settingKey;
	}

	public void setSettingKey(final String settingKey) {
		this.settingKey = settingKey;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(final String dataType) {
		this.dataType = dataType;
	}

	public String getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(final String collectionType) {
		this.collectionType = collectionType;
	}

	public List<XPFSettingValueDTO> getSettingValues() {
		return settingValues;
	}

	public void setSettingValues(final List<XPFSettingValueDTO> settingValues) {
		this.settingValues = settingValues;
	}
}
