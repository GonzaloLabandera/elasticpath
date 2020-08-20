/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence.openjpa.impl;

import javax.persistence.EntityManager;

import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;
import com.elasticpath.persistence.openjpa.routing.QueryRouter;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.persistence.openjpa.util.QueryRouterMetaInfoHolder;

/**
 * A factory class for creation of {@link QueryReader} instances. There could be more than one persistence engine in the application
 * (e.g. batch server) and each engine must have a unique instance of query readers, as well as query routers.
 *
 * {@link QueryRouter} is created through creation of query readers, for the same reason.
 *
 */
public class QueryReaderFactory {

	private EntityManager readOnlyEntityManager;
	private FetchPlanHelper fetchPlanHelper;
	private HDSSupportBean hdsSupportBean;
	private QueryRouterMetaInfoHolder queryRouterMetaInfoHolder;

	/**
	 * Create a new {@link QueryReader} and pass read-write entity manager for creation of a {@link QueryRouter}.
	 * @param readWriteEntityManager the read-write entity manager.
	 * @return a new instance of {@link QueryReader}
	 */
	public QueryReader createQueryReader(final EntityManager readWriteEntityManager) {
		QueryReader queryReader = new QueryReader();

		QueryRouter queryRouter = createQueryRouter(readWriteEntityManager);
		queryReader.setQueryRouter(queryRouter);
		queryReader.setFetchPlanHelper(fetchPlanHelper);

		return queryReader;
	}

	private QueryRouter createQueryRouter(final EntityManager readWriteEntityManager) {
		QueryRouter queryRouter = new QueryRouter();

		queryRouter.setReadWriteEntityManager(readWriteEntityManager);
		queryRouter.setReadOnlyEntityManager(readOnlyEntityManager);
		queryRouter.setHdsSupportBean(hdsSupportBean);
		queryRouter.setQueryRouterMetaInfoHolder(queryRouterMetaInfoHolder);
		queryRouter.init();

		return queryRouter;
	}

	public void setReadOnlyEntityManager(final EntityManager readOnlyEntityManager) {
		this.readOnlyEntityManager = readOnlyEntityManager;
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}

	public void setQueryRouterMetaInfoHolder(final QueryRouterMetaInfoHolder queryRouterMetaInfoHolder) {
		this.queryRouterMetaInfoHolder = queryRouterMetaInfoHolder;
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}
}
