/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */
package com.elasticpath.persistence.openjpa.util;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.enhance.PersistenceCapable;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.misc.types.Modifiable;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.persistence.openjpa.support.JPAUtil;

/**
 *	This class is responsible for (de)serializing a map with modifier values (e.g. cart/order data) into JSON and vice versa.
 *	It can be used in all domain classes with map collections and String key-value pairs.
 */
public final class ModifierFieldsMapper {
	private static final String MODIFIER_FIELDS_FIELD = "modifierFields";
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	/**
	 * Private constructor since this class should never be instantiated.
	 */
	private ModifierFieldsMapper() {
		//do nothing
	}

	/**
	 * Serialize a map to a JSON string.
	 *
	 * @param modifierFields the {@link ModifierFieldsMapWrapper} with modifier fields to serialize
	 * @return JSON string with modifier values
	 */
	public static String toJSON(final ModifierFieldsMapWrapper modifierFields) {
		if (modifierFields == null || modifierFields.getMap().isEmpty()) {
			return null;
		}

		try {
			return JSON_MAPPER.writeValueAsString(modifierFields.getMap());
		} catch (JsonProcessingException e) {
			throw new EpSystemException("Error occurred while serializing map " + modifierFields.getMap() + " to JSON", e);
		}
	}

	/**
	 * De-serialize JSON string to a {@link ModifierFieldsMapWrapper}.
	 *
	 * @param json the db value containing modifier values in JSON format
	 * @return new {@link ModifierFieldsMapWrapper} with a map with properties
	 */
	@SuppressWarnings("unchecked")
	public static ModifierFieldsMapWrapper fromJSON(final String json) {
		//this check is required because set/get ModifierFields method can be called multiple times during flush-out
		if (StringUtils.isBlank(json)) {
			return new ModifierFieldsMapWrapper();
		}

		try {
			ModifierFieldsMapWrapper modifierFields = new ModifierFieldsMapWrapper();
			modifierFields.putAll(JSON_MAPPER.readValue(json, Map.class));

			return modifierFields;
		} catch (JsonProcessingException e) {
			throw new EpSystemException("Error occurred while de-serializing JSON string " + json, e);
		}
	}

	/**
	 * Load modifier fields if entity's "hasModifiers" flag is true and the modifiers are not loaded.
	 * To allow access to detached modifiers map, the "modifierFields" is flagged as loaded.
	 *
	 * @param entity the entity to load the modifiers for
	 */
	public static void loadModifierFieldsIfRequired(final Object entity) {
		Modifiable modifiable = (Modifiable) entity;
		PersistenceCapable pcInstance = (PersistenceCapable) entity;

		if (modifiable.getHasModifiers() && !JPAUtil.isFieldLoaded(pcInstance, MODIFIER_FIELDS_FIELD)) {
			modifiable.getModifierFields();
		}

		JPAUtil.markFieldAsLoaded(pcInstance, MODIFIER_FIELDS_FIELD);
	}
}
