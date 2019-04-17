/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.common.dto.search;

import static com.elasticpath.common.dto.search.FacetDTOConstants.DISPLAY_NAME_MAP;
import static com.elasticpath.common.dto.search.FacetDTOConstants.END;
import static com.elasticpath.common.dto.search.FacetDTOConstants.START;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.Facet;

/**
 * DTO Assembler for Facets.
 */
public class FacetDtoAssembler extends AbstractDtoAssembler<FacetDTO, Facet> {

	private static final String ERROR_PARSING_JSON = "Error parsing json: ";
	private static final String ERROR_SERIALIZING_TO_JSON = "Error serializing to json: ";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private BeanFactory beanFactory;

	@Override
	public Facet getDomainInstance() {
		return beanFactory.getBean(ContextIdNames.FACET);
	}

	@Override
	public FacetDTO getDtoInstance() {
		return new FacetDTO();
	}

	@Override
	public void assembleDto(final Facet source, final FacetDTO target) {

		try {
			target.setDisplayValues(displayNameJsonToDisplayValueList(source.getDisplayName()));
			target.setRangeFacetValues(rangeFacetsJsonToRangeFacetDTOs(source.getRangeFacetValues()));
		} catch (IOException e) {
			throw new EpServiceException(ERROR_PARSING_JSON, e);
		}

		target.setFacetGuid(source.getFacetGuid());
		target.setBusinessObjectId(source.getBusinessObjectId());
		target.setFacetName(source.getFacetName());
		target.setFieldKeyType(source.getFieldKeyType());
		target.setStoreCode(source.getStoreCode());
		target.setFacetType(source.getFacetType());
		target.setSearchableOption(source.getSearchableOption());
		target.setFacetGroup(source.getFacetGroup());
	}

	@Override
	public void assembleDomain(final FacetDTO source, final Facet target) {
		try {
			target.setDisplayName(displayValuesToJson(source.getDisplayValues()));
			target.setRangeFacetValues(rangeFacetsToJson(source.getRangeFacetValues()));
		} catch (JsonProcessingException e) {
			throw new EpServiceException(ERROR_SERIALIZING_TO_JSON, e);
		}
		target.setFacetGuid(source.getFacetGuid());
		target.setBusinessObjectId(source.getBusinessObjectId());
		target.setFacetName(source.getFacetName());
		target.setFieldKeyType(source.getFieldKeyType());
		target.setStoreCode(source.getStoreCode());
		target.setFacetType(source.getFacetType());
		target.setSearchableOption(source.getSearchableOption());
		target.setFacetGroup(source.getFacetGroup());
	}

	private String displayValuesToJson(final List<DisplayValue> displayValues) throws JsonProcessingException {
		Map<String, String> displayNameMap = new HashMap<>();
		displayValues.forEach(entry -> displayNameMap.put(entry.getLanguage(), entry.getValue()));
		return OBJECT_MAPPER.writeValueAsString(displayNameMap);
	}

	private String rangeFacetsToJson(final List<RangeFacetDTO> rangeFacetDTOs) throws JsonProcessingException {
		SortedSet<RangeFacet> rangeFacets = new TreeSet<>();

		for (RangeFacetDTO rangeFacetDTO : rangeFacetDTOs) {
			Map<String, String> displayMap = new HashMap<>();
			for (DisplayValue displayValue : rangeFacetDTO.getDisplayValues()) {
				displayMap.put(displayValue.getLanguage(), displayValue.getValue());
			}
			rangeFacets.add(new RangeFacet(rangeFacetDTO.getStart(), rangeFacetDTO.getEnd(), displayMap));
		}

		return OBJECT_MAPPER.writeValueAsString(rangeFacets);
	}

	private List<DisplayValue> displayNameJsonToDisplayValueList(final String displayNameJson) throws IOException {
		List<DisplayValue> displayValueList = new ArrayList<>();

		OBJECT_MAPPER.readTree(displayNameJson).fields()
				.forEachRemaining(entry -> displayValueList.add(new DisplayValue(entry.getKey(), entry.getValue().textValue())));

		return displayValueList;
	}

	private List<RangeFacetDTO> rangeFacetsJsonToRangeFacetDTOs(final String rangeFacetsJson) throws IOException {
		List<RangeFacetDTO> rangeFacetDTOs = new ArrayList<>();

		for (JsonNode jsonNode : OBJECT_MAPPER.readTree(rangeFacetsJson)) {
			RangeFacetDTO rangeFacetDTO = new RangeFacetDTO();

			JsonNode startNode = jsonNode.get(START);
			JsonNode endNode = jsonNode.get(END);

			rangeFacetDTO.setStart(startNode.getNodeType() == JsonNodeType.NULL ? null : new BigDecimal(startNode.asText()));
			rangeFacetDTO.setEnd(endNode.getNodeType() == JsonNodeType.NULL ? null : new BigDecimal(endNode.asText()));

			List<DisplayValue> displayValueList = new ArrayList<>();
			jsonNode.get(DISPLAY_NAME_MAP).fields()
					.forEachRemaining(entry -> displayValueList.add(new DisplayValue(entry.getKey(), entry.getValue().textValue())));
			rangeFacetDTO.setDisplayValues(displayValueList);

			rangeFacetDTOs.add(rangeFacetDTO);
		}

		return rangeFacetDTOs;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
