/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.importexport.common.dto.facet;

import static com.elasticpath.importexport.common.dto.facet.FacetsDTO.ROOT_ELEMENT;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.elasticpath.common.dto.search.FacetDTO;

/**
 * DTO for facets used for import export.
 */
@XmlRootElement(name = ROOT_ELEMENT)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "facetsDTO", propOrder = { })
public class FacetsDTO {

	/**
	 * The name of root element in xml representation.
	 */
	public static final String ROOT_ELEMENT = "facets";

	@XmlElement(name = "facet")
	private final List<FacetDTO> facets = new ArrayList<>();

	/**
	 * Get list of facets.
	 *
	 * @return list of facets
	 */
	public List<FacetDTO> getFacets() {
		return facets;
	}
}
