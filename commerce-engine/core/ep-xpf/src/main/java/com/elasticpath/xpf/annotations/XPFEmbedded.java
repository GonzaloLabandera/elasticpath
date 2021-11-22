/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that marks that an extension implementation class is provided by EP and should be allowed access to the
 * spring context.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XPFEmbedded {

}
