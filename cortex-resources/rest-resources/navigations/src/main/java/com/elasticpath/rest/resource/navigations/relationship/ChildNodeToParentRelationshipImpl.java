/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.navigations.relationship;

import java.util.List;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.navigations.ParentNodeToChildRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.PathIdentifier;

/**
 * Reverse link from the child to the parent navigation.
 */
public class ChildNodeToParentRelationshipImpl implements ParentNodeToChildRelationship.LinkFrom {

	private final NavigationIdentifier childIdentifier;

	/**
	 * Constructor.
	 *
	 * @param childIdentifier child navigation identifier
	 */
	@Inject
	public ChildNodeToParentRelationshipImpl(@RequestIdentifier final NavigationIdentifier childIdentifier) {
		this.childIdentifier = childIdentifier;
	}

	@Override
	public Observable<NavigationIdentifier> onLinkFrom() {
		IdentifierPart<List<String>> parentId = ((PathIdentifier) childIdentifier.getNodeId()).extractParentId();

		if (parentId.getValue().isEmpty()) {
			return Observable.empty();
		}

		return Observable.just(NavigationIdentifier.builder()
				.withNavigations(childIdentifier.getNavigations())
				.withNodeId(parentId)
				.build());
	}
}
