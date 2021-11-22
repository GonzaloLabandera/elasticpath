/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;

public class ModifierFieldsMapperTest {

	@Test
	public void shouldSerializeMapToJson() {
		Map<String, String> mapToSerialize = new LinkedHashMap<>();
		mapToSerialize.put("special chars", " \",'`~!@#$%^&*()-_=+[]{}\\|;:<>,./?");
		mapToSerialize.put("greek", "αλφάβητο"); //alphabet
		mapToSerialize.put("cyrillic", "абецеда"); //alphabet
		mapToSerialize.put("chinese simplified", "建造"); //the building
		mapToSerialize.put("chinese traditional", "建築"); //the building

		StringBuilder expected = new StringBuilder("{")
				.append("\"special chars\":\" \\\",'`~!@#$%^&*()-_=+[]{}\\\\|;:<>,./?\",")
				.append("\"greek\":\"αλφάβητο\",")
				.append("\"cyrillic\":\"абецеда\",")
				.append("\"chinese simplified\":\"建造\",")
				.append("\"chinese traditional\":\"建築\"")
				.append("}");

		ModifierFieldsMapWrapper mapWrapper = new ModifierFieldsMapWrapper(mapToSerialize);
		String serializedMap = ModifierFieldsMapper.toJSON(mapWrapper);

		assertThat(serializedMap)
				.isEqualTo(expected.toString());
	}
	@Test
	public void shouldDeSerializeJsonToMap() {
		String jsonToDeserialize = "{\"special chars\":\" \\\",'`~!@#$%^&*()-_=+[]{}\\\\|;:<>,./?\",\"greek\":\"αλφάβητο\","
				+ "\"cyrillic\":\"абецеда\",\"chinese simplified\":\"建造\",\"chinese traditional\":\"建築\"}";

		Map<String, String> expectedMap = new LinkedHashMap<>();
		expectedMap.put("special chars", " \",'`~!@#$%^&*()-_=+[]{}\\|;:<>,./?");
		expectedMap.put("greek", "αλφάβητο"); //alphabet
		expectedMap.put("cyrillic", "абецеда"); //alphabet
		expectedMap.put("chinese simplified", "建造"); //the building
		expectedMap.put("chinese traditional", "建築"); //the building

		ModifierFieldsMapWrapper actualMapWrapper = ModifierFieldsMapper.fromJSON(jsonToDeserialize);

		assertThat(actualMapWrapper.getMap())
				.isEqualTo(expectedMap);
	}
}
