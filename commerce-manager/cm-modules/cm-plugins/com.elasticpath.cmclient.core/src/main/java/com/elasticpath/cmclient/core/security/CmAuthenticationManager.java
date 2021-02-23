/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.security;

import java.util.Collection;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.rcp.RemoteAuthenticationException;
import org.springframework.security.authentication.rcp.RemoteAuthenticationManagerImpl;
import org.springframework.security.core.GrantedAuthority;

import com.elasticpath.service.cmuser.CmUserService;

/**
 * Server-side processor of a remote authentication request.
 * <P>
 * This bean requires no security interceptor to protect it. Instead, the bean uses the configured <code>CmAuthenticationManager</code> to resolve an
 * authentication request.
 * </p>
 */
public class CmAuthenticationManager extends RemoteAuthenticationManagerImpl {

	private CmUserService cmUserService;

	@Override
	public Collection<? extends GrantedAuthority> attemptAuthentication(final String username, final String password)
		throws RemoteAuthenticationException {

		try {
			return getAuthorities(username, password);
		} catch (BadCredentialsException e) {
			handleCmUserFailedLoginAttempt(username);
			throw e;
		}
	}

	private Collection<? extends GrantedAuthority> getAuthorities(final String username, final String password) {
		final UsernamePasswordAuthenticationToken request = new UsernamePasswordAuthenticationToken(username, password);
		return getAuthenticationManager().authenticate(request).getAuthorities();
	}

	private void handleCmUserFailedLoginAttempt(final String userName) {
		if (userName != null) {
			cmUserService.addFailedLoginAttempt(userName);
		}
	}

	/**
	 * Sets the cmUser service.
	 *
	 * @param cmUserService the cmUserService
	 */
	public void setCmUserService(final CmUserService cmUserService) {
		this.cmUserService = cmUserService;
	}

}
