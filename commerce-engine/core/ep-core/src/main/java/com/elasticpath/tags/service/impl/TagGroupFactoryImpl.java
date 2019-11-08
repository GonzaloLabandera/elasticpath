/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tags.service.impl;

import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.domain.impl.TagGroupImpl;
import com.elasticpath.tags.service.TagGroupFactory;

/**
 * Factory for creating tags.
 */
public class TagGroupFactoryImpl implements TagGroupFactory {

	/**
	 * Create an empty tag group.
	 *
	 * @return a new, empty instance of a TagGroup object.
	 */
	@Override
	public TagGroup createTagGroup() {
		return new TagGroupImpl();
	}
}
