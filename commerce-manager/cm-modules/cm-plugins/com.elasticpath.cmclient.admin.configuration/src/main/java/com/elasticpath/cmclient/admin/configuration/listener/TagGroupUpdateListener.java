/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.configuration.listener;

import com.elasticpath.tags.domain.TagGroup;

/**
 * Listener for when a tag group is updated.
 */
public interface TagGroupUpdateListener {

	/**
	 * Notify that a tag group has been updated.
	 * @param tagGroup the tag group that has been updated
	 */
	void tagGroupUpdated(TagGroup tagGroup);
}
