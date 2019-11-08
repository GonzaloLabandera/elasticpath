/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.importexport.common.dto.tag;

import static com.elasticpath.importexport.common.dto.tag.TagGroupsDTO.ROOT_ELEMENT;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * DTO for tag groups used for import export.
 */
@XmlRootElement(name = ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tagGroupsDTO", propOrder = {})
public class TagGroupsDTO {

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "tag_groups";

	@XmlElement(name = "tag_group")
	private final List<TagGroupDTO> tagGroups = new ArrayList<>();

	/**
	 * Get list of tag groups.
	 *
	 * @return list of TagGroupDTO
	 */
	public List<TagGroupDTO> getTagGroups() {
		return tagGroups;
	}

	/**
	 * Find a particular tag group in the list by its guid/code.
	 *
	 * @param guid the guid to find the tag group for
	 * @return the TagGroupDTO from the list
	 */
	public TagGroupDTO getByTagGroupGuid(final String guid) {
		TagGroupDTO tagGroupDTO = null;
		for (TagGroupDTO matchDTO : getTagGroups()) {
			if (guid.equals(matchDTO.getCode())) {
				tagGroupDTO = matchDTO;
				break;
			}
		}
		return tagGroupDTO;
	}
}
