/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tags.service.impl;

import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.impl.TagDefinitionImpl;
import com.elasticpath.tags.service.TagDefinitionFactory;

/**
 * Factory for creating tags.
 */
public class TagDefinitionFactoryImpl implements TagDefinitionFactory {

	/**
	 * Create an empty tag definition.
	 *
	 * @return a new, empty instance of a TagDefinition object.
	 */
	@Override
	public TagDefinition createTagDefinition() {
		return new TagDefinitionImpl();
	}
}
