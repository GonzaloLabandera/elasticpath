/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.service.objectgroup.dao.impl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.pagination.DirectedSortingField;
import com.elasticpath.commons.pagination.SortingField;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.misc.RandomGuid;
import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.domain.objectgroup.BusinessObjectGroupMember;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.DirectedSortingFieldException;
import com.elasticpath.service.changeset.ChangeSetMemberSortingField;
import com.elasticpath.service.objectgroup.dao.BusinessObjectGroupDao;

/**
 * The default implementation of {@link BusinessObjectGroupDao}.
 */
public class BusinessObjectGroupDaoImpl implements BusinessObjectGroupDao {

	private PersistenceEngine persistenceEngine;
	private ElasticPath elasticPath;

	@Override
	public void addGroupMember(final BusinessObjectGroupMember objectGroupMember) {
		persistenceEngine.save(objectGroupMember);
	}

	@Override
	public BusinessObjectGroupMember findGroupMemberByGuid(final String guid) {
		List<BusinessObjectGroupMember> result = persistenceEngine.retrieveByNamedQuery("FIND_OBJECT_MEMBER_BY_GUID", guid);
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public Collection<BusinessObjectGroupMember> findGroupMembersByGroupId(final String groupId) {
		return persistenceEngine.retrieveByNamedQuery("FIND_OBJECT_MEMBERS_BY_GROUPID", groupId);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Implementation Note:</b> Current implementation only supports a single sorting field.
	 */
	@Override
	public Collection<BusinessObjectGroupMember> findGroupMembersByGroupId(final String groupId,
			final int startIndex, final int maxResults, final DirectedSortingField [] sortingFields) {

		return findFilteredGroupMembersByGroupId(groupId, startIndex, maxResults, sortingFields, null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Implementation Note:</b> Current implementation only supports a single sorting field.
	 */
	@Override
	public Collection<BusinessObjectGroupMember> findFilteredGroupMembersByGroupId(final String groupId,
			final int startIndex, final int maxResults, final DirectedSortingField [] sortingFields, final List<String> objectTypeFilter) {

		sanityCheck(groupId, startIndex, maxResults, sortingFields);

		//only supports one sorting field
		SortingField sortingField = sortingFields[0].getSortingField();

		if (!sortingField.equals(ChangeSetMemberSortingField.OBJECT_ID)
			&& !sortingField.equals(ChangeSetMemberSortingField.OBJECT_TYPE)) {

			return findFilteredGroupMembersByGroupIdAndMetaData(groupId, startIndex, maxResults, sortingFields, objectTypeFilter);
		}

		StringBuilder query = new StringBuilder("SELECT gm FROM BusinessObjectGroupMemberImpl gm ")
			.append("WHERE gm.groupId = ?1 ");

			if (CollectionUtils.isNotEmpty(objectTypeFilter)) {
				query.append("AND gm.objectType NOT IN (:list) ");
			}

			query.append("ORDER BY gm.")
			.append(sortingField.getName())
			.append(' ')
			.append(sortingFields[0].getSortingDirection());

		return persistenceEngine
			.retrieveWithList(query.toString(), "list", objectTypeFilter, new Object[] { groupId }, startIndex, maxResults);
	}


	/**
	 * Find filtered group members by group id and meta data.
	 *
	 * @param groupId the group id
	 * @param startIndex the start index
	 * @param maxResults the max results
	 * @param sortingFields the sorting fields
	 * @param objectTypeFilter the object type filter
	 * @return the collection
	 */
	protected Collection<BusinessObjectGroupMember> findFilteredGroupMembersByGroupIdAndMetaData(final String groupId,
			final int startIndex, final int maxResults, final DirectedSortingField [] sortingFields, final List<String> objectTypeFilter) {

		sanityCheck(groupId, startIndex, maxResults, sortingFields);

		SortingField sortingField = sortingFields[0].getSortingField();

		if (sortingField.equals(ChangeSetMemberSortingField.OBJECT_ID)
				|| sortingField.equals(ChangeSetMemberSortingField.OBJECT_TYPE)) {

			return findGroupMembersByGroupId(groupId, startIndex, maxResults, sortingFields);
		}

		StringBuilder query = new StringBuilder("SELECT DISTINCT gm FROM BusinessObjectMetadataImpl meta ")
			.append("INNER JOIN meta.businessObjectGroupMember gm ")
			.append("WHERE gm.groupId = ?1 ");

			if (CollectionUtils.isNotEmpty(objectTypeFilter)) {
				query.append("AND gm.objectType NOT IN (:list) ");
			}

			query.append("AND meta.metadataKey='")
			.append(sortingField.getName())
			.append("' ORDER BY meta.metadataValue ")
			.append(sortingFields[0].getSortingDirection());

		return persistenceEngine.retrieveWithList(query.toString(), "list", objectTypeFilter, new Object[] { groupId },
			startIndex, maxResults);
	}

	private void sanityCheck(final String groupId, final int startIndex,
			final int maxResults, final DirectedSortingField[] sortingFields) {
		if (groupId == null || ArrayUtils.isEmpty(sortingFields)) {
			throw new DirectedSortingFieldException("Null-value/zero argument", "groupId", sortingFields, groupId);
		}
		if (startIndex < 0 || maxResults < 0) {
			throw new IllegalArgumentException(
					String.format("Negative-value argument: startIndex=%d, maxResults=%d", startIndex, maxResults));
		}
	}

	@Override
	public void removeGroupMember(final BusinessObjectDescriptor descriptor, final String objectGroupId) {
		persistenceEngine.executeNamedQuery("DELETE_GROUP_MEMBER_BY_OBJ_TYPE_AND_ID_AND_GROUP_ID",
				descriptor.getObjectType(), descriptor.getObjectIdentifier(), objectGroupId);
	}

	/**
	 * Sets the persistence engine to use.
	 *
	 * @param persistenceEngine The persistence engine.
	 */
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Check whether a group member referring to an object
	 * described by <code>objectDescritor</code> exists in the system.
	 *
	 * @param objectDescriptor the object descriptor
	 * @return true if a group member already exists
	 */
	@Override
	public boolean groupMemberExists(final BusinessObjectDescriptor objectDescriptor) {
		List<Object> result = persistenceEngine.retrieveByNamedQuery("GROUP_MEMBER_EXISTS_BY_OBJ_TYPE_AND_ID",
				objectDescriptor.getObjectType(), objectDescriptor.getObjectIdentifier());
		return !result.isEmpty();
	}

	/**
	 * Finds all the group IDs an object exists in.
	 *
	 * @param objectDescriptor the object descriptor
	 * @return a collection of group IDs
	 */
	@Override
	public Collection<String> findGroupIdsByDescriptor(final BusinessObjectDescriptor objectDescriptor) {
		return persistenceEngine.retrieveByNamedQuery("FIND_GROUP_IDS_BY_OBJ_TYPE_AND_ID",
				FlushMode.AUTO, true,
				new Object [] {
				objectDescriptor.getObjectType(),
				objectDescriptor.getObjectIdentifier()});
	}

	@Override
	public String generateGroupId() {
		final Object randomGuid = elasticPath.getPrototypeBean(ContextIdNames.RANDOM_GUID, RandomGuid.class);
		return randomGuid.toString();
	}

	/**
	 * Sets ElasticPath instance.
	 *
	 * @param elasticPath the instance to set
	 */
	public void setElasticPath(final ElasticPath elasticPath) {
		this.elasticPath = elasticPath;
	}

	@Override
	public void removeGroupMembersByGroupId(final String objectGroupId) {
		persistenceEngine.executeNamedQuery("DELETE_MEMBERS_BY_GROUP_ID", objectGroupId);
	}

	@Override
	public BusinessObjectGroupMember findGroupMemberByGroupIdObjectDescriptor(final String groupId,
                                                                              final BusinessObjectDescriptor objectDescriptor) {
		List<BusinessObjectGroupMember> result = persistenceEngine.retrieveByNamedQuery("FIND_OBJECT_MEMBER_BY_GROUPID_OBJ_TYPE_AND_ID",
				FlushMode.AUTO, true,
				new Object[] {groupId, objectDescriptor.getObjectType(), objectDescriptor.getObjectIdentifier()});
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public long getGroupMembersCount(final String groupId) {
		List<Long> result = persistenceEngine.retrieveByNamedQuery("COUNT_MEMBERS_BY_GROUP_ID", groupId);

		return result.get(0);
	}

	@Override
	public long getFilteredGroupMembersCount(final String groupId, final List<String> objectTypeFilter) {
		List<Long> result = persistenceEngine.retrieveByNamedQuery("COUNT_FILTERED_MEMBERS_BY_GROUP_ID", groupId, objectTypeFilter);

		return result.get(0);
	}

}
