/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tags.service;

import com.elasticpath.tags.domain.TagDefinition;

/**
 * Factory for creating tag definitions.
 */
public interface TagDefinitionFactory {

	/**
	 * Create an empty tag group.
	 *
	 * @return a new, empty instance of a TagDefinition object.
	 */
	TagDefinition createTagDefinition();

}
