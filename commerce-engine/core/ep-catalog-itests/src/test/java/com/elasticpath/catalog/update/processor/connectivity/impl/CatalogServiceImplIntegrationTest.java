/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.plugin.converter.ProjectionToEntityConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

/**
 * Integration tests for {@link com.elasticpath.catalog.plugin.service.CatalogServiceImpl}.
 */
public class CatalogServiceImplIntegrationTest extends DbTestCase {

	private static final String OPTION = "option";
	private static final String STORE = "store";
	private static final String EMPTY_STORE = Utils.uniqueCode("store");
	private static final String INVALID_CODE = "invalidCode";
	private static final int DEFAULT_PAGINATION_LIMIT = 10;
	private static final int FIVE_MINUTES = 20;
	private static final int TEN_MINUTES = 10;
	private static final int TWENTY_MINUTES = 20;


	@Autowired
	private CatalogService catalogService;

	@Autowired
	private CatalogProjectionHistoryRepository historyRepository;

	@Autowired
	private ProjectionToEntityConverter projectionToEntityConverter;

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	@Before
	public void setUp() {
		historyRepository.deleteAll();
		catalogProjectionRepository.deleteAll();
		createAndPersistProjectionEntities(Arrays.asList("A", "B", "C", "D", "E", "F"));
	}

