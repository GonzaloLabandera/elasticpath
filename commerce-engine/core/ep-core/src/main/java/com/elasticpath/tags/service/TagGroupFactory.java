/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tags.service;

import com.elasticpath.tags.domain.TagGroup;

/**
 * Factory for creating tag groups.
 */
public interface TagGroupFactory {

	/**
	 * Create an empty tag group.
	 * @return a new, empty instance of a TagGroup object.
	 */
	TagGroup createTagGroup();

}
