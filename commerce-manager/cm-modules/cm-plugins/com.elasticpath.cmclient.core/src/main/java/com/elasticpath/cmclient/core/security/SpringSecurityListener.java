/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.security;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.AuthenticationException;

import com.elasticpath.domain.cmuser.CmUser;

/**
 * Listener that is responsible for handling spring security events.
 *
 */
public class SpringSecurityListener implements ApplicationListener<ApplicationEvent> {
	
	@Override
	public void onApplicationEvent(final ApplicationEvent event) {
		if (event instanceof AuthenticationFailureBadCredentialsEvent) {
			final AuthenticationFailureBadCredentialsEvent badCredentialsEvent = (AuthenticationFailureBadCredentialsEvent) event;

			handleBadCredentialsEvent(badCredentialsEvent);
		}

	}

	private void handleBadCredentialsEvent(final AuthenticationFailureBadCredentialsEvent badCredentialsEvent) {
		final AuthenticationException exception = badCredentialsEvent.getException();
		final String userName = badCredentialsEvent.getAuthentication().getPrincipal().toString();
		if (exception instanceof BadCredentialsException && userName != null) {

			final CmUser cmUser = (CmUser) badCredentialsEvent.getAuthentication().getDetails();
			if (cmUser != null) {
				handleLastAttempt(cmUser);
			}
		}
	}

	private void handleLastAttempt(final CmUser cmUser) {
		if (!cmUser.isAccountNonLocked()) {
			throw new LockedException("Account has been locked.");
		}
	}
}
