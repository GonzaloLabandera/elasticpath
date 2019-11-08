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
 * Wrapper JAXB entity for schema generation to collect a group of modifierGroups.
 */
@XmlRootElement(name = ModifierGroupsDTO.ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "modifierGroupsDTO", propOrder = { })

public class ModifierGroupsDTO implements Dto {
	private static final long serialVersionUID = 1L;

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "modifiergroups";

	@XmlElement(name = "modifiergroup")
	private final List<ModifierGroupDTO> modifiergroups = new ArrayList<>();

	/**
	 * Get list of modifier groups.
	 *
	 * @return list of modifier groups
	 */
	public List<ModifierGroupDTO> getModifierGroups() {
		return modifiergroups;
	}

}
