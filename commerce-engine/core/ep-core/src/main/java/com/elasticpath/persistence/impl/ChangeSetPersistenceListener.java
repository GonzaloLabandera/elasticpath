/**
 * Copyright (c) Elastic Path Software Inc., 2009
 */
package com.elasticpath.persistence.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.event.TransactionListener;
import com.elasticpath.commons.ThreadLocalMap;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.domain.objectgroup.BusinessObjectGroupMember;
import com.elasticpath.domain.objectgroup.BusinessObjectMetadata;
import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.openjpa.support.JPAUtil;
import com.elasticpath.service.changeset.ChangeSetMemberAction;
import com.elasticpath.service.changeset.ChangeSetPolicy;
import com.elasticpath.service.changeset.ChangeSetService;
import com.elasticpath.service.changeset.dao.ChangeSetMemberDao;
import com.elasticpath.service.changeset.helper.ChangeSetHelper;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.objectgroup.dao.BusinessObjectGroupDao;

/**
 * Listens to persistence events and updates the metadata for the change set.
 */
@SuppressWarnings("PMD.GodClass")
public class ChangeSetPersistenceListener extends AbstractLifecycleListener implements TransactionListener {

	private static final Logger LOG = LogManager.getLogger(ChangeSetPersistenceListener.class);
	private static final String KEY_ACTION = "action";

	private BeanFactory beanFactory;
	private ChangeSetMemberDao changeSetMemberDao;
	private BusinessObjectGroupDao businessObjectGroupDao;
	private TimeService timeService;
	private ChangeSetService changeSetService;
	private ChangeSetHelper changeSetHelper;
	private Set<String> ignoreClasses = new HashSet<>();
	private LifecycleEventFilter lifecycleEventFilter;

	// Keep the thread-locals together
	private final ThreadLocalMap<BusinessObjectDescriptor, BusinessObjectMetadata> actionMetadataMap = new ThreadLocalMap<>();
	private final ThreadLocal<Map<BusinessObjectDescriptor, BusinessObjectGroupMember>> newGroupMembers = ThreadLocal.withInitial(HashMap::new);
	private ThreadLocalMap<String, Object> metadataMap;

	private static final Map<ChangeSetMemberAction, Integer> ACTION_PRIORITY = new HashMap<>();
	static {
		// the lower the number the lower the priority
		ACTION_PRIORITY.put(ChangeSetMemberAction.ADD, 2);
		ACTION_PRIORITY.put(ChangeSetMemberAction.DELETE, 2);
		ACTION_PRIORITY.put(ChangeSetMemberAction.EDIT, 1);
		ACTION_PRIORITY.put(ChangeSetMemberAction.UNDEFINED, 0);
	}

	/**
	 * Adds or updates an existing object group member with its object metadata.
	 *
	 * @param objectDescriptor the object descriptor
	 * @param changeType the change operation type
	 */
	protected void addOrUpdateMetadata(final BusinessObjectDescriptor objectDescriptor, final ChangeType changeType) {

		BusinessObjectMetadata actionMetadata = getActionMetadataMap().get(objectDescriptor);
		boolean actionChanged = false;

		if (actionMetadata == null) {
			// no cached meta data exists for the object descriptor
			actionMetadata = createAndPopulateMetadata(KEY_ACTION, changeType, objectDescriptor);
			LOG.trace("Created action metadata {}", actionMetadata);
			actionChanged = true;
		} else {
			// see if the cached meta data action needs updating based on the new action
			String oldActionName = actionMetadata.getMetadataValue();
			String newActionName = resolveAction(ChangeSetMemberAction.getChangeSetMemberAction(oldActionName), getAction(changeType)).getName();
			if (!StringUtils.equals(newActionName, oldActionName)) {
				actionMetadata.setMetadataValue(newActionName);
				getActionMetadataMap().put(objectDescriptor, actionMetadata);
				actionChanged = true;
				LOG.trace("Updated action metadata {}", actionMetadata);
			}
		}
		if (actionMetadata.getBusinessObjectGroupMember() == null) {
			//Nothing to do.
			//The object should be already added into change set in stage 1
			//This method should only be executed in stage2.
			//If the business object group member cannot be found, it means the object is related to some other object.
			//For example, single sku will not be added into change set, the product will be added into change set instead.
			LOG.debug("{} is not in the change set. It may be a dependency of another object.", objectDescriptor);
			return;
		}

		if (actionChanged) {
			getActionMetadataMap().put(objectDescriptor, getChangeSetMemberDao().addOrUpdateObjectMetadata(actionMetadata));
		}
	}

