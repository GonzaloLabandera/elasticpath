/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.tag;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.Dto;

/**
 * Contains mapping between XML and Tag domain object. Designed for JAXB.
 */
@XmlRootElement(name = TagDefinitionDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class TagDefinitionDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "tag";
	
	@XmlElement(name = "type", required = true)
	private String type;
	
	@XmlElement(name = "name", required = true)
	private String name;

	@XmlElement(name = "description")
	private String description;

	@XmlElementWrapper(name = "displayName")
	@XmlElement(name = "value", required = true)
	private List<DisplayValue> nameValues;

	@XmlElementWrapper(name = "dictionaries")
	@XmlElement(name = "dictionary", required = true)
	private List<String> dictionaries;

	@XmlAttribute(name = "code")
	private String code;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Get the list of localized names for this tag definition.
	 * @return a list of DisplayValue objects that represent the localized names for this tag definition
	 */
	public List<DisplayValue> getNameValues() {
		if (nameValues == null) {
			return Collections.emptyList();
		}
		return nameValues;
	}

	/**
	 * Set the list of localized names for this tag definition.
	 * @param nameValues the list of DisplayValue objects that represent the localized names for this tag definition
	 */
	public void setNameValues(final List<DisplayValue> nameValues) {
		this.nameValues = nameValues;
	}

	/**
	 * Get the list of dictionaries that this tag definition is associated to.
	 * @return a list of Strings that represent the dictionaries that this tag definition is associated to.
	 */
	public List<String> getDictionaries() {
		if (dictionaries == null) {
			return Collections.emptyList();
		}
		return dictionaries;
	}

	/**
 	 * Get the list of dictionaries that this tag definition is associated to.
	 * @param dictionaries a list of Strings that represent the dictionaries that this tag definition should be associated to
	 */
	public void setDictionaries(final List<String> dictionaries) {
		this.dictionaries = dictionaries;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}
}
