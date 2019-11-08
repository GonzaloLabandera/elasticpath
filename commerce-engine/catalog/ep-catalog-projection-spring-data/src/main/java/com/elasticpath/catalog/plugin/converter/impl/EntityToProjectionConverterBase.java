/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.exception.ConverterException;

/**
 * Represents a base converter.
 */
public class EntityToProjectionConverterBase {

	private final ObjectMapper objectMapper;

	/**
	 * Constructor.
	 *
	 * @param objectMapper  singleton object mapper.
	 */
	public EntityToProjectionConverterBase(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Convert json content of projection to content of projection.

	 * @param source is projection with contains content to convert.
	 * @param valueType is class of projection content.
	 * @param <T> type of content of projection.
	 * @return content of projection.
	 */
	protected <T> T readFromJson(final ProjectionEntity source, final Class<T> valueType) {
		try {
			return objectMapper.readValue(source.getContent(), valueType);
		} catch (IOException e) {
			throw new ConverterException(e);
		}
	}
}