	/**
	 * Adds an object to a change set.
	 *
     * @param objectDescriptor the object descriptor
     * @return the groupMember if newly created during this call
     */
	protected BusinessObjectGroupMember addObjectToChangeSet(final BusinessObjectDescriptor objectDescriptor) {
		if (objectDescriptor != null && getMetadataMap().containsKey("addToChangeSetFlag")) {

			Map<String, String> changeSetMetaDataMap = new HashMap<>();

			changeSetMetaDataMap.put("addedByUserGuid", getChangeSetUserGuid());
			changeSetMetaDataMap.put("dateAdded", getCurrentDateString());

			return getChangeSetService().addObjectToChangeSet(getChangeSetGuid(), objectDescriptor, changeSetMetaDataMap, false);
		}
        return null;
    }

	/**
	 *
	 * @return the change set user GUID
	 */
	protected String getChangeSetUserGuid() {
		return (String) getMetadataMap().get("changeSetUserGuid");
	}

	private String getCurrentDateString() {
		return DateFormatUtils.format(getTimeService().getCurrentTime(), "yyyyMMddHHmmssSSS");
	}

	/**
	 * @return the time service
	 */
	public TimeService getTimeService() {
		if (timeService == null) {
			timeService = beanFactory.getSingletonBean(ContextIdNames.TIME_SERVICE, TimeService.class);
		}
		return timeService;
	}

	/**
	 *
	 * @return the changeSetService
	 */
	ChangeSetService getChangeSetService() {
		if (changeSetService == null) {
			changeSetService = beanFactory.getSingletonBean(ContextIdNames.CHANGESET_SERVICE, ChangeSetService.class);
		}
		return changeSetService;
	}


	/**
	 * Resolves which action should be set to the change set memeber.
	 *
	 * @param currentAction the currently set action
	 * @param newAction the new action to set
	 * @return the change set member action to set
	 */
	ChangeSetMemberAction resolveAction(final ChangeSetMemberAction currentAction, final ChangeSetMemberAction newAction) {
		int currentActionPriority = ACTION_PRIORITY.get(currentAction);
		int newActionPriority = ACTION_PRIORITY.get(newAction);
		if (currentActionPriority > newActionPriority) {
			return currentAction;
		}
		return newAction;
	}

	/**
	 * Checks whether the listener is active.
	 *
	 * @return true if active
	 */
	@SuppressWarnings("PMD.PositionLiteralsFirstInComparisons")
	boolean isListenerActive() {
		return StringUtils.isNotEmpty(getChangeSetGuid())
			&& ObjectUtils.equals("stage2", getMetadataMap().get("activeImportStage"));
	}

	/**
	 * Creates a {@code BusinessObjectMetaData} object, populates it with the parameters and returns it.
	 *
	 * @param key The key.
	 * @param changeType The type of change.
	 * @param objectDescriptor The businessObjectDescriptor.
	 * @return The new object.
	 */
	protected BusinessObjectMetadata createAndPopulateMetadata(final String key,
			final ChangeType changeType, final BusinessObjectDescriptor objectDescriptor) {

		BusinessObjectMetadata metadata = beanFactory.getPrototypeBean(ContextIdNames.BUSINESS_OBJECT_METADATA, BusinessObjectMetadata.class);
		metadata.setMetadataKey(key);
		metadata.setMetadataValue(getAction(changeType).getName());

		// Prevent a 'instance already existing in L1 cache' issue, shortcut to the newly created instance, so we avoid flushing to the DB.
		BusinessObjectGroupMember businessObjectGroupMember = newGroupMembers.get().get(objectDescriptor);
		if (businessObjectGroupMember == null) {
           businessObjectGroupMember = getBusinessObjectGroupDao().findGroupMemberByGroupIdObjectDescriptor(getChangeSetGuid(), objectDescriptor);
        }
		metadata.setBusinessObjectGroupMember(businessObjectGroupMember);
		return metadata;
	}

