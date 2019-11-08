/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.configuration.listener;

import com.elasticpath.tags.domain.TagDefinition;

/**
 * The listener for when a tag definition is updated.
 */
public interface TagDefinitionUpdateListener {

	/**
	 * Notify that a TagDefinition has been updated.
	 * @param tagDefinition the updated TagDefinition object
	 */
	void tagDefinitionUpdated(TagDefinition tagDefinition);

}
