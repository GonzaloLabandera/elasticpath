/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.KeyValueSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.type.ImmutableSubject;
import com.elasticpath.rest.relos.rs.subject.SubjectStorage;
import com.elasticpath.rest.relos.rs.subject.util.SubjectHeadersUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants;

/**
 * Enhances the subject with metadata attributes from the header.
 * Replaces the subject stored in the subjectStorage TL.
 *
 * This is a bit of a hack, as it should really be done in cortex/api-platform.
 * However, we don't want b2b business case logic leaking into api-platform, so this will do for now.
 *
 * Also, this needs to be done AFTER the subject has been placed into subject storage,
 * which currently is done by a filter with service ranking of 2. Therefore this has a service ranking of 1 to follow that.
 * 
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=1",
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/*"  }
)
public class SubjectEnhancementFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectEnhancementFilter.class);

	@Reference
	private SubjectStorage subjectStorage;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		Collection<String> scopes = SubjectHeadersUtil.getUserScopesFromRequest(httpRequest);
		if (scopes.size() > 1) {
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			try (PrintWriter writer = httpResponse.getWriter()) {
				writer.write("Too many scopes");
			}
			return;
		}



		String userMetadataFromRequest = SubjectHeadersUtil.getUserMetadataFromRequest(httpRequest);

		if (userMetadataFromRequest != null) {
			JsonObject metadata = readBase64EncodedJson(userMetadataFromRequest);
			if (metadata != null) {
				Subject subject = subjectStorage.getSubject("");
				Collection<SubjectAttribute> attributes = new ArrayList<>(subject.getAttributes());
				String headerMetadataSubjectUserName = safeGetString(metadata, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME);
				String headerMetadataSubjectUserId = safeGetString(metadata, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID);
				String headerMetadataSubjectUserEmail = safeGetString(metadata, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL);

				attributes.add(new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME,
						headerMetadataSubjectUserName, ShoppingCartResourceConstants.METADATA));
				attributes.add(new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL,
						headerMetadataSubjectUserEmail, ShoppingCartResourceConstants.METADATA));
				attributes.add(new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID,
						headerMetadataSubjectUserId, ShoppingCartResourceConstants.METADATA));

				Subject wrappedSubject = new ImmutableSubject(subject.getPrincipals(), attributes);

				subjectStorage.storeSubject(wrappedSubject);
			}
		}

		chain.doFilter(request, response);

	}


	private JsonObject readBase64EncodedJson(final String base64EncodedJson) {
		try {
			byte[] jsonBytes = Base64.getDecoder().decode(base64EncodedJson);
			ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonBytes);

			try (JsonReader reader = Json.createReader(jsonInputStream)) {
				return reader.readObject();
			}
		} catch (IllegalArgumentException | JsonException e) {
			LOG.error("Error decoding payload {}", base64EncodedJson, e);
			return null;
		}
	}

	private String safeGetString(final JsonObject jsonObject, final String key) {
		if (jsonObject.containsKey(key)) {
			try {
				return jsonObject.getString(key);
			} catch (ClassCastException e) {
				LOG.error("The value for key '{}' is not a string", key, e);
				return null;
			}
		}
		return null;
	}



	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// nothing
	}

	@Override
	public void destroy() {
		// nothing
	}
}
