/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.tags.service.impl;

import com.elasticpath.tags.dao.TagDefinitionDao;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.service.TagDefinitionFactory;
import com.elasticpath.tags.service.TagDefinitionService;

/**
 * Implementation of TagDefinitionService interface.
 */
public class TagDefinitionServiceImpl implements TagDefinitionService {

	private TagDefinitionDao tagDefinitionDao;

	private TagDefinitionFactory tagDefinitionFactory;

	@Override
	public void saveOrUpdate(final TagDefinition tagDefinition) {
		tagDefinitionDao.saveOrUpdate(tagDefinition);

	}

	@Override
	public void delete(final TagDefinition tagDefinition) {
		tagDefinitionDao.remove(tagDefinition);
	}

	/**
	 * Find and return a TagDefinition by the guid.
	 *
	 * @param guid the guid of the TagDefinition to return
	 * @return the TagDefinition with matching guid, else null
	 */
	@Override
	public TagDefinition findByGuid(final String guid) {
		return tagDefinitionDao.findByGuid(guid);
	}

	/**
	 * Find and return a TagDefinition by the name.
	 *
	 * @param name the name of the TagDefinition to find and return
	 * @return the TagDefinition with matching name, else null
	 */
	@Override
	public TagDefinition findByName(final String name) {
		return tagDefinitionDao.findByName(name);
	}

	/**
	 * Create a new instance of a tag definition.
	 *
	 * @return a new, empty instance of a TagDefinition object
	 */
	@Override
	public TagDefinition create() {
		return tagDefinitionFactory.createTagDefinition();
	}

	/**
	 * TagDefinitionDao injection method.
	 *
	 * @param tagDefinitionDao TagDefinitionDao.
	 */
	public void setTagDefinitionDao(final TagDefinitionDao tagDefinitionDao) {
		this.tagDefinitionDao = tagDefinitionDao;
	}

	/**
	 * Set the factory used to create new TagDefinition objects. Typically configured via spring injection xml.
	 *
	 * @param tagDefinitionFactory the factor to use to create new TagDefinition instances
	 */
	public void setTagDefinitionFactory(final TagDefinitionFactory tagDefinitionFactory) {
		this.tagDefinitionFactory = tagDefinitionFactory;
	}
}