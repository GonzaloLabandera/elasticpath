/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an entity for NameIdentity.
 */
public class NameIdentity {

	private final String type;
	private final String code;
	private final String store;

	/**
	 * NameIdentity constructor.
	 *
	 * @param type  projection type.
	 * @param code  projection code.
	 * @param store projection store.
	 */
	@JsonCreator
	public NameIdentity(@JsonProperty("type") final String type,
						@JsonProperty("code") final String code,
						@JsonProperty("store") final String store) {
		this.type = type;
		this.code = code;
		this.store = store;
	}

	/**
	 * Return the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Return the code.
	 *
	 * @return the code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Return the store code.
	 *
	 * @return the store code.
	 */
	public String getStore() {
		return store;
	}

}
