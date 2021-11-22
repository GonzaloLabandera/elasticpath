/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.dto.modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.elasticpath.common.dto.Dto;

/**
 * The implementation of the <code>Dto</code> interface that contains data of ModifierGroupFilter object.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = ModifierGroupFilterDTO.ROOT_ELEMENT)
public class ModifierGroupFilterDTO implements Dto {

	private static final long serialVersionUID = 1L;
	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "modifiergroupfilter";

	@XmlElement(name = "modifiercode", required = true)
	private String modifierCode;

	@XmlElement(name = "referenceGuid", required = true)
	private String referenceGuid;

	@XmlElement(name = "type", required = true)
	private String type;


	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getModifierCode() {
		return modifierCode;
	}

	public void setModifierCode(final String modifierCode) {
		this.modifierCode = modifierCode;
	}

	public String getReferenceGuid() {
		return referenceGuid;
	}

	public void setReferenceGuid(final String referenceGuid) {
		this.referenceGuid = referenceGuid;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("type", getType())
				.append("referenceGuid", getReferenceGuid())
				.append("modifierCode", getModifierCode())
				.toString();
	}
}
