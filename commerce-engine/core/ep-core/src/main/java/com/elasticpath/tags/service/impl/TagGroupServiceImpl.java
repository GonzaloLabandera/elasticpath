/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tags.service.impl;

import java.util.List;

import com.elasticpath.tags.dao.TagGroupDao;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.service.TagGroupFactory;
import com.elasticpath.tags.service.TagGroupService;

/**
 * Implementation of TagGroupService.
 */
public class TagGroupServiceImpl implements TagGroupService {

	private TagGroupDao tagGroupDao;

	private TagGroupFactory tagGroupFactory;

	@Override
	public void delete(final TagGroup tagGroup) {
		tagGroupDao.remove(tagGroup);
	}

	@Override
	public TagGroup create() {
		return tagGroupFactory.createTagGroup();
	}

	@Override
	public TagGroup findByGuid(final String guid) {
		return tagGroupDao.findByGuid(guid);
	}

	@Override
	public List<TagGroup> getTagGroups() {
		return tagGroupDao.getTagGroups();
	}

	@Override
	public void saveOrUpdate(final TagGroup tagGroup) {
		tagGroupDao.saveOrUpdate(tagGroup);
	}

	/**
	 * Mainly for Spring injection.
	 *
	 * @param tagGroupDao TagGroupDao
	 */
	public void setTagGroupDao(final TagGroupDao tagGroupDao) {
		this.tagGroupDao = tagGroupDao;
	}

	/**
	 * Set the factory used to create new TagGroup objects. Typically configured via spring xml.
	 * @param tagGroupFactory the factory to use to create new TagGroup objects
	 */
	public void setTagGroupFactory(final TagGroupFactory tagGroupFactory) {
		this.tagGroupFactory = tagGroupFactory;
	}
}


