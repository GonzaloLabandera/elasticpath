/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.auth.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.auth.OAuth2AccessTokenMemento;
import com.elasticpath.domain.auth.impl.OAuth2AccessTokenMementoImpl;
import com.elasticpath.service.auth.OAuth2AccessTokenService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;

/**
 * Implementation of OAuthTokenService.
 */
public class OAuth2AccessTokenServiceImpl extends AbstractEpPersistenceServiceImpl implements OAuth2AccessTokenService {
	private static final Logger LOG = Logger.getLogger(OAuth2AccessTokenServiceImpl.class);

	@Override
	public void removeTokensByDate(final Date removalDate) {

		if (removalDate == null) {
			LOG.error("null removalDate");
		}
		final Object[] parameters = new Object[] {
				removalDate
		};
		getPersistenceEngine().executeNamedQuery("REMOVE_OAUTHACCESSTOKEN_BY_DATE", parameters);
	}

	@Override
	public OAuth2AccessTokenMemento saveOrUpdate(final OAuth2AccessTokenMemento oauthToken) {
		return getPersistenceEngine().saveOrMerge(oauthToken);
	}

	@Override
	public OAuth2AccessTokenMemento load(final String tokenId) {
		final Object[] parameters = new Object[] {
				tokenId
		};
		final List<OAuth2AccessTokenMemento> results = getPersistenceEngine().retrieveByNamedQuery("FIND_OAUTHACCESSTOKEN_BY_TOKENID", parameters);

		if (results.size() != 1) {
			return null;
		}

		return results.get(0);
	}

	@Override
	public void remove(final String tokenId) {
		final Object[] parameters = new Object[] {
				tokenId
		};
		getPersistenceEngine().executeNamedQuery("REMOVE_OAUTHACCESSTOKEN_BY_TOKENID", parameters);
	}

	@Override
	public OAuth2AccessTokenMemento getObject(final long uid) throws EpServiceException {
		return getPersistenceEngine().load(OAuth2AccessTokenMementoImpl.class, uid);
	}

}
