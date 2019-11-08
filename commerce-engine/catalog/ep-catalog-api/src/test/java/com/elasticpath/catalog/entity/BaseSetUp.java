/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;

/**
 * Represents a base set up for tests.
 */
public class BaseSetUp {
	private static ObjectMapper objectMapper;

	/**
	 * Initializes object mapper.
	 */
	@BeforeClass
	public static void setUpObjectMapper() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
