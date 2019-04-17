/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.springoauth2.tokenstore;

import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.authentication.dto.AccessTokenDto;
import com.elasticpath.rest.relos.rs.authentication.token.AccessTokenStrategy;

/**
 * Strategy for storing token to token store.
 */
public interface TokenStoreStrategy extends AccessTokenStrategy {

	/**
	 * Store the given token to token store.
	 *
	 * @param accessTokenDto the access token dto
	 * @return the execution result.
	 */
	ExecutionResult<Void> storeToken(AccessTokenDto accessTokenDto);

	/**
	 * Remove an access token from the database.
	 *
	 * @param tokenValue the token value
	 * @return the execution result
	 */
	ExecutionResult<Void> removeAccessToken(String tokenValue);
}
