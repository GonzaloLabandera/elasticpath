/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders;

import javax.servlet.http.HttpServletRequest;

import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;

/**
 * Builder for {@code com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext}.
 */
public interface HttpTagSetContextBuilder {
	/**
	 * Builds XPFHttpTagSetContext using the inputs provided.
	 *
	 * @param servletRequest the servlet request
	 * @return ShoppingItemValidationContext built using inputs provided
	 */
	XPFHttpTagSetContext build(HttpServletRequest servletRequest);

}
