/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.service;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.openjpa.persistence.EntityExistsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.plugin.converter.ProjectionToEntityConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.exception.OverflowAttemptsException;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.plugin.repository.impl.CatalogRepositoryImpl;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.core.messaging.catalog.CatalogEventType;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.impl.EventTypeFactory;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link CatalogServiceImpl}.
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class CatalogServiceImplTest {

	private static final String TYPE = "type";
	private static final String CODE = "code";
	private static final String PARENT_CODE = "parentCode";
	private static final String STORE = "store";
	private static final String STORED_HASH = "stored_hash";
	private static final String NEW_HASH = "new_hash";
	private static final Date DATE = new Date();
	private static final String AGGREGATE = "AGGREGATE";
	private static final String EMPTY_STRING = "";
	private static final long DEFAULT_MODIFIED_SINCE_OFFSET = 30;
	private static final int DEFAULT_LIMIT = 10;
	private static final int ZERO_ELEMENT = 0;
	private static final int FIRST_ELEMENT = 1;
	private static final int SECOND_ELEMENT = 2;
	private static final int THIRD_ELEMENT = 3;
	private static final int FOURTH_ELEMENT = 4;
	private static final int FIFTH_ELEMENT = 5;
	private static final int FIVE_ELEMENTS = 5;

	@Mock
	private CatalogProjectionRepository repository;
	@Mock
	private CatalogProjectionHistoryRepository historyRepository;

	@Mock
	private ProjectionToEntityConverter projectionToEntityConverter;

	@Mock
	private EventTypeFactory eventTypeFactory;

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private EventMessage eventMessage;

	@Mock
	private EventMessagePublisher eventMessagePublisher;

	@Mock
	private TimeService timeService;

	private CatalogServiceImpl catalogDataService;

	@Before
	public void setUp() {
		catalogDataService = new CatalogServiceImpl(new CatalogRepositoryImpl(repository, historyRepository),
				timeService,
				projectionToEntityConverter,
				eventMessageFactory,
				eventMessagePublisher,
				eventTypeFactory, DEFAULT_MODIFIED_SINCE_OFFSET);

		when(timeService.getCurrentTime()).thenReturn(DATE);
	}

	@Test
	public void shouldCallConvertProjectionWhenProjectionEntityWithSameTypeCodeStoreNotExistsInDatabase() {
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.empty());
		final Projection projection = mockProjection();
		final ProjectionEntity projectionEntity = mock(ProjectionEntity.class);
		when(projectionEntity.getProjectionDateTime()).thenReturn(DATE);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);

		catalogDataService.saveOrUpdate(projection);

		verify(projectionToEntityConverter).convertFromProjection(projection);
	}

	@Test
	public void shouldCallSaveProjectionEntityWhenProjectionEntityWithSameTypeCodeStoreNotExistsInDatabase() {
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.empty());
		final Projection projection = mockProjection();
		final ProjectionEntity projectionEntity = mockProjectionEntity();
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(CatalogEventType.OPTIONS_UPDATED);
		when(eventMessageFactory.createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData())).thenReturn(eventMessage);
		doNothing().when(eventMessagePublisher).publish(eventMessage);

		catalogDataService.saveOrUpdate(projection);

		verify(repository).save(projectionEntity);
		verify(repository).extractProjectionEntity(TYPE, CODE, STORE);
		verify(eventTypeFactory).getEventType(TYPE);
		verify(eventMessageFactory).createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData());
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void shouldCallConvertProjectionWithNewVersionWhenProjectionEntityWithSameTypeCodeStoreExistsInDatabase() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity(STORED_HASH);
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity(NEW_HASH);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(CatalogEventType.OPTIONS_UPDATED);
		when(eventMessageFactory.createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData())).thenReturn(eventMessage);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(storedProjectionEntity)).thenReturn(historyEntity);

		doNothing().when(eventMessagePublisher).publish(eventMessage);

		catalogDataService.saveOrUpdate(projection);

		verify(projectionToEntityConverter).convertFromProjection(projection);
		verify(repository).extractProjectionEntity(TYPE, CODE, STORE);
		verify(eventTypeFactory).getEventType(TYPE);
		verify(eventMessageFactory).createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData());
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void shouldCallSaveUpdatedProjectionEntityWhenProjectionEntityWithSameTypeCodeStoreHasNotEqualHashCode() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity(STORED_HASH);
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity(NEW_HASH);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(CatalogEventType.OPTIONS_UPDATED);
		when(eventMessageFactory.createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData())).thenReturn(eventMessage);
		doNothing().when(eventMessagePublisher).publish(eventMessage);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(storedProjectionEntity)).thenReturn(historyEntity);

		catalogDataService.saveOrUpdate(projection);

		verify(historyRepository).save(historyEntity);
		verify(repository).extractProjectionEntity(TYPE, CODE, STORE);
		verify(eventTypeFactory).getEventType(TYPE);
		verify(eventMessageFactory).createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData());
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void shouldSendMessageInCaseDelete() {
		ProjectionEntity entity = new ProjectionEntity();
		ProjectionId projectionId = new ProjectionId();
		projectionId.setType(TYPE);
		projectionId.setCode(CODE);
		projectionId.setStore(STORE);
		entity.setProjectionId(projectionId);

		when(repository.findNotDeletedProjectionEntities(TYPE, CODE)).thenReturn(Collections.singletonList(entity));
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(CatalogEventType.OPTIONS_UPDATED);
		when(eventMessageFactory.createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData())).thenReturn(eventMessage);
		doNothing().when(eventMessagePublisher).publish(eventMessage);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(entity)).thenReturn(historyEntity);

		catalogDataService.delete(TYPE, CODE);

		verify(eventMessageFactory).createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData());
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void shouldNOTSendMessageInCaseProjectionHasEqualHashCodeInDatabase() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity();
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity();
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);

		catalogDataService.saveOrUpdate(projection);

		verify(eventTypeFactory, never()).getEventType(any());
		verify(eventMessageFactory, never()).createEventMessage(any(), any(), any());
		verify(eventMessagePublisher, never()).publish(any());
	}

	@Test
	public void shouldConvertToProjectionWhenProjectionEntityWithSameTypeCodeStoreExistsInDatabase() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity();
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		when(projectionToEntityConverter.convertToProjection(storedProjectionEntity)).thenReturn(projection);

		catalogDataService.read(TYPE, CODE, STORE);

		verify(repository).extractProjectionEntity(TYPE, CODE, STORE);
		verify(projectionToEntityConverter).convertToProjection(storedProjectionEntity);
	}

	@Test
	public void noPaginationAndResultLessThanLimitProjectionsTest() {

		when(repository.extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, new PaginationRequestImpl().getLimit() + 1,
				new PaginationRequestImpl().getStartAfter())).
				thenReturn(Arrays.asList(new ProjectionEntity(), new ProjectionEntity()));
		final Projection projection = mockProjection();
		when(projectionToEntityConverter.convertToProjection(new ProjectionEntity())).thenReturn(projection);
		PaginationRequest pagination = new PaginationRequestImpl();

		FindAllResponse<Option> findAllResponse = catalogDataService.readAll(TYPE, STORE, pagination, new ModifiedSinceImpl());

		verify(repository).extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, new PaginationRequestImpl().getLimit() + 1,
				new PaginationRequestImpl().getStartAfter());
		verify(projectionToEntityConverter, times(2)).convertToProjection(new ProjectionEntity());
		verify(timeService, times(1)).getCurrentTime();

		assertThat(findAllResponse.getResults()).hasSize(2);
		assertThat(findAllResponse.getPagination().getNext().getLimit()).isEqualTo(DEFAULT_LIMIT);
		assertThat(findAllResponse.getPagination().getNext().isHasMoreResults()).isFalse();
		assertThat(findAllResponse.getCurrentDateTime()).isNotNull();
	}

	@Test
	public void noPaginationAndResultMoreThanLimitProjectionsTest() {
		PaginationRequest paginationRequest = new PaginationRequestImpl("5", EMPTY_STRING);
		List<ProjectionEntity> entities = getEntities();

		when(repository.extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter())).
				thenReturn(entities);

		when(projectionToEntityConverter.convertToProjection(entities.get(ZERO_ELEMENT))).thenReturn(createOption("A"));
		when(projectionToEntityConverter.convertToProjection(entities.get(FIRST_ELEMENT))).thenReturn(createOption("B"));
		when(projectionToEntityConverter.convertToProjection(entities.get(SECOND_ELEMENT))).thenReturn(createOption("C"));
		when(projectionToEntityConverter.convertToProjection(entities.get(THIRD_ELEMENT))).thenReturn(createOption("D"));
		when(projectionToEntityConverter.convertToProjection(entities.get(FOURTH_ELEMENT))).thenReturn(createOption("E"));
		when(projectionToEntityConverter.convertToProjection(entities.get(FIFTH_ELEMENT))).thenReturn(createOption("F"));

		FindAllResponse<Option> findAllResponse = catalogDataService.readAll(TYPE, STORE, paginationRequest, new ModifiedSinceImpl());

		verify(repository).extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, paginationRequest.getLimit() + 1,
				new PaginationRequestImpl().getStartAfter());
		verify(projectionToEntityConverter).convertToProjection(entities.get(ZERO_ELEMENT));
		verify(projectionToEntityConverter).convertToProjection(entities.get(FIRST_ELEMENT));
		verify(projectionToEntityConverter).convertToProjection(entities.get(SECOND_ELEMENT));
		verify(projectionToEntityConverter).convertToProjection(entities.get(THIRD_ELEMENT));
		verify(projectionToEntityConverter).convertToProjection(entities.get(FOURTH_ELEMENT));
		verify(projectionToEntityConverter).convertToProjection(entities.get(FIFTH_ELEMENT));
		verify(timeService, times(1)).getCurrentTime();

		assertThat(findAllResponse.getResults()).hasSize(FIVE_ELEMENTS);
		assertThat(findAllResponse.getPagination().getNext().getLimit()).isEqualTo(FIVE_ELEMENTS);
		assertThat(findAllResponse.getPagination().getNext().isHasMoreResults()).isTrue();
		assertThat(findAllResponse.getCurrentDateTime()).isNotNull();
	}

	@Test
	public void middlePaginationTest() {
		final int entitiesSubListFromIndex = 2;
		final int entitiesSubListToIndex = 5;

		PaginationRequest paginationRequest = new PaginationRequestImpl("2", "B");
		List<ProjectionEntity> entities = getEntities();

		when(repository.extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter())).
				thenReturn(entities.subList(entitiesSubListFromIndex, entitiesSubListToIndex));

		when(projectionToEntityConverter.convertToProjection(entities.get(SECOND_ELEMENT))).thenReturn(createOption("C"));
		when(projectionToEntityConverter.convertToProjection(entities.get(THIRD_ELEMENT))).thenReturn(createOption("D"));
		when(projectionToEntityConverter.convertToProjection(entities.get(FOURTH_ELEMENT))).thenReturn(createOption("E"));

		FindAllResponse<Option> findAllResponse = catalogDataService.readAll(TYPE, STORE, paginationRequest, new ModifiedSinceImpl());

		assertThat(findAllResponse.getResults()).hasSize(2);
		assertThat(findAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
		assertThat(findAllResponse.getPagination().getNext().getStartAfter()).isEqualTo("D");
		assertThat(findAllResponse.getPagination().getNext().isHasMoreResults()).isTrue();
		assertThat(findAllResponse.getCurrentDateTime()).isNull();
	}

	@Test
	public void lastPaginationTest() {
		final int entitiesSubListFromIndex = 4;
		final int entitiesSubListToIndex = 6;

		PaginationRequest paginationRequest = new PaginationRequestImpl("2", "D");
		List<ProjectionEntity> entities = getEntities();

		when(repository.extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter())).
				thenReturn(entities.subList(entitiesSubListFromIndex, entitiesSubListToIndex));
		when(projectionToEntityConverter.convertToProjection(entities.get(FOURTH_ELEMENT))).thenReturn(createOption("E"));
		when(projectionToEntityConverter.convertToProjection(entities.get(FIFTH_ELEMENT))).thenReturn(createOption("F"));

		FindAllResponse<Option> findAllResponse = catalogDataService.readAll(TYPE, STORE, paginationRequest, new ModifiedSinceImpl());

		assertThat(findAllResponse.getResults()).hasSize(2);
		assertThat(findAllResponse.getPagination().getNext().getLimit()).isEqualTo(2);
		assertThat(findAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
		assertThat(findAllResponse.getPagination().getNext().isHasMoreResults()).isFalse();
		assertThat(findAllResponse.getCurrentDateTime()).isNull();
	}

	@Test
	public void emptyResultWithPaginationTest() {
		PaginationRequest paginationRequest = new PaginationRequestImpl();

		when(repository.extractProjectionsByTypeAndStoreWithPagination(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter())).
				thenReturn(Collections.emptyList());

		FindAllResponse<Option> findAllResponse = catalogDataService.readAll(TYPE, STORE, paginationRequest, new ModifiedSinceImpl());

		assertThat(findAllResponse.getResults()).isEmpty();
		assertThat(findAllResponse.getPagination().getNext().getLimit()).isEqualTo(DEFAULT_LIMIT);
		assertThat(findAllResponse.getPagination().getNext().getStartAfter()).isEmpty();
		assertThat(findAllResponse.getPagination().getNext().isHasMoreResults()).isFalse();
		assertThat(findAllResponse.getCurrentDateTime()).isNotNull();
	}

	@Test
	public void testThatQueryWithPaginationAndModifiedSinceIsCalledWithGivenOffset() {
		final long modifiedSinceOffset = 5L;
		PaginationRequest paginationRequest = new PaginationRequestImpl();
		ModifiedSinceImpl modifiedSince = new ModifiedSinceImpl(new Date(1), modifiedSinceOffset);
		Date modifiedSinceWithOffset = Date.from(modifiedSince.getModifiedSince().toInstant().minus(modifiedSince.getModifiedSinceOffset(),
				ChronoUnit.MINUTES));

		when(repository.extractProjectionsByTypeAndStoreWithPaginationAndModified(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter(), modifiedSinceWithOffset)).
				thenReturn(Collections.emptyList());

		catalogDataService.readAll(TYPE, STORE, paginationRequest, modifiedSince);

		verify(repository).extractProjectionsByTypeAndStoreWithPaginationAndModified(TYPE, STORE, paginationRequest.getLimit() + 1,
				paginationRequest.getStartAfter(), modifiedSinceWithOffset);
	}

	@Test
	public void testThatInCaseOptimisticLockingExceptionRequestWillBeRetried() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity(STORED_HASH);
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity(NEW_HASH);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(CatalogEventType.OPTIONS_UPDATED);
		when(eventMessageFactory.createEventMessage(CatalogEventType.OPTIONS_UPDATED, AGGREGATE, getData())).thenReturn(eventMessage);
		doNothing().when(eventMessagePublisher).publish(eventMessage);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(storedProjectionEntity)).thenReturn(historyEntity);
		when(historyRepository.save(historyEntity)).thenThrow(new OptimisticLockingFailureException("")).thenReturn(new ProjectionHistoryEntity());

		catalogDataService.saveOrUpdate(projection);

		verify(historyRepository, times(2)).save(historyEntity);
	}

	@Test(expected = OverflowAttemptsException.class)
	public void testThatInCaseOptimisticLockingExceptionMoreThenTwiceSaveOrUpdateMethodThrowsException() {
		final ProjectionEntity storedProjectionEntity = mockProjectionEntity(STORED_HASH);
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(storedProjectionEntity));
		final Projection projection = mockProjection();
		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity(NEW_HASH);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(storedProjectionEntity)).thenReturn(historyEntity);
		when(historyRepository.save(historyEntity)).thenThrow(new OptimisticLockingFailureException(""));

		catalogDataService.saveOrUpdate(projection);

		verify(historyRepository, times(2)).save(historyEntity);
	}

	@Test
	public void testThatInCaseEntityExistsExceptionRequestWillBeRetried() {
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.empty());
		final Projection projection = mockProjection();
		final ProjectionEntity projectionEntity = mock(ProjectionEntity.class);
		when(projectionEntity.getProjectionDateTime()).thenReturn(DATE);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);
		when(repository.save(projectionEntity)).thenThrow(new DataIntegrityViolationException("", new EntityExistsException("", null, null, true)))
				.thenReturn(new ProjectionEntity());

		catalogDataService.saveOrUpdate(projection);

		verify(repository, times(2)).save(projectionEntity);
	}

	@Test(expected = OverflowAttemptsException.class)
	public void testThatInCaseEntityExistsExceptionMoreThenTwiceSaveOrUpdateMethodThrowsException() {
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.empty());
		final Projection projection = mockProjection();
		final ProjectionEntity projectionEntity = mock(ProjectionEntity.class);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);
		when(repository.save(projectionEntity)).thenThrow(new DataIntegrityViolationException("", new EntityExistsException("", null, null, true)));

		catalogDataService.saveOrUpdate(projection);
	}

	@Test
	public void shouldCallPublishWhenCallSaveOrUpdateAll() {
		final EventType eventType = mock(EventType.class);
		when(eventTypeFactory.getEventType(TYPE)).thenReturn(eventType);
		when(eventMessageFactory.createEventMessage(any(EventType.class), anyString(), anyMap())).thenReturn(eventMessage);

		final NameIdentity nameIdentity = mock(NameIdentity.class);
		final Projection projection = mock(Projection.class);
		when(projection.getIdentity()).thenReturn(nameIdentity);

		final ProjectionEntity projectionEntity = mock(ProjectionEntity.class);
		when(projectionEntity.getType()).thenReturn(TYPE);
		when(projectionEntity.getStore()).thenReturn(STORE);
		when(projectionEntity.getCode()).thenReturn(CODE);

		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);

		catalogDataService.saveOrUpdateAll(Collections.singletonList(projection));

		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void testThatRepositoryShouldCallToGetAndSaveParentCategory() {
		final ProjectionEntity entity = new ProjectionEntity();
		ProjectionId projectionId = new ProjectionId();
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		projectionId.setCode(CODE);
		projectionId.setStore(STORE);
		entity.setProjectionId(projectionId);

		final ProjectionEntity parentEntity = new ProjectionEntity();
		ProjectionId parentProjectionId = new ProjectionId();
		parentProjectionId.setType(CATEGORY_IDENTITY_TYPE);
		parentProjectionId.setCode(PARENT_CODE);
		parentProjectionId.setStore(STORE);
		parentEntity.setProjectionId(parentProjectionId);
		parentEntity.setContentHash("Content");

		final ProjectionEntity updatedParentEntity = new ProjectionEntity();
		ProjectionId updatedParentProjectionId = new ProjectionId();
		updatedParentProjectionId.setType(CATEGORY_IDENTITY_TYPE);
		updatedParentProjectionId.setCode(PARENT_CODE);
		updatedParentProjectionId.setStore(STORE);
		updatedParentEntity.setProjectionId(updatedParentProjectionId);
		updatedParentEntity.setContentHash("ContentUpdated");

		final ProjectionEntity deletedEntity = new ProjectionEntity();

		final Category category = mockCategory(null);
		final List<String> children = new ArrayList<>();
		children.add(CODE);
		final Category parentCategory = mockCategory(children);
		final ProjectionHistoryEntity historyEntity = mockProjectionHistoryEntity();

		when(repository.findNotDeletedProjectionEntities(CATEGORY_IDENTITY_TYPE, CODE)).thenReturn(Collections.singletonList(entity));
		when(projectionToEntityConverter.convertToProjection(entity)).thenReturn(category);
		when(repository.findNotDeletedProjectionEntities(CATEGORY_IDENTITY_TYPE, PARENT_CODE)).thenReturn(Collections.singletonList(parentEntity));
		when(projectionToEntityConverter.convertToProjection(parentEntity)).thenReturn(parentCategory);
		when(repository.extractProjectionEntity(parentCategory.getIdentity().getType(),
				parentCategory.getIdentity().getCode(),
				parentCategory.getIdentity().getStore())).thenReturn(Optional.of(parentEntity));
		when(projectionToEntityConverter.convertFromProjection(parentCategory)).thenReturn(updatedParentEntity);
		when(projectionToEntityConverter.convertToProjectionHistory(parentEntity)).thenReturn(historyEntity);
		when(projectionToEntityConverter.convertToProjectionHistory(entity)).thenReturn(historyEntity);
		when(projectionToEntityConverter.convertToDeletedEntity(entity, DATE)).thenReturn(deletedEntity);

		catalogDataService.delete(CATEGORY_IDENTITY_TYPE, CODE);

		verify(repository).findNotDeletedProjectionEntities(CATEGORY_IDENTITY_TYPE, PARENT_CODE);
		verify(historyRepository).save(historyEntity);
	}

	@Test
	public void shouldCallRepositorySaveAndReturnTrueWhenProjectionWithSameIdentityNotExistsInDatabase() {
		final Projection projection = mockProjection();

		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.empty());

		final ProjectionEntity projectionEntity = mockProjectionEntity();
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);

		final boolean result = catalogDataService.saveOrUpdate(projection);

		verify(repository).save(projectionEntity);
		assertThat(result).isTrue();
	}

	@Test
	public void shouldNotCallRepositorySaveAndReturnFalseWhenProjectionWithSameIdentityExistsInDatabase() {
		final Projection projection = mockProjection();

		final ProjectionEntity projectionEntity = mockProjectionEntity();

		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(projectionEntity));
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(projectionEntity);

		final boolean result = catalogDataService.saveOrUpdate(projection);

		verify(repository, never()).save(any(ProjectionEntity.class));
		assertThat(result).isFalse();
	}

	@Test
	public void shouldCallHistoryRepositorySaveAndReturnTrueWhenProjectionWithSameIdentityExistsInDatabaseButHaveNotSameHashCode() {
		final Projection projection = mockProjection();

		final ProjectionEntity projectionEntity = mockProjectionEntity(STORED_HASH);
		when(repository.extractProjectionEntity(TYPE, CODE, STORE)).thenReturn(Optional.of(projectionEntity));

		final ProjectionEntity updatedProjectionEntity = mockProjectionEntity(NEW_HASH);
		when(projectionToEntityConverter.convertFromProjection(projection)).thenReturn(updatedProjectionEntity);

		final ProjectionHistoryEntity projectionHistoryEntity = mockProjectionHistoryEntity();
		when(projectionToEntityConverter.convertToProjectionHistory(projectionEntity)).thenReturn(projectionHistoryEntity);

		final boolean result = catalogDataService.saveOrUpdate(projection);

		verify(historyRepository).save(projectionHistoryEntity);
		assertThat(result).isTrue();
	}

	private Projection mockProjection() {
		final NameIdentity nameIdentity = new NameIdentity(TYPE, CODE, STORE);
		final Projection projection = mock(Projection.class);
		when(projection.getIdentity()).thenReturn(nameIdentity);
		return projection;
	}

	private ProjectionHistoryEntity mockProjectionHistoryEntity() {
		final ProjectionHistoryEntity projectionHistoryEntity = mock(ProjectionHistoryEntity.class);
		when(projectionHistoryEntity.getProjectionDateTime()).thenReturn(DATE);
		when(projectionHistoryEntity.getType()).thenReturn(TYPE);
		when(projectionHistoryEntity.getCode()).thenReturn(CODE);
		when(projectionHistoryEntity.getStore()).thenReturn(STORE);

		return projectionHistoryEntity;
	}

	private ProjectionEntity mockProjectionEntity(final String hash) {
		final ProjectionEntity projectionEntity = mockProjectionEntity();
		when(projectionEntity.getContentHash()).thenReturn(hash);

		return projectionEntity;
	}

	private ProjectionEntity mockProjectionEntity() {
		final ProjectionEntity projectionEntity = mock(ProjectionEntity.class);
		when(projectionEntity.getContentHash()).thenReturn(STORED_HASH);
		when(projectionEntity.getType()).thenReturn(TYPE);
		when(projectionEntity.getStore()).thenReturn(STORE);
		when(projectionEntity.getCode()).thenReturn(CODE);
		when(projectionEntity.getProjectionDateTime()).thenReturn(DATE);

		return projectionEntity;
	}

	private Map<String, Object> getData() {
		final Map<String, Object> data = new HashMap<>();
		data.put("type", TYPE);
		data.put("store", STORE);
		data.put("modifiedDateTime", ZonedDateTime.ofInstant(DATE.toInstant(), ZoneId.of("GMT")).format(ISO_OFFSET_DATE_TIME));
		data.put("codes", Collections.singletonList(CODE));

		return Collections.unmodifiableMap(data);
	}

	private Option createOption(final String code) {
		return new Option(code, null, null, null, false);

	}

	private List<ProjectionEntity> getEntities() {
		ProjectionEntity aEntity = new ProjectionEntity();
		ProjectionId idA = new ProjectionId();
		idA.setCode("A");
		idA.setType(OPTION_IDENTITY_TYPE);
		aEntity.setProjectionId(idA);

		ProjectionEntity bEntity = new ProjectionEntity();
		ProjectionId idB = new ProjectionId();
		idB.setType(OPTION_IDENTITY_TYPE);
		idB.setCode("B");
		bEntity.setProjectionId(idB);

		ProjectionEntity cEntity = new ProjectionEntity();
		ProjectionId idC = new ProjectionId();
		idC.setType(OPTION_IDENTITY_TYPE);
		idC.setCode("C");
		cEntity.setProjectionId(idC);

		ProjectionEntity dEntity = new ProjectionEntity();
		ProjectionId idD = new ProjectionId();
		idD.setType(OPTION_IDENTITY_TYPE);
		idD.setCode("D");
		dEntity.setProjectionId(idD);

		ProjectionEntity eEntity = new ProjectionEntity();
		ProjectionId idE = new ProjectionId();
		idE.setType(OPTION_IDENTITY_TYPE);
		idE.setCode("E");
		eEntity.setProjectionId(idE);

		ProjectionEntity fEntity = new ProjectionEntity();
		ProjectionId idF = new ProjectionId();
		idF.setType(OPTION_IDENTITY_TYPE);
		idF.setCode("F");
		fEntity.setProjectionId(idF);
		return Arrays.asList(aEntity, bEntity, cEntity, dEntity, eEntity, fEntity);
	}

	private Category mockCategory(final List<String> children) {
		final ProjectionProperties projectionProperties = new ProjectionProperties(CODE, STORE,
				ZonedDateTime.now(), false);
		final List<Property> categorySpecificProperties = Collections.emptyList();
		final CategoryProperties categoryProperties = new CategoryProperties(projectionProperties, categorySpecificProperties);

		final Object extensions = new Object();
		final List<CategoryTranslation> categoryTranslations = Collections.emptyList();

		return new Category(categoryProperties, extensions, categoryTranslations, children,
				new AvailabilityRules(null, null), Collections.emptyList(), PARENT_CODE);
	}
}