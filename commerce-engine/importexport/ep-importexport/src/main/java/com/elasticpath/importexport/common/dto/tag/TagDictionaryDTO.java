/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.dto.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.elasticpath.common.dto.Dto;

/**
 * Contains mapping between XML and TagDictionary domain object. Designed for JAXB.
 */
@XmlRootElement(name = TagGroupDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class TagDictionaryDTO implements Dto {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "dictionary";

	private String name;

	/**
	 * Get the name of the dictionary.
	 *
	 * @return the String value of the dictionary name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the dictionary.
	 *
	 * @param name the String value of the dictionary name
	 */
	public void setName(final String name) {
		this.name = name;
	}
}
