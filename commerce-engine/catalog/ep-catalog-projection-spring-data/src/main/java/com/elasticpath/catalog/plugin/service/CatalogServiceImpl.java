/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.service;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.EntityExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.plugin.converter.ProjectionToEntityConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.exception.OverflowAttemptsException;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.plugin.repository.CatalogRepository;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.ModifiedSince;
import com.elasticpath.catalog.reader.PaginationRequest;
import com.elasticpath.catalog.reader.PaginationResponse;
import com.elasticpath.catalog.reader.impl.FindAllResponseImpl;
import com.elasticpath.catalog.reader.impl.PaginationResponseImpl;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.impl.EventTypeFactory;
import com.elasticpath.service.misc.TimeService;

/**
 * Represents an implementation for {@link CatalogService} for {@link ProjectionEntity} persistence.
 */
public class CatalogServiceImpl implements CatalogService {

	private static final int HAS_NEXT = 1;
	private static final String EVENT_MESSAGE_GUID = "AGGREGATE";
	private static final int PREVIOUS = 1;

	private final CatalogProjectionRepository projectionRepository;
	private final CatalogProjectionHistoryRepository historyRepository;
	private final TimeService timeService;
	private final ProjectionToEntityConverter projectionToEntityConverter;
	private final EventMessageFactory eventMessageFactory;
	private final EventMessagePublisher eventMessagePublisher;
	private final EventTypeFactory eventTypeFactory;
	private final long defaultModifiedSinceOffset;

	/**
	 * Constructor.
	 *
	 * @param repository                  the repository of catalog projections.
	 * @param timeService                 the time service.
	 * @param projectionToEntityConverter the converter of {@link Projection} to {@link ProjectionEntity}.
	 * @param eventMessageFactory         event message factory.
	 * @param eventMessagePublisher       event message publisher.
	 * @param eventTypeFactory            event type factory.
	 * @param defaultModifiedSinceOffset  default value for modifiedSinceOffset in minutes.
	 */
	public CatalogServiceImpl(final CatalogRepository repository,
							  final TimeService timeService,
							  final ProjectionToEntityConverter projectionToEntityConverter,
							  final EventMessageFactory eventMessageFactory,
							  final EventMessagePublisher eventMessagePublisher,
							  final EventTypeFactory eventTypeFactory,
							  final long defaultModifiedSinceOffset) {
		this.projectionRepository = repository.getProjectionRepository();
		this.historyRepository = repository.getProjectionHistoryRepository();
		this.timeService = timeService;
		this.projectionToEntityConverter = projectionToEntityConverter;
		this.eventMessageFactory = eventMessageFactory;
		this.eventMessagePublisher = eventMessagePublisher;
		this.eventTypeFactory = eventTypeFactory;
		this.defaultModifiedSinceOffset = defaultModifiedSinceOffset;
	}

	/**
	 * Save or update in database new projection entity and add new journal entity for updated projection.
	 *
	 * @param projection the projection to save or update.
	 * @param <T>        the type of projection.
	 * @return true if projection is saved successfully, false if projection is not saved.
	 */
	@Override
	public <T extends Projection> boolean saveOrUpdate(final T projection) {
		return saveOrUpdate(projection, true);
	}

	/**
	 * Save or update in database new projection entity and add new journal entity for updated projection.
	 * In case {@link OptimisticLockingFailureException} or {@link EntityExistsException} makes additional attempt to save or update.
	 *
	 * @param projection   the projection to save or update.
	 * @param firstAttempt the number of attempt to save or update.
	 * @param <T>          the type of projection.
	 * @return true if projection is saved successfully, false if projection is not saved.
	 */
	private <T extends Projection> boolean saveOrUpdate(final T projection, final boolean firstAttempt) {
		try {
			return trySaveOrUpdate(projection);
		} catch (final OptimisticLockingFailureException e) {
			if (firstAttempt) {
				return saveOrUpdate(projection, false);
			} else {
				throw new OverflowAttemptsException("Second attempt to persist projection entity has failed.", e);
			}
		} catch (final DataIntegrityViolationException e) {
			if (firstAttempt && e.getCause() instanceof EntityExistsException) {
				return saveOrUpdate(projection, false);
			} else {
				throw new OverflowAttemptsException("Second attempt to persist projection entity has failed.", e);
			}
		}
	}

