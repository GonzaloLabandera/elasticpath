/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.tag;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.Dto;

/**
 * Contains mapping between XML and TagGroup domain object. Designed for JAXB.
 */
@XmlRootElement(name = TagGroupDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class TagGroupDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "tag_group";

	@XmlElementWrapper(name = "displayName")
	@XmlElement(name = "value", required = true)
	private List<DisplayValue> nameValues;

	@XmlElementWrapper(name = "definitions", required = true)
	@XmlElement(name = "tag", required = true)
	private Set<TagDefinitionDTO> tags;

	@XmlAttribute(name = "code", required = true)
	private String code;

	/**
	 * Get the list of localized names for this tag group.
	 * @return a list of DisplayValue objects that represent the localized names for this tag group
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
	 * Get the list of tag definitions that belong to this tag group.
	 * @return a list of TagDefinitionDTO that are associated to this tag group
	 */
	public Set<TagDefinitionDTO> getTags() {
		if (tags == null) {
			return Collections.emptySet();
		}
		return tags;
	}

	/**
	 * Set the list of tag definitions that belong to this tag group.
	 * @param tags the list of TagDefinitionDTO objects that should be associated to this tag group.
	 *
	 */
	public void setTags(final Set<TagDefinitionDTO> tags) {
		this.tags = tags;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}
}
