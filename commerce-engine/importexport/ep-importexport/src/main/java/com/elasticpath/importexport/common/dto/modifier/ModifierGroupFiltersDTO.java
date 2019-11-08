/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.dto.modifier;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.Dto;

/**
 * Wrapper JAXB entity for schema generation to collect a group of modifierGroup filters.
 */
@XmlRootElement(name = ModifierGroupFiltersDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "modifierGroupFiltersDTO", propOrder = { })

public class ModifierGroupFiltersDTO implements Dto {
	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "modifiergroupfilters";


	@XmlElement(name = "modifiergroupfilter")
	private final List<ModifierGroupFilterDTO> modifiergroupFilters = new ArrayList<>();

	/**
	 * Get list of modifier group filters.
	 *
	 * @return list of modifier group filters.
	 */
	public List<ModifierGroupFilterDTO> getModifierGroupFilters() {
		return modifiergroupFilters;
	}

}