	/**
	 * @return The guid for the change set or null if change sets are not operating.
	 */
	protected String getChangeSetGuid() {
		return (String) getMetadataMap().get("changeSetGuid");
	}

	private ChangeSetPolicy getChangeSetPolicy() {
		return getBeanFactory().getSingletonBean(ContextIdNames.CHANGESET_POLICY, ChangeSetPolicy.class);
	}

	/**
	 * Resolves the object descriptor for {@code source}.
	 * @param source The object to resolve for.
	 * @return The BusinessObjectDescriptor or null if {@code source} is not supported by the change set framework.
	 */
	protected BusinessObjectDescriptor getBusinessObjectDescriptor(final Object source) {
		return getChangeSetPolicy().resolveObjectDescriptor(source);
	}

	/**
	 * Converts a change set type into a change set member action.
	 */
	private ChangeSetMemberAction getAction(final ChangeType changeType) {
		if (ChangeType.CREATE.equals(changeType)) {
			return ChangeSetMemberAction.ADD;
		} else if (ChangeType.DELETE.equals(changeType)) {
			return ChangeSetMemberAction.DELETE;
		} else if (ChangeType.UPDATE.equals(changeType)) {
			return ChangeSetMemberAction.EDIT;
		}
		return null;
	}

	/**
	 *
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 *
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@SuppressWarnings("fallthrough")
	@Override
	public void eventOccurred(final LifecycleEvent event) {
		if (!isListenerActive()) {
			// the listener is not enabled
			return;
		}
		if (ignoreObject(event.getSource())) {
			return;
		}


		final PersistenceCapable pcObject = (PersistenceCapable) event.getSource();
		switch (event.getType()) {
			case LifecycleEvent.BEFORE_ATTACH:
			case LifecycleEvent.BEFORE_STORE:
				logEvent(event);
				if (JPAUtil.hasStateManager(pcObject)) {
					processChangeSetData(ChangeType.UPDATE, pcObject);
				} else {
					processChangeSetData(ChangeType.CREATE, pcObject);
				}
				break;
			case LifecycleEvent.BEFORE_UPDATE:
				logEvent(event);
				processChangeSetData(ChangeType.UPDATE, pcObject);
				break;
			case LifecycleEvent.BEFORE_PERSIST:
				logEvent(event);
				processChangeSetData(ChangeType.CREATE, pcObject);
				break;
			case LifecycleEvent.BEFORE_DELETE:
				logEvent(event);
				processChangeSetData(ChangeType.DELETE, pcObject);
				break;
			default:
				// not supported event
		}
	}

	private void logEvent(final LifecycleEvent event) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing {}", event.getType());
		}
	}

	private boolean ignoreObject(final Object object) {
		if (ignoreClasses.contains(object.getClass().getName())) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("{} is ignored by changesetPersistenceListener!!!", object.getClass().getName());
			}
			return true;
		}
		return false;
	}

	/**
	 * Process the change set data.
	 *
	 * @param changeType the change operation type
	 * @param object the object we are actually storing
	 */
	protected void processChangeSetData(final ChangeType changeType, final PersistenceCapable object) {
		final BusinessObjectDescriptor objectDescriptor = getBusinessObjectDescriptor(object);

		EventActionEnum eventActionEnum = getEventActionEnum(changeType);
		if (objectDescriptor == null || lifecycleEventFilter.wasPreviouslyProcessed(eventActionEnum,
				(Class<PersistenceCapable>) object.getClass(), objectDescriptor.getObjectIdentifier())) {
			return;
		}
		lifecycleEventFilter.trackProcessed(eventActionEnum, (Class<PersistenceCapable>) object.getClass(), objectDescriptor.getObjectIdentifier());

		if (newGroupMembers.get().get(objectDescriptor) != null) {
			return;
		}

		BusinessObjectGroupMember groupMember = addObjectToChangeSet(objectDescriptor);
		if (groupMember != null && JPAUtil.isNew((PersistenceCapable) groupMember)) {
			// Cache so we can reuse, avoiding triggering a database flush.
			newGroupMembers.get().put(objectDescriptor, groupMember);
		}

		addOrUpdateMetadata(objectDescriptor, changeType);
	}

