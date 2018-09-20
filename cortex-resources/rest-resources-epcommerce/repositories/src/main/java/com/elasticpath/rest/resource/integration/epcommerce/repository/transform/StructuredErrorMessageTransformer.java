/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.transform;

import java.util.List;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.rest.advise.Message;

/**
 * Transforms a list of {@link StructuredErrorMessage} to a list of {@link Message}.
 */
public interface StructuredErrorMessageTransformer {
	/**
	 * Provides a way to generate {@link Message} for {@link StructuredErrorMessage}.
	 * @param structuredErrorMessageList the list of StructuredErrorMessage coming from ce.
	 * @return List<Message> Message list.
	 */
	List<Message> transform(List<StructuredErrorMessage> structuredErrorMessageList);
}
