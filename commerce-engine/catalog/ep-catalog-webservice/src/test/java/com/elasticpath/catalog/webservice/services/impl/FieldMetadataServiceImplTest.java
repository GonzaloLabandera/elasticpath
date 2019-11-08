/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.CatalogReaderCapability;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetaDataReaderCapability;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.InvalidRequestParameterException;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link FieldMetadataServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FieldMetadataServiceImplTest {

	private static final String STORE_CODE = "store";
	private static final String CODE = "code";
	private static final String START_AFTER_STRING = "startAfterString";

	@Mock
	private FieldMetaDataReaderCapability reader;

	@Mock
	private TimeService timeService;

	private FieldMetadataServiceImpl fieldMetadataService;

	@Before
	public void setUp() {
		final CatalogProjectionPluginProvider catalogProjectionPluginProvider = mock(CatalogProjectionPluginProvider.class,
				Mockito.RETURNS_DEEP_STUBS);
		final Optional<CatalogReaderCapability> readerCapability = Optional.of(reader);

		when(catalogProjectionPluginProvider.getCatalogProjectionPlugin().getReaderCapability(any())).thenReturn(readerCapability);

		fieldMetadataService = new FieldMetadataServiceImpl(catalogProjectionPluginProvider, timeService);
	}

	@Test
	public void testThatFieldMetadataReaderCapabilityIsCalledInGetMethod() {
		when(reader.get(STORE_CODE, CODE)).thenReturn(Optional.empty());

		fieldMetadataService.get(STORE_CODE, CODE);

		verify(reader).get(STORE_CODE, CODE);
	}

	@Test
	public void testThatFieldMetadataReaderCapabilityCallFindAllMethodIfLimitIsCorrect() {
		final int testLimit = 2;
		final FindAllResponse<FieldMetadata> testResponse = new FindAllResponseImpl<>(
				new PaginationResponseImpl(testLimit, START_AFTER_STRING, false), ZonedDateTime.now(), Collections.emptyList());

		when(reader.findAll(any(), any(), any())).thenReturn(testResponse);

		fieldMetadataService.getAllFieldMetadata(STORE_CODE, String.valueOf(testLimit), START_AFTER_STRING, null, null);

		verify(reader).findAll(any(), any(), any());
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatFieldMetadataServiceThrowExceptionIfParameterIsLessThanZero() {
		final String invalidLimit = "-1";

		fieldMetadataService.getAllFieldMetadata(STORE_CODE, invalidLimit, START_AFTER_STRING, null, null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatFieldMetadataServiceThrowExceptionIfParameterIsLetter() {
		final String invalidLimit = "x";

		fieldMetadataService.getAllFieldMetadata(STORE_CODE, invalidLimit, START_AFTER_STRING, null, null);
	}

	@Test
	public void testThatFieldMetadataServiceConvertsValidDate() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
		fieldMetadataService.getAllFieldMetadata(STORE_CODE, "2", START_AFTER_STRING, "2018-01-01T14:47:00+00:00", null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatFieldMetadataServiceThrowExceptionIfInvalidDate() {
		fieldMetadataService.getAllFieldMetadata(STORE_CODE, "2", START_AFTER_STRING, "A2018-01-01T14:47:00+00:00", null);
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatModifiedSinceOffsetCannotBeSpecifiedIfModifiedSinceNotPresent() {
		fieldMetadataService.getAllFieldMetadata(STORE_CODE, "2", START_AFTER_STRING, null, "5");
	}

	@Test(expected = InvalidRequestParameterException.class)
	public void testThatModifiedSinceMustBeInThePast() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
		fieldMetadataService.getAllFieldMetadata(STORE_CODE, "2", START_AFTER_STRING, "3018-01-01T14:47:00+00:00", "5");
	}

	@Test()
	public void testThatFieldMetadataReaderCapabilityIsCalledInGetLatestFieldMetadatasWithCodesMethod() {
		when(reader.findAllWithCodes(STORE_CODE, Collections.emptyList())).thenReturn(Collections.emptyList());
		fieldMetadataService.getLatestFieldMetadataWithCodes(STORE_CODE, Collections.emptyList());
		verify(reader).findAllWithCodes(STORE_CODE, Collections.emptyList());
	}
}