	private EventActionEnum getEventActionEnum(final ChangeType changeType) {
		EventActionEnum eventType = EventActionEnum.CREATED;
		if (changeType == ChangeType.DELETE) {
			eventType = EventActionEnum.DELETED;
		} else if (changeType == ChangeType.UPDATE) {
			eventType = EventActionEnum.UPDATED;
		}
		return eventType;
	}


	/**
	 *
	 * @return The dao for changeSetMembers.
	 */
	protected ChangeSetMemberDao getChangeSetMemberDao() {
		if (changeSetMemberDao == null) {
			changeSetMemberDao = beanFactory.getSingletonBean(ContextIdNames.CHANGE_SET_MEMBER_DAO,
					ChangeSetMemberDao.class);
		}
		return changeSetMemberDao;
	}

	/**
	 *
	 * @return the dao for BusinessObjectGroups
	 */
	protected BusinessObjectGroupDao getBusinessObjectGroupDao() {
		if (businessObjectGroupDao == null) {
			businessObjectGroupDao = beanFactory.getSingletonBean(ContextIdNames.BUSINESS_OBJECT_GROUP_DAO,
					BusinessObjectGroupDao.class);
		}
		return businessObjectGroupDao;
	}

	/**
	 * Get the map of metadata for this listener.
	 *
	 * @return the threadLocalMap
	 */
	protected ThreadLocalMap<String, Object> getMetadataMap() {
		return metadataMap;
	}

	/**
	 * Set the map of metadata for this listener.
	 *
	 * @param threadLocalMap the threadLocalMap to set
	 */
	public void setMetadataMap(final ThreadLocalMap<String, Object> threadLocalMap) {
		metadataMap = threadLocalMap;
	}

	/**
	 * Set the ignore classes.
	 *
	 * @param ignoreClasses the ignore classes
	 */
	public void setIgnoreClasses(final Set<String> ignoreClasses) {
		this.ignoreClasses = ignoreClasses;
	}

	/**
	 * Gets the change set helper.
	 *
	 * @return the change set helper
	 */
	public ChangeSetHelper getChangeSetHelper() {
		if (changeSetHelper == null) {
			changeSetHelper = beanFactory.getSingletonBean(ContextIdNames.CHANGESET_HELPER, ChangeSetHelper.class);
		}
		return changeSetHelper;
	}

	/**
	 * Gets the action metadata map.
	 *
	 * @return the action metadata map
	 */
	protected ThreadLocalMap<BusinessObjectDescriptor, BusinessObjectMetadata> getActionMetadataMap() {
		return actionMetadataMap;
	}

	private void resetAfterTransaction() {
		if (!isListenerActive()) {
			return;
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Resetting thread local storage after transaction");
		}
		getActionMetadataMap().clear();
		lifecycleEventFilter.endTransaction();
	}


	@Override
	public void afterBegin(final TransactionEvent event) {
		lifecycleEventFilter.beginTransaction();
	}

	@Override
	public void afterCommit(final TransactionEvent event) {
		resetAfterTransaction();
	}

	@Override
	public void afterRollback(final TransactionEvent event) {
		resetAfterTransaction();
	}

	@Override
	public void afterFlush(final TransactionEvent event) {
		if (isListenerActive()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Resetting new member cache after flush.");
			}
			newGroupMembers.set(new HashMap<>());
		}
	}

	@Override
	public void beforeCommit(final TransactionEvent event) { /* noop */ }

	@Override
	public void afterStateTransitions(final TransactionEvent event) { /*noop*/ }

	@Override
	public void afterCommitComplete(final TransactionEvent event) { /*noop*/ }

	@Override
	public void afterRollbackComplete(final TransactionEvent event) { /*noop*/ }

	@Override
	public void beforeFlush(final TransactionEvent event) { /*noop*/ }

	protected LifecycleEventFilter getLifecycleEventFilter() {
		return lifecycleEventFilter;
	}

	public void setLifecycleEventFilter(final LifecycleEventFilter lifecycleEventFilter) {
		this.lifecycleEventFilter = lifecycleEventFilter;
	}
}
