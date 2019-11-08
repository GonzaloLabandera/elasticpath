/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.importexport.common.dto.sort;

import static com.elasticpath.importexport.common.dto.sort.SortAttributesDTO.ROOT_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.sort.SortAttributeDTO;

/**
 * DTO for sortAttributes used for import export.
 */
@XmlRootElement(name = ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "sortAttributesDTO", propOrder = { })
public class SortAttributesDTO {

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "sort_attributes";

	@XmlElement(name = "sort_attribute")
	private final List<SortAttributeDTO> sortAttributes = new ArrayList<>();

	/**
	 * Get list of sortAttributes.
	 *
	 * @return list of sortAttributes
	 */
	public List<SortAttributeDTO> getSortAttributes() {
		return sortAttributes;
	}
}