	/**
	 * Save or update in database new projection entity and add new journal entity for updated projection.
	 *
	 * @param projection the projection to save or update.
	 * @param <T>        the type of projection.
	 * @return true if projection is saved successfully, false if projection is not saved.
	 */
	private <T extends Projection> boolean trySaveOrUpdate(final T projection) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(projection.getIdentity().getStore());
		projectionId.setType(projection.getIdentity().getType());
		projectionId.setCode(projection.getIdentity().getCode());
		final Optional<ProjectionEntity> projectionEntityOptional = projectionRepository.extractProjectionEntity(projection.getIdentity().getType(),
				projection.getIdentity().getCode(),
				projection.getIdentity().getStore());

		if (projectionEntityOptional.isPresent()) {
			final ProjectionEntity projectionEntity = projectionEntityOptional.get();
			final ProjectionEntity updatedProjectionEntity = projectionToEntityConverter.convertFromProjection(projection);
			if (!Objects.equals(projectionEntity.getContentHash(), updatedProjectionEntity.getContentHash())) {
				final ProjectionHistoryEntity historyEntity = projectionToEntityConverter.convertToProjectionHistory(projectionEntity);
				updateProjectionEntity(projectionEntity, updatedProjectionEntity);
				historyEntity.setProjectionEntity(projectionEntity);
				historyRepository.save(historyEntity);
				sendCatalogEvent(historyEntity);

				return true;
			}
		} else {
			final ProjectionEntity projectionEntity = projectionToEntityConverter.convertFromProjection(projection);
			projectionRepository.save(projectionEntity);
			sendCatalogEvent(projectionEntity);

			return true;
		}

