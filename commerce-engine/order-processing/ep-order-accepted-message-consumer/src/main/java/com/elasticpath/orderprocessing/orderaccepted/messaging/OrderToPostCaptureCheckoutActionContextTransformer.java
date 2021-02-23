/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderaccepted.messaging;

import java.util.function.Function;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;

/**
 * Defining a transformation interface.
 */
public interface OrderToPostCaptureCheckoutActionContextTransformer extends Function<EventMessage, PostCaptureCheckoutActionContext> {

	/**
	 * Transform {@link EventMessage} to {@link PostCaptureCheckoutActionContext}.
	 *
	 * @param eventMessage the event message needs to be transformed.
	 * @return the context of post capture checkout action.
	 */
	PostCaptureCheckoutActionContext transform(EventMessage eventMessage);

	@Override
	default PostCaptureCheckoutActionContext apply(EventMessage eventMessage) {
		return transform(eventMessage);
	}

}
