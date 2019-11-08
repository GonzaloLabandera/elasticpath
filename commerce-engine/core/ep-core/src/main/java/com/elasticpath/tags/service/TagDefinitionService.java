/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */

package com.elasticpath.tags.service;

import com.elasticpath.tags.domain.TagDefinition;

/**
 * Service interface for TagDefinition domain object.
 */
public interface TagDefinitionService {

	/**
	 * Create or update a TagDefinition.
	 *
	 * @param tagDefinition the tag definition.
	 */
	void saveOrUpdate(TagDefinition tagDefinition);

	/**
	 * Delete the tag definition.
	 *
	 * @param tagDefinition tag definition.
	 */
	void delete(TagDefinition tagDefinition);

	/**
	 * Find and return a TagDefinition by the guid.
	 *
	 * @param guid the guid of the TagDefinition to return
	 * @return the TagDefinition with matching guid, else null
	 */
	TagDefinition findByGuid(String guid);

	/**
	 * Find and return a TagDefinition by the name.
	 *
	 * @param name the name of the TagDefinition to find and return
	 * @return the TagDefinition with matching name, else null
	 */
	TagDefinition findByName(String name);

	/**
	 * Create a new instance of a tag definition.
	 *
	 * @return a new, empty instance of a TagDefinition object
	 */
	TagDefinition create();

}