	@Test
	public void shouldReturnLimitEqualTo10WhenLessThan10OptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(DEFAULT_PAGINATION_LIMIT);
	}

	@Test
	public void shouldReturnStartAfterEqualToEmptyStringWhenLessThan10OptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
	}

	@Test
	public void shouldReturnHasMoreResultEqualToTrueWhenLessThan10OptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isEqualTo(false);
	}

	@Test
	public void shouldReturnLimitEqualTo2When6OptionsInStoreAndRequestWithLimit2() {
		final PaginationRequest paginationRequest = createPaginationRequestWithLimit("2");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
	}

	@Test
	public void shouldReturnStartAfterEqualToBWhen6OptionsInStoreAndRequestWithLimit2() {
		final PaginationRequest paginationRequest = createPaginationRequestWithLimit("2");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEqualTo("B");
	}

	@Test
	public void shouldReturnHasMoreResultsEqualToTrueWhen6OptionsInStoreAndRequestWithLimit2() {
		final PaginationRequest paginationRequest = createPaginationRequestWithLimit("2");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isTrue();
	}

	@Test
	public void shouldReturnLimitEqualTo2When6OptionsInStoreAndRequestWithLimit2AndStartAfterB() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "B");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
	}

	@Test
	public void shouldReturnStartAfterEqualToBWhen6OptionsInStoreAndRequestWithLimit2AndStartAfterB() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "B");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEqualTo("D");
	}

	@Test
	public void shouldReturnHasMoreResultsEqualToTrueWhen6OptionsInStoreAndRequestWithLimit2AndStartAfterB() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "B");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isTrue();
	}

	@Test
	public void shouldReturnLimitEqualTo2When6OptionsInStoreAndRequestWithLimit2AndStartAfterD() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "D");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
	}

	@Test
	public void shouldReturnStartAfterEqualToBWhen6OptionsInStoreAndRequestWithLimit2AndStartAfterD() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "D");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
	}

	@Test
	public void shouldReturnHasMoreResultsEqualToTrueWhen6OptionsInStoreAndRequestWithLimit2AndStartAfterD() {
		final PaginationRequest paginationRequest = createPaginationRequest("2", "D");
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isFalse();
	}

	@Test
	public void shouldReturnLimitEqualTo10WhenNoOptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, EMPTY_STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(DEFAULT_PAGINATION_LIMIT);
	}

	@Test
	public void shouldReturnStartAfterEqualToEmptyStringWhenNoOptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, EMPTY_STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
	}

	@Test
	public void shouldReturnLimitedModifiedProjectionsSinceGivenDateFirstRequest() {
		Stream.of("B", "D", "E", "F")
				.map(code -> catalogProjectionRepository.extractProjectionEntity(OPTION, code, STORE))
				.map(projection -> createProjectionHistory(projection, TEN_MINUTES)).forEach(historyRepository::save);

		final PaginationRequest paginationRequest = new PaginationRequestImpl("2", null);
		final FindAllResponse<Option> projectionFindAllResponse = catalogService
				.readAll(OPTION, STORE, paginationRequest,
						new ModifiedSinceImpl(Date.from(new Date().toInstant().minus(15,
								ChronoUnit.MINUTES)),
								0L));

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isTrue();
		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEqualTo("D");
		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
		assertThat(projectionFindAllResponse.getResults().size()).isEqualTo(2);
		assertThat(projectionFindAllResponse.getResults().get(0).getIdentity().getCode()).isEqualTo("B");
		assertThat(projectionFindAllResponse.getResults().get(0).getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(projectionFindAllResponse.getResults().get(1).getIdentity().getCode()).isEqualTo("D");
		assertThat(projectionFindAllResponse.getResults().get(1).getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
	}

	@Test
	public void shouldReturnLimitedModifiedProjectionsSinceGivenDateSecondRequest() {
		Stream.of("B", "D", "E", "F")
				.map(code -> catalogProjectionRepository.extractProjectionEntity(OPTION, code, STORE))
				.map(projection -> createProjectionHistory(projection, TEN_MINUTES)).forEach(historyRepository::save);
		createProjectionHistory(catalogProjectionRepository.extractProjectionEntity(OPTION, "B", STORE), FIVE_MINUTES);
		final PaginationRequest paginationRequest = new PaginationRequestImpl("2", "D");

		final FindAllResponse<Option> projectionFindAllResponse = catalogService
				.readAll(OPTION, STORE, paginationRequest, new ModifiedSinceImpl(Date.from(new Date().toInstant().minus(15, ChronoUnit.MINUTES)),
						0L));

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isFalse();
		assertThat(projectionFindAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
		assertThat(projectionFindAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
		assertThat(projectionFindAllResponse.getResults().size()).isEqualTo(2);
		assertThat(projectionFindAllResponse.getResults().get(0).getIdentity().getCode()).isEqualTo("E");
		assertThat(projectionFindAllResponse.getResults().get(0).getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(projectionFindAllResponse.getResults().get(1).getIdentity().getCode()).isEqualTo("F");
		assertThat(projectionFindAllResponse.getResults().get(1).getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
	}

	@Test
	public void shouldReturnAllProjectionsSinceGivenDate() {
		final int expectedFindAllResponseResultsSize = 6;

		final PaginationRequest paginationRequest = new PaginationRequestImpl("999", null);
		final FindAllResponse<Option> projectionFindAllResponse = catalogService
				.readAll(OPTION, STORE, paginationRequest, new ModifiedSinceImpl(Date.from(new Date().toInstant().minus(30, ChronoUnit.MINUTES)),
						0L));

		assertThat(projectionFindAllResponse.getResults().size()).isEqualTo(expectedFindAllResponseResultsSize);
	}

	@Test
	public void shouldReturnHasMoreResultEqualToTrueWhenNoOptionsInStoreAndRequestWithoutParameters() {
		final PaginationRequest paginationRequest = createPaginationRequestWithoutParameters();
		final FindAllResponse<Projection> projectionFindAllResponse = catalogService.readAll(OPTION, EMPTY_STORE, paginationRequest,
				new ModifiedSinceImpl());

		assertThat(projectionFindAllResponse.getPagination().getNext().isHasMoreResults()).isEqualTo(false);
	}

	@Test
	public void shouldReturnListProjectionsWithSize2WhenRequestContains2ValidCodes() {
		Stream.of("A", "B", "C", "D", "E", "F")
				.map(code -> catalogProjectionRepository.extractProjectionEntity(OPTION, code, STORE))
				.map(projection -> createProjectionHistory(projection, TEN_MINUTES)).forEach(historyRepository::save);
		Stream.of("A", "C")
				.map(code -> catalogProjectionRepository.extractProjectionEntity(OPTION, code, STORE))
				.map(projection -> createProjectionHistory(projection, FIVE_MINUTES)).forEach(historyRepository::save);

		List<Projection> projections = catalogService.readAll(OPTION, STORE, Arrays.asList("A", "B"));

		assertThat(projections).hasSize(2);
	}

	@Test
	public void shouldReturnListProjectionsWithSize1WhenRequestContains2CodesAndOneOfThemInvalid() {
		List<Projection> projections = catalogService.readAll(OPTION, STORE, Arrays.asList("A", INVALID_CODE));

		assertThat(projections).hasSize(1);
	}

	@Test
	public void shouldAddDeletedProjectionAsTombstone() {
		final Optional<Option> projection = catalogService.read(OPTION, "A", STORE);
		if (projection.isPresent()) {
			final Option option = new Option(projection.get().getIdentity().getCode(), projection.get().getIdentity().getStore(),
					projection.get().getTranslations(), projection.get().getModifiedDateTime(), true);

			catalogService.saveOrUpdate(option);
		}

		final ProjectionId id = new ProjectionId();
		id.setCode("A");
		id.setType(OPTION);
		id.setStore(STORE);

		final ProjectionEntity p = catalogProjectionRepository.findOne(id);

		assertThat(p).isNotNull();
		assertThat(p.isDeleted()).isTrue();
		assertThat(p.getSchemaVersion()).isNull();
		assertThat(p.getContentHash()).isNull();
		assertThat(p.getContent()).isNull();
	}

	private void createAndPersistProjectionEntities(final List<String> codes) {
		codes.stream().map(this::createProjectionEntity).forEach(catalogProjectionRepository::save);
	}

	private ProjectionHistoryEntity createProjectionHistory(final Optional<ProjectionEntity> projectionEntity, final long minusMinutes) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setCode(projectionEntity.get().getCode());
		projectionId.setType(projectionEntity.get().getType());
		projectionId.setStore(projectionEntity.get().getStore());

		final ProjectionEntity entity = new ProjectionEntity();
		entity.setContent("{\"translations\":[{\"language\":\"en\",\"displayName\":\"displayName\",\"optionValues\":[]}]}");
		entity.setContentHash(projectionEntity.get().getContentHash());
		entity.setSchemaVersion(projectionEntity.get().getSchemaVersion());
		entity.setDeleted(projectionEntity.get().isDeleted());
		entity.setProjectionId(projectionId);
		entity.setProjectionDateTime(Date.from(new Date().toInstant().minus(minusMinutes, ChronoUnit.MINUTES)));
		final ProjectionHistoryEntity historyEntity = projectionToEntityConverter.convertToProjectionHistory(projectionEntity.get());
		historyEntity.setProjectionEntity(entity);

		return historyEntity;
	}

	private ProjectionEntity createProjectionEntity(final String code) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setType(OPTION);
		projectionId.setCode(code);
		projectionId.setStore(STORE);

		final ProjectionEntity projectionEntity = new ProjectionEntity();
		projectionEntity.setProjectionId(projectionId);
		projectionEntity.setProjectionDateTime(Date.from(new Date().toInstant().minus(TWENTY_MINUTES, ChronoUnit.MINUTES)));
		projectionEntity.setDeleted(false);
		projectionEntity.setSchemaVersion("1");
		projectionEntity.setContent("{}");
		projectionEntity.setContentHash("contentHash");
		projectionEntity.setGuid(UUID.randomUUID().toString());

		return projectionEntity;
	}

	private PaginationRequest createPaginationRequestWithoutParameters() {
		return new PaginationRequestImpl(null, null);
	}

	private PaginationRequest createPaginationRequestWithLimit(final String limit) {
		return new PaginationRequestImpl(limit, null);
	}

	private PaginationRequest createPaginationRequest(final String limit, final String startAfter) {
		return new PaginationRequestImpl(limit, startAfter);
	}

}
