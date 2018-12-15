/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.schema.uri;

import javax.inject.Provider;

/**
 * Factory for {@link ItemsUriBuilder}.
 *
 * @deprecated remove once dependent resources are converted to Helix.
 */
@Deprecated
public interface ItemsUriBuilderFactory extends Provider<ItemsUriBuilder> {

}