		return false;
	}

	/**
	 * Save in database new journal entity for deleted projection.
	 *
	 * @param type type of {@link ProjectionEntity}
	 * @param code code of {@link ProjectionEntity}
	 */
	@Override
	public void delete(final String type, final String code) {
		final List<ProjectionHistoryEntity> projectionHistoryEntities = projectionRepository.findNotDeletedProjectionEntities(type, code).stream()
				.map(this::createAsDeleted)
				.collect(toList());

		historyRepository.save(projectionHistoryEntities);

		projectionHistoryEntities.forEach(this::sendCatalogEvent);
	}

	@Override
	public void delete(final String type, final String store, final String code) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setType(type);
		projectionId.setStore(store);
		projectionId.setCode(code);

		final ProjectionEntity projectionEntity = projectionRepository.findOne(projectionId);

		if (Objects.nonNull(projectionEntity)) {
			final ProjectionHistoryEntity projectionHistoryEntity = createAsDeleted(projectionEntity);

			historyRepository.save(projectionHistoryEntity);

			sendCatalogEvent(projectionHistoryEntity);
		}
	}

	@Override
	public <T extends Projection> void deleteAll(final List<T> projections) {
		final List<ProjectionEntity> projectionEntities = projections.stream()
				.map(Projection::getIdentity)
				.map(this::extractProjectionEntity)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());

		final List<ProjectionHistoryEntity> projectionHistoryEntities = projectionEntities.stream().map(this::createAsDeleted).collect(toList());

		historyRepository.save(projectionHistoryEntities);
		sendCompositeCatalogEvents(projectionEntities);
	}

	/**
	 * Save in database new journal entity for deleted projection.
	 *
	 * @param projections projections to delete.
	 */
	@Override
	public void delete(final List<? extends Projection> projections) {
		projections.forEach(projection -> delete(projection.getIdentity().getType(), projection.getIdentity().getCode()));
	}

	/**
	 * Reads a journal entity from database and converts them to projections.
	 *
	 * @param type  type of {@link ProjectionEntity}.
	 * @param code  code of {@link ProjectionEntity}.
	 * @param store store of {@link ProjectionEntity}.
	 * @return projections.
	 */
	@Override
	public <T extends Projection> Optional<T> read(final String type, final String code, final String store) {
		return projectionRepository.extractProjectionEntity(type, code, store)
				.map(projectionToEntityConverter::convertToProjection);
	}

	/**
	 * Reads a list of projections from a datasource.
	 *
	 * @param type the type of the projection.
	 * @param code the guid of the projection.
	 * @param <T>  type of projection.
	 * @return the list of projections.
	 */
	@Override
	public <T extends Projection> List<T> readAll(final String type, final String code) {
		return projectionRepository.extractProjectionsByTypeAndCode(type, code).stream()
				.map(projectionToEntityConverter::<T>convertToProjection)
				.collect(Collectors.toList());
	}

	/**
	 * Reads a list of projections from a datasource.
	 *
	 * @param type  the type of the projection.
	 * @param codes the list of projections codes.
	 * @param <T>   type of projection.
	 * @return the list of projections.
	 */
	@Override
	public <T extends Projection> List<T> readAll(final String type, final List<String> codes) {
		return projectionRepository.findLatestProjectionsWithCodes(type, codes).stream()
				.map(projectionToEntityConverter::<T>convertToProjection)
				.collect(Collectors.toList());
	}

	/**
	 * Reads all entities from database for given store and pagination, after that and converts them to projections.
	 * The results are sorted by projection code in ascending order.
	 * To calculate pagination extracts from datasource pagination limit + {@value HAS_NEXT}
	 *
	 * @param type          type of {@link ProjectionEntity}.
	 * @param store         store of {@link ProjectionEntity}.
	 * @param pagination    pagination fo result.
	 * @param modifiedSince pagination threshold that instructs the service to return results starting after the provided data.
	 * @return projections.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Projection> FindAllResponse<T> readAll(final String type, final String store, final PaginationRequest pagination,
															 final ModifiedSince modifiedSince) {
		final List<ProjectionEntity> projectionEntities = extractAllProjections(type, store, pagination, modifiedSince);

		final List<T> projections = projectionEntities
				.stream()
				.map(entity -> (T) projectionToEntityConverter.convertToProjection(entity))
				.collect(toList());

		return new FindAllResponseImpl<>(calculatePaginationResponse(projections, pagination),
				maybeAddCurrentTime(pagination),
				calculateResult(projections, pagination));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Projection> List<T> readAll(final String type, final String store, final List<String> codes) {
		return projectionRepository.findLatestProjectionsWithCodes(type, store, codes).stream()
				.map(entity -> (T) projectionToEntityConverter.convertToProjection(entity))
				.collect(toList());
	}

	@Override
	public <T extends Projection> List<T> saveOrUpdateAll(final List<T> projections) {
		final Map<ProjectionEntity, Optional<ProjectionEntity>> newAndExistProjectionEntities =
				projections.stream().collect(Collectors.toMap(projectionToEntityConverter::convertFromProjection,
						projection -> this.extractProjectionEntity(projection.getIdentity())));

		final List<ProjectionHistoryEntity> projectionHistoryEntities = newAndExistProjectionEntities.entrySet().stream()
				.filter(entrySet -> entrySet.getValue().isPresent())
				.filter(entrySet -> !Objects.equals(entrySet.getKey().getContentHash(), entrySet.getValue().get().getContentHash()))
				.map(entrySet -> createProjectionHistoryEntity(entrySet.getValue().get(), entrySet.getKey()))
				.collect(toList());

		historyRepository.save(projectionHistoryEntities);

		final List<ProjectionEntity> newProjectionEntities = newAndExistProjectionEntities.entrySet().stream()
				.filter(entrySet -> !entrySet.getValue().isPresent())
				.map(Map.Entry::getKey)
				.collect(toList());

		projectionRepository.save(newProjectionEntities);

		final List<ProjectionEntity> newAndUpdatedProjections = projectionHistoryEntities
				.stream()
				.map(ProjectionHistoryEntity::getProjectionEntity)
				.collect(toList());
		newAndUpdatedProjections.addAll(newProjectionEntities);

		sendCompositeCatalogEvents(newAndUpdatedProjections);

		return projections;
	}

	@Override
	public int removeAll(final String type) {
		return projectionRepository.deleteAllProjectionsInBatchByType(type);
	}

	/**
	 * Reads nearest date projections to expire.
	 *
	 * @return a nearest date projections to expire.
	 */
	@Override
	public Optional<Date> readNearestExpiredTime() {
		return projectionRepository.extractNearestExpiredTime(timeService.getCurrentTime());
	}

	private List<ProjectionEntity> extractAllProjections(final String type, final String store, final PaginationRequest pagination,
														 final ModifiedSince modifiedSince) {
		if (Objects.isNull(modifiedSince.getModifiedSince())) {
			return projectionRepository.extractProjectionsByTypeAndStoreWithPagination(type,
					store,
					pagination.getLimit() + HAS_NEXT,
					pagination.getStartAfter());
		} else {
			return projectionRepository.extractProjectionsByTypeAndStoreWithPaginationAndModified(type,
					store,
					pagination.getLimit() + HAS_NEXT,
					pagination.getStartAfter(),
					calculateModifiedSince(modifiedSince));
		}
	}

	/**
	 * Result result of modifiedSince as modifiedSince date - modifiedSinceOffset.
	 *
	 * @param modifiedSince contains given modifiedSince and offset.
	 * @return calculated modifiedSince value.
	 */
	private Date calculateModifiedSince(final ModifiedSince modifiedSince) {
		final long offset = Optional.ofNullable(modifiedSince.getModifiedSinceOffset()).orElse(defaultModifiedSinceOffset);
		return Date.from(modifiedSince.getModifiedSince().toInstant().minus(offset, ChronoUnit.MINUTES));
	}

	/**
	 * Create new ProjectionEntity and mark it as deleted.
	 * Set up:
	 * deleted = true
	 * schemaVersion = null
	 * contentHash = null
	 * content = null
	 *
	 * @param projectionEntity source.
	 * @return created ProjectionEntity.
	 */
	private ProjectionHistoryEntity createAsDeleted(final ProjectionEntity projectionEntity) {
		if (projectionEntity.getType().equals(CATEGORY_IDENTITY_TYPE)) {
			updateParentCategory(projectionEntity);
		}

		final ProjectionHistoryEntity historyEntity = projectionToEntityConverter.convertToProjectionHistory(projectionEntity);
		final ProjectionEntity deletedEntity = projectionToEntityConverter.convertToDeletedEntity(projectionEntity, timeService.getCurrentTime());

		historyEntity.setProjectionEntity(deletedEntity);

		return historyEntity;
	}

	/**
	 * Update child list for parent Category.
	 *
	 * @param projectionEntity represent category entity, for which the parent will update.
	 */
	private void updateParentCategory(final ProjectionEntity projectionEntity) {
		final Category category = projectionToEntityConverter.convertToProjection(projectionEntity);
		final List<Category> parentCategories = extractParentCategoryProjections(category);

		parentCategories.forEach(parentCategory -> parentCategory
				.getChildren()
				.remove(category.getIdentity().getCode()));

		parentCategories.forEach(this::saveOrUpdate);
	}

	/**
	 * Extract parent categories.
	 *
	 * @param category category, from which categories will be extracted.
	 * @return parent Categories.
	 */
	private List<Category> extractParentCategoryProjections(final Category category) {
		return projectionRepository
				.findNotDeletedProjectionEntities(CATEGORY_IDENTITY_TYPE, category.getParent())
				.stream()
				.map(parentEntity -> (Category) projectionToEntityConverter.convertToProjection(parentEntity))
				.collect(toList());
	}

	/**
	 * Sends a catalog event message.
	 *
	 * @param projectionEntity projection entity to send an event for.
	 */
	private void sendCatalogEvent(final ProjectionEntity projectionEntity) {
		sendCatalogEvent(projectionEntity.getType(),
				projectionEntity.getStore(),
				projectionEntity.getProjectionDateTime(),
				Collections.singletonList(projectionEntity.getCode()));
	}

	/**
	 * Sends a catalog event message.
	 *
	 * @param historyEntity projection history entity to send an event for.
	 */
	private void sendCatalogEvent(final ProjectionHistoryEntity historyEntity) {
		sendCatalogEvent(historyEntity.getType(),
				historyEntity.getStore(),
				historyEntity.getProjectionDateTime(),
				Collections.singletonList(historyEntity.getCode()));
	}

	/**
	 * Sends a composite catalog events messages.
	 *
	 * @param projectionEntities list of projection entities to send an event for.
	 */
	private void sendCompositeCatalogEvents(final Collection<ProjectionEntity> projectionEntities) {
		projectionEntities.stream().collect(groupingBy(ProjectionEntity::getType, groupingBy(ProjectionEntity::getStore)))
				.forEach((type, projectionEntitiesGroupingByStore) -> projectionEntitiesGroupingByStore
						.forEach((store, entities) -> sendCatalogEvent(type, store, timeService.getCurrentTime(), extractCodes(entities))));
	}

	/**
	 * Sends a catalog event message.
	 *
	 * @param type             projection type.
	 * @param store            projection store to send an event for.
	 * @param modifiedDateTime modified date time.
	 * @param codes            list projection codes.
	 */
	private void sendCatalogEvent(final String type, final String store, final Date modifiedDateTime, final List<String> codes) {
		EventType eventType = eventTypeFactory.getEventType(type);

		Map<String, Object> eventData = new HashMap<>();
		eventData.put("type", type);
		eventData.put("store", store);
		eventData.put("modifiedDateTime",
				ZonedDateTime.ofInstant(modifiedDateTime.toInstant(), ZoneId.of("GMT")).format(ISO_OFFSET_DATE_TIME));
		eventData.put("codes", codes);

		EventMessage catalogEventMessage = eventMessageFactory.createEventMessage(eventType, EVENT_MESSAGE_GUID, eventData);
		eventMessagePublisher.publish(catalogEventMessage);
	}

	/**
	 * Since result from database calculates list of projection maybe pagination limit + {@value HAS_NEXT}, we should check size of result list and
	 * drops last item if it needs.
	 *
	 * @param projections list of projections.
	 * @param pagination  is pagination for request.
	 * @param <T>         is type of projection.
	 * @return valid list of projection.
	 */
	private <T extends Projection> List<T> calculateResult(final List<T> projections, final PaginationRequest pagination) {
		if (projections.size() > pagination.getLimit()) {
			return projections.subList(0, pagination.getLimit());
		} else {
			return projections;
		}
	}

	/**
	 * Since result from database calculates list of projection maybe pagination limit + {@value HAS_NEXT}, we can find out if next result exists
	 * for current request and calculate the pagination for next request.
	 * drops last item if it needs.
	 *
	 * @param projections list of projections.
	 * @param pagination  is pagination for request.
	 * @return pagination for next request.
	 */
	private PaginationResponse calculatePaginationResponse(final List<? extends Projection> projections, final PaginationRequest pagination) {
		if (projections.size() > pagination.getLimit()) {
			return new PaginationResponseImpl(pagination.getLimit(),
					projections.get(pagination.getLimit() - PREVIOUS).getIdentity().getCode(),
					true);
		} else {
			return new PaginationResponseImpl(pagination.getLimit(), StringUtils.EMPTY, false);
		}
	}


	/**
	 * Adds current time to response if start after param empty in request.
	 *
	 * @param pagination is pagination for request.
	 * @return current time.
	 */
	private ZonedDateTime maybeAddCurrentTime(final PaginationRequest pagination) {
		return StringUtils.isEmpty(pagination.getStartAfter())
				? ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"))
				: null;
	}

	/**
	 * Creates {@link ProjectionHistoryEntity} from exist and new projection entities.
	 *
	 * @param existProjectionEntity exist projection entity.
	 * @param newProjectionEntity   new projection entity.
	 * @return ProjectionHistoryEntity.
	 */
	private ProjectionHistoryEntity createProjectionHistoryEntity(final ProjectionEntity existProjectionEntity,
																  final ProjectionEntity newProjectionEntity) {
		final ProjectionHistoryEntity historyEntity = projectionToEntityConverter.convertToProjectionHistory(existProjectionEntity);
		historyEntity.setProjectionEntity(newProjectionEntity);

		return historyEntity;
	}

	/**
	 * Find {@link ProjectionEntity} in database by {@link NameIdentity}.
	 *
	 * @param nameIdentity name identity of {@link Projection}
	 * @return {@link ProjectionEntity}
	 */
	private Optional<ProjectionEntity> extractProjectionEntity(final NameIdentity nameIdentity) {
		return projectionRepository.extractProjectionEntity(nameIdentity.getType(), nameIdentity.getCode(), nameIdentity.getStore());
	}

	/**
	 * Extracts codes from list of projection entities.
	 *
	 * @param projectionEntities given list of projection entities.
	 * @return list of codes.
	 */
	private List<String> extractCodes(final List<ProjectionEntity> projectionEntities) {
		return projectionEntities.stream()
				.map(ProjectionEntity::getCode)
				.collect(toList());
	}

	private void updateProjectionEntity(final ProjectionEntity oldProjectionEntity, final ProjectionEntity updatedProjectionEntity) {
		oldProjectionEntity.setProjectionDateTime(updatedProjectionEntity.getProjectionDateTime());
		oldProjectionEntity.setDeleted(updatedProjectionEntity.isDeleted());
		oldProjectionEntity.setSchemaVersion(updatedProjectionEntity.getSchemaVersion());
		oldProjectionEntity.setContentHash(updatedProjectionEntity.getContentHash());
		oldProjectionEntity.setContent(updatedProjectionEntity.getContent());
		oldProjectionEntity.setDisableDateTime(updatedProjectionEntity.getDisableDateTime());
		oldProjectionEntity.setGuid(updatedProjectionEntity.getGuid());
	}
}
