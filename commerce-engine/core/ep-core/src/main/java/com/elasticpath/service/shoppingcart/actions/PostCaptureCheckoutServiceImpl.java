/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;

/**
 * Implements {@link PostCaptureCheckoutService}.
 */
public class PostCaptureCheckoutServiceImpl implements PostCaptureCheckoutService {

	private static final Logger LOG = Logger.getLogger(PostCaptureCheckoutServiceImpl.class);

	private List<ReversiblePostCaptureCheckoutAction> reversiblePostCaptureCheckoutActions;

	private List<ReversibleCheckoutAction> reversibleActionList;

	@Override
	public void completeCheckout(final PostCaptureCheckoutActionContext context) {
		final List<ReversiblePostCaptureCheckoutAction> executedActions = new ArrayList<>();
		try {
			getReversiblePostCaptureCheckoutActions().forEach(action -> {
				action.execute(context);
				executedActions.add(action);
			});
		} catch (final Exception e) {
			rollbackCheckout(executedActions, context, e);
			throw new EpServiceException("Checkout failed.", e);
		}
	}

	/**
	 * Call the rollback command on each ReversiblePostCaptureCheckoutAction that has been executed and invoke
	 * the cleanup of every PostCaptureOrderFailureCheckoutAction.
	 *
	 * @param executedActions - a list of ReversiblePostCaptureCheckoutActions that have been invoked at the time of rollback
	 * @param context - the PostCaptureCheckoutContext to use to execute the rollback
	 * @param causeForRollback - the exception that triggered the rollback processing
	 */
	protected void rollbackCheckout(final List<ReversiblePostCaptureCheckoutAction> executedActions, final PostCaptureCheckoutActionContext context,
			final Exception causeForRollback) {
		Lists.reverse(executedActions).forEach(action -> {
			try {
				action.rollback(context);
			} catch (Exception ex) {
				LOG.error("Exception thrown during checkout rollback", ex);
			}
		});
		Lists.reverse(reversibleActionList).forEach(action -> {
			if (action instanceof PostCaptureOrderFailureCheckoutAction) {
				try {
					((PostCaptureOrderFailureCheckoutAction) action).postCaptureRollback(context, causeForRollback);
				} catch (Exception ex) {
					LOG.error("Exception thrown during checkout rollback", ex);
				}
			}
		});
	}

	protected List<ReversiblePostCaptureCheckoutAction> getReversiblePostCaptureCheckoutActions() {
		return reversiblePostCaptureCheckoutActions;
	}

	public void setReversiblePostCaptureCheckoutActions(final List<ReversiblePostCaptureCheckoutAction> checkoutActions) {
		this.reversiblePostCaptureCheckoutActions = checkoutActions;
	}

	protected List<ReversibleCheckoutAction> getReversibleActionList() {
		return reversibleActionList;
	}

	public void setReversibleActionList(final List<ReversibleCheckoutAction> reversibleActionList) {
		this.reversibleActionList = reversibleActionList;
	}
}
