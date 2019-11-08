/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.dto.modifier;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.Dto;

/**
 * The implementation of the <code>Dto</code> interface that contains data of ModifierGroup object.
 */
@XmlRootElement(name = ModifierGroupDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class ModifierGroupDTO implements Dto {
	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "modifiergroup";

	@XmlElement(name = "code", required = true)
	private String code;

	@XmlElementWrapper(name = "displayname")
	@XmlElement(name = "value", required = true)
	private List<DisplayValue> values;

	@XmlElementWrapper(name = "modifierfields")
	@XmlElement(name = "modifierfield")
	private List<ModifierFieldDTO> modifierFields;

	/**
	 * Gets the modifierFields.
	 *
	 * @return the modifierFields
	 */
	public List<ModifierFieldDTO> getModifierFields() {
		return modifierFields;
	}

	/**
	 * Sets the modifierFields.
	 *
	 * @param modifierFields the modifierFields to set
	 */
	public void setModifierFields(final List<ModifierFieldDTO> modifierFields) {
		this.modifierFields = modifierFields;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the code to set
	 */
	public void setCode(final String code) {
		this.code = code;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<DisplayValue> getValues() {
		return values;
	}

	/**
	 * Sets the values.
	 *
	 * @param values the values to set
	 */
	public void setValues(final List<DisplayValue> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("code", getCode())
				.append("values", getValues())
				.append("modifierFields", getModifierFields())
				.toString();
	}

}
