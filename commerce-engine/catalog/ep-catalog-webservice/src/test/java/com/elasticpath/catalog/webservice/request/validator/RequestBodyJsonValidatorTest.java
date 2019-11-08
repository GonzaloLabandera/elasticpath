package com.elasticpath.catalog.webservice.request.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.webservice.request.entity.EventType;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;

@RunWith(MockitoJUnitRunner.class)
public class RequestBodyJsonValidatorTest {

	private static final String EVENT_MESSAGE_SCHEMA_PATH = "/schema/eventMessage.schema.json";

	@Test
	public void shouldReturnTrueWhenRequestBodyIsValid() {
		final RequestBody validRequestBody = createValidRequestBody();

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(validRequestBody)).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyIsEmpty() {
		final RequestBody invalidRequestBody = new RequestBody();

		RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullEventType() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.setEventType(null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullEventTypeClass() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getEventType().setEventClass(null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullEventTypeName() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getEventType().setName(null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullGuid() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.setGuid(null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyNotContainsDataType() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().remove("type");

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullDataType() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().put("type", null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyNotContainsDataStore() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().remove("store");

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullDataStore() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().put("store", null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyNotContainsDataModifiedDateTime() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().remove("modifiedDateTime");

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullDataModifiedDateTime() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().put("modifiedDateTime", null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyNotContainsDataCodes() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().remove("codes");

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenRequestBodyContainsNullDataCodes() {
		final RequestBody invalidRequestBody = createValidRequestBody();
		invalidRequestBody.getData().put("codes", null);

		final RequestBodyJsonValidator validator = new RequestBodyJsonValidator(EVENT_MESSAGE_SCHEMA_PATH, new ObjectMapper());

		assertThat(validator.validate(invalidRequestBody)).isFalse();
	}

	private RequestBody createValidRequestBody() {
		final RequestBody validRequestBody = new RequestBody();

		final EventType eventType = new EventType();
		eventType.setEventClass("CatalogEventType");
		eventType.setName("OPTIONS_UPDATED");

		validRequestBody.setEventType(eventType);
		validRequestBody.setGuid("AGGREGATE");

		final HashMap<String, Object> data = new HashMap<>();
		data.put("type", "option");
		data.put("store", "MyStore");
		data.put("modifiedDateTime", "2018-01-01T14:47:00+00:00");
		data.put("codes", Arrays.asList("COLOUR", "SHAPE"));

		validRequestBody.setData(data);

		return validRequestBody;
	}

}