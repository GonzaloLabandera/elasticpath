/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.order.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderLock;
import com.elasticpath.service.cmuser.CmUserService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.InvalidUnlockerException;
import com.elasticpath.service.order.OrderLockService;
import com.elasticpath.service.order.OrderService;

/**
 * Provides storage and access to <code>OrderLock</code> objects. 
 */
public class OrderLockServiceImpl extends AbstractEpPersistenceServiceImpl implements OrderLockService {

	private static final Logger LOG = LogManager.getLogger(OrderLockServiceImpl.class);
	
	private TimeService timeService;

	private OrderService orderService;

	private CmUserService cmUserService;

	// for messaging purpose only
	private int orderIsLockedOrModified = VALIDATED_SUCCESSFULLY;
	
	@Override
	public OrderLock obtainOrderLock(final Order order, final CmUser cmUser, final Date openEditorDate) throws EpServiceException {
		final Order fresh = getOrderService().get(order.getUidPk());
		// Order was modified before user opened this order in editor.
		if (fresh.getLastModifiedDate().after(openEditorDate)) {
			// What is this field for? Why are we maintaining state in a singleton?
			// What happens when the OrderLockService singleton is invoked for different orders?
			orderIsLockedOrModified = ORDER_WAS_MODIFIED;
			return null;
		}

		final OrderLock orderLock = getPrototypeBean(ContextIdNames.ORDER_LOCK, OrderLock.class);
		orderLock.setOrder(order);
		orderLock.setCmUser(cmUser);
		orderLock.setCreatedDate(getTimeService().getCurrentTime().getTime());

		// What is this field for? Why are we maintaining state in a singleton?
		// What happens when the OrderLockService singleton is invoked for different orders?
		orderIsLockedOrModified = VALIDATED_SUCCESSFULLY;

		// Obtain order lock.
		try {
			final OrderLockService transactionalOrderLockService = getSingletonBean(ContextIdNames.ORDER_LOCK_SERVICE, OrderLockService.class);
			return transactionalOrderLockService.writeOrderLock(orderLock);
		} catch (final DataAccessException e) {
			LOG.info("Lock already exists for order [" + orderLock.getOrder().getGuid() + "]");
			return null;
		}
	}
	
	@Override
	public int validateLock(final OrderLock orderLock, final Date openEditorDate) {
		if (orderLock == null) {
			if (ORDER_WAS_MODIFIED == orderIsLockedOrModified) {
				return ORDER_WAS_MODIFIED;
			}
			return ORDER_IS_LOCKED;
		}
		final Order freshOrder = getOrderService().get(orderLock.getOrder().getUidPk());
		final OrderLock freshOrderLock = this.getOrderLock(freshOrder);
		if (freshOrderLock == null) {
			return ORDER_WAS_UNLOCKED;
		}
		if (freshOrderLock.getCreatedDate() != orderLock.getCreatedDate()) {
			return LOCK_IS_ALIEN;
		}
		if (freshOrder.getLastModifiedDate().after(openEditorDate)) {
			return ORDER_WAS_MODIFIED;
		}
		return VALIDATED_SUCCESSFULLY;
	}

	@Override
	public OrderLock getOrderLock(final Order order) throws EpServiceException {
		final List<OrderLock> results = getPersistenceEngine().retrieveByNamedQuery("ORDER_LOCK_BY_ORDER_UID", order.getUidPk());
		if (!results.isEmpty()) {
			return results.get(0);
			/** there can only be one order lock for the specified order. */
		}

		return null;
	}

	@Override
	public void releaseOrderLock(final OrderLock orderLock, final CmUser user) throws EpServiceException {
		if (!checkOrderLockSanity(orderLock, user)) {
			LOG.debug("The order lock for order " + orderLock.getOrder().getUidPk() + " was obtained for user " + orderLock.getOrder().getUidPk()
					+ " but is being released by user " + user);
			throw new InvalidUnlockerException("The order lock for order " + orderLock.getOrder().getUidPk()
					+ " is being released by incorrect user");
		}
		remove(orderLock);
	}

	@Override
	public void forceReleaseOrderLock(final OrderLock orderLock) throws EpServiceException {
		remove(orderLock);
	}

	protected TimeService getTimeService() {
		return timeService;
	}
	
	@Override
	public List<OrderLock> findAllOrderLocksBeforeDate(final long endTime, final int firstResult, final int maxResult) {
		return getPersistenceEngine().retrieveByNamedQuery("ORDER_LOCK_BY_TIME", new Object[] { endTime }, firstResult, maxResult);
	}

	private boolean checkOrderLockSanity(final OrderLock orderLock, final CmUser unlockerUser) {

		CmUser lockerUser = orderLock.getCmUser();

		return unlockerUser != null && lockerUser != null && unlockerUser.getUidPk() == lockerUser.getUidPk();
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	/**
	 * Get the order lock with the given UID. Return null if no matching record exists.
	 * 
	 * @param orderLockUid the order lock UID
	 * @return the order lock if UID exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	public OrderLock get(final long orderLockUid) throws EpServiceException {
		sanityCheck();
		OrderLock orderLock = null;
		if (orderLockUid <= 0) {
			orderLock = getPrototypeBean(ContextIdNames.ORDER_LOCK, OrderLock.class);
		} else {
			orderLock = getPersistentBeanFinder().get(ContextIdNames.ORDER_LOCK, orderLockUid);
		}
		return orderLock;
	}

	@Override
	public OrderLock add(final OrderLock orderLock) throws EpServiceException {
		sanityCheck();
		getPersistenceEngine().save(orderLock);
		return orderLock;

	}

	@Override
	public OrderLock writeOrderLock(final OrderLock orderLock) {
		return add(orderLock);
	}

	@Override
	public OrderLock writeOrderLock(final String orderUid, final long cmUserId) {
		CmUser cmUser = cmUserService.get(cmUserId);

		final OrderLock orderLock = getPrototypeBean(ContextIdNames.ORDER_LOCK, OrderLock.class);
		orderLock.setOrder(orderService.findOrderByOrderNumber(orderUid));
		orderLock.setCmUser(cmUser);
		orderLock.setCreatedDate(getTimeService().getCurrentTime().getTime());

		return add(orderLock);
	}

	@Override
	public void remove(final OrderLock orderLock) throws EpServiceException {
		sanityCheck();
		getPersistenceEngine().delete(orderLock);		
	}

	@Override
	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	protected CmUserService getCmUserService() {
		return cmUserService;
	}

	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}
}
